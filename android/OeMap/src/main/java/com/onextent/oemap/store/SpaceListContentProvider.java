package com.onextent.oemap.store;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.onextent.android.util.ListDbHelper;
import com.onextent.oemap.R;

public class SpaceListContentProvider extends ContentProvider {

    public static final int ITEMS = 1;
    public static final int ITEM = 2;
    private static final String AUTHORITY = "com.onextent.oemap.store.SpaceListContentProvider";
    private static final String SPACE_TABLE = "items";
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SPACE_TABLE);

    static {
        sURIMatcher.addURI(AUTHORITY, SPACE_TABLE, ITEMS);
        sURIMatcher.addURI(AUTHORITY, SPACE_TABLE + "/#", ITEMS);
    }

    private ListDbHelper _dbHelper;

    @Override
    public boolean onCreate() {
        _dbHelper = new ListDbHelper(this.getContext(), getContext().getString(R.string.oemap_spacename_store));
        return false;
    }

    public ListDbHelper getDbHelper() {
        return _dbHelper;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = _dbHelper.getWritableDatabase();

        long id = 0;
        switch (uriType) {
            case ITEMS:
                id = sqlDB.insert(ListDbHelper.ITEMS_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(SPACE_TABLE + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}

