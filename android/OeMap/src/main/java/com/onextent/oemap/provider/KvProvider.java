package com.onextent.oemap.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class KvProvider extends ContentProvider {

    private static final String AUTHORITY   = "com.onextent.oemap.provider.KvProvider";
    public  static final int    KV_LIST  = 1;
    public  static final int    KV_ID    = 2;
    public  static final Uri    CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Kv.CONTENT_PATH);

    private static final UriMatcher _uriMatcher;

    private SQLiteDatabase _db = null;

    static {
        _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        _uriMatcher.addURI(AUTHORITY, Kv.CONTENT_PATH, KV_LIST);
        _uriMatcher.addURI(AUTHORITY, Kv.CONTENT_PATH + "/#", KV_ID);
    }

    public static interface Kv extends BaseColumns {
        static String KEY               = KvDbHelper.KV_KEY;
        static String VALUE             = KvDbHelper.KV_VALUE;
        static String CONTENT_PATH      = KvDbHelper.KV_TABLE;
        static String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.onextent.oemap.provider.kv";
        static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.onextent.oemap.provider.kv";
        static String[] PROJECTION_ALL  = {_ID, KEY, VALUE};
        static String SORT_ORDER_DEFAULT = KEY + " ASC";
    }

    @Override
    public String getType(Uri uri) {
        switch (_uriMatcher.match(uri)) {
            case KV_LIST:
                return Kv.CONTENT_TYPE;
            case KV_ID:
                return Kv.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        _db = new KvDbHelper(getContext()).getWritableDatabase();
        if (_db == null) {
            return false;
        }
        if (_db.isReadOnly()) {
            _db.close();
            _db = null;
            return false;
        }
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Kv.CONTENT_PATH);
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = Kv.SORT_ORDER_DEFAULT;
        }
        switch (_uriMatcher.match(uri)) {
            case KV_LIST:
                // all nice and well
                break;
            case KV_ID:
                // limit query to one row at most:
                builder.appendWhere(Kv._ID + " = " + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        Cursor cursor = builder.query(_db, projection, selection, selectionArgs, null, null, sortOrder);
        // if we want to be notified of any changes:
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (_uriMatcher.match(uri) != KV_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        long id = _db.insert(Kv.CONTENT_PATH, null, values);
        if (id > 0) {
            // notify all listeners of changes and return itemUri:
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into " + Kv.CONTENT_PATH + ", uri: " + uri); // use another exception here!!!
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount = 0;
        switch (_uriMatcher.match(uri)) {
            case KV_LIST:
                delCount = _db.delete(Kv.CONTENT_PATH, selection, selectionArgs);
                break;
            case KV_ID:
                String idStr = uri.getLastPathSegment();
                String where = Kv._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = _db.delete(Kv.CONTENT_PATH, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

}

