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

public class SpaceProvider extends ContentProvider {

    private static final String AUTHORITY   = "com.onextent.oemap.provider.SpaceProvider";
    public  static final int    SPACE_LIST  = 1;
    public  static final int    SPACE_ID    = 2;
    public  static final Uri    CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Spaces.CONTENT_PATH);

    private static final UriMatcher _uriMatcher;

    private SQLiteDatabase _db = null;

    static {
        _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        _uriMatcher.addURI(AUTHORITY, Spaces.CONTENT_PATH, SPACE_LIST);
        _uriMatcher.addURI(AUTHORITY, Spaces.CONTENT_PATH + "/#", SPACE_ID);
    }

    public static interface Spaces extends BaseColumns {
        static String LEASE             = SpaceDbHelper.LEASE;
        static String SIZE_IN_METERS    = SpaceDbHelper.SIZE_IN_METERS;
        static String SIZE_IN_POINTS    = SpaceDbHelper.SIZE_IN_POINTS;
        static String CONTENT_PATH      = SpaceDbHelper.SPACE_TABLE;
        static String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.onextent.oemap.provider.spaces";
        static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.onextent.oemap.provider.spaces";
        static String[] PROJECTION_ALL  = {_ID, LEASE, SIZE_IN_METERS, SIZE_IN_POINTS};
        static String SORT_ORDER_DEFAULT = _ID + " ASC";
    }

    @Override
    public String getType(Uri uri) {
        switch (_uriMatcher.match(uri)) {
            case SPACE_LIST:
                return Spaces.CONTENT_TYPE;
            case SPACE_ID:
                return Spaces.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        _db = new SpaceDbHelper(getContext()).getWritableDatabase();
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
        builder.setTables(Spaces.CONTENT_PATH);
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = Spaces.SORT_ORDER_DEFAULT;
        }
        switch (_uriMatcher.match(uri)) {
            case SPACE_LIST:
                // all nice and well
                break;
            case SPACE_ID:
                // limit query to one row at most:
                builder.appendWhere(Spaces._ID + " = " + uri.getLastPathSegment());
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
        if (_uriMatcher.match(uri) != SPACE_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        long id = _db.insert(Spaces.CONTENT_PATH, null, values);
        if (id > 0) {
            // notify all listeners of changes and return itemUri:
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into " + Spaces.CONTENT_PATH + ", uri: " + uri); // use another exception here!!!
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount = 0;
        switch (_uriMatcher.match(uri)) {
            case SPACE_LIST:
                delCount = _db.delete(Spaces.CONTENT_PATH, selection, selectionArgs);
                break;
            case SPACE_ID:
                String idStr = uri.getLastPathSegment();
                String where = Spaces._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = _db.delete(Spaces.CONTENT_PATH, where, selectionArgs);
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

