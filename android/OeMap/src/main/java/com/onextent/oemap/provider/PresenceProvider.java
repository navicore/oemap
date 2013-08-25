/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

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

public class PresenceProvider extends ContentProvider {

    private static final String AUTHORITY   = "com.onextent.oemap.provider.PresenceProvider";
    public  static final int    PRESENCE_LIST  = 1;
    public  static final int    PRESENCE_ID    = 2;
    public  static final Uri    CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + IPresence.CONTENT_PATH);

    private static final UriMatcher _uriMatcher;

    private SQLiteDatabase _db = null;

    static {
        _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        _uriMatcher.addURI(AUTHORITY, IPresence.CONTENT_PATH, PRESENCE_LIST);
        _uriMatcher.addURI(AUTHORITY, IPresence.CONTENT_PATH + "/#", PRESENCE_ID);
    }

    public static interface IPresence extends BaseColumns {
        static String UID               = PresenceDbHelper.PRESENCE_UID;
        static String SPACE             = PresenceDbHelper.PRESENCE_SPACE;
        static String DATA              = PresenceDbHelper.PRESENCE_DATA;
        static String CONTENT_PATH      = PresenceDbHelper.PRESENCE_TABLE;
        static String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE + "/com.onextent.oemap.provider.presence";
        static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/com.onextent.oemap.provider.presence";
        static String[] PROJECTION_ALL  = {_ID, UID, SPACE, DATA};
        static String SORT_ORDER_DEFAULT = UID + " ASC";
    }

    @Override
    public String getType(Uri uri) {
        switch (_uriMatcher.match(uri)) {
            case PRESENCE_LIST:
                return IPresence.CONTENT_TYPE;
            case PRESENCE_ID:
                return IPresence.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        _db = new PresenceDbHelper(getContext()).getWritableDatabase();
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
        builder.setTables(IPresence.CONTENT_PATH);
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = IPresence.SORT_ORDER_DEFAULT;
        }
        switch (_uriMatcher.match(uri)) {
            case PRESENCE_LIST:
                // all nice and well
                break;
            case PRESENCE_ID:
                // limit query to one row at most:
                builder.appendWhere(IPresence._ID + " = " + uri.getLastPathSegment());
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
        if (_uriMatcher.match(uri) != PRESENCE_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
        long id = _db.insert(IPresence.CONTENT_PATH, null, values);
        if (id > 0) {
            // notify all listeners of changes and return itemUri:
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into " + IPresence.CONTENT_PATH + ", uri: " + uri); // use another exception here!!!
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount = 0;
        switch (_uriMatcher.match(uri)) {
            case PRESENCE_LIST:
                delCount = _db.delete(IPresence.CONTENT_PATH, selection, selectionArgs);
                break;
            case PRESENCE_ID:
                String idStr = uri.getLastPathSegment();
                String where = IPresence._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = _db.delete(IPresence.CONTENT_PATH, where, selectionArgs);
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

