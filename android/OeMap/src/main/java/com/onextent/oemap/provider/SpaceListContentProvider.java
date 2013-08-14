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

public class SpaceListContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.onextent.oemap.provider.SpaceListContentProvider";
    public static final int SPACE_LIST = 1;
    public static final int SPACE_ID = 2;
    private static final String SPACE_TABLE = SpaceListDbHelper.SPACE_TABLE;
    public static final String SPACE_NAME = SpaceListDbHelper.SPACE_NAME;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SPACE_TABLE);
    private static final UriMatcher sURIMatcher;
    private SQLiteDatabase _db = null;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, Spaces.CONTENT_PATH, SPACE_LIST);
        sURIMatcher.addURI(AUTHORITY, Spaces.CONTENT_PATH + "/#", SPACE_ID);
    }

    public static interface Spaces extends BaseColumns {
        public static final Uri CONTENT_URI = SpaceListContentProvider.CONTENT_URI;
        public static final String NAME = SpaceListDbHelper.SPACE_NAME;
        public static final String CONTENT_PATH = SpaceListContentProvider.SPACE_TABLE;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.onextent.oemap.provider.spaces";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.onextent.oemap.provider.spaces";
        public static final String[] PROJECTION_ALL = {_ID, NAME};
        public static final String SORT_ORDER_DEFAULT = NAME + " ASC";
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
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
        _db = new SpaceListDbHelper(getContext()).getWritableDatabase();
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
        builder.setTables(SPACE_TABLE);
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = Spaces.SORT_ORDER_DEFAULT;
        }
        switch (sURIMatcher.match(uri)) {
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
        if (sURIMatcher.match(uri) != SPACE_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        long id = _db.insert(SPACE_TABLE, null, values);
        if (id > 0) {
            // notify all listeners of changes and return itemUri:
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into " + SPACE_TABLE + ", uri: " + uri); // use another exception here!!!
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount = 0;
        switch (sURIMatcher.match(uri)) {
            case SPACE_LIST:
                delCount = _db.delete(SPACE_TABLE, selection, selectionArgs);
                break;
            case SPACE_ID:
                String idStr = uri.getLastPathSegment();
                String where = Spaces._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = _db.delete(SPACE_TABLE, where, selectionArgs);
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

