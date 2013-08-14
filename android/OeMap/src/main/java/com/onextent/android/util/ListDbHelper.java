package com.onextent.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class ListDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 1;
    SQLiteDatabase _db;
    public static final String ITEMS_TABLE = "items"; //table name
    public static final String ITEM = "item"; //table name

    public ListDbHelper(Context context, String dbname) { //for tests
        super(context, dbname, null, DATABASE_VERSION);
        establishDb();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        createTables(sqLiteDatabase);
    }

    private void createTables(SQLiteDatabase sqLiteDatabase) {

        String qs = "CREATE TABLE " + ITEMS_TABLE + " (item, PRIMARY KEY (" + ITEM + "));";
        sqLiteDatabase.execSQL(qs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE + ";");
    }

    private void establishDb() {

        if (_db == null)
            _db = getWritableDatabase();
    }

    @Override
    public void close() {
        if (_db != null) {
            //does android do this anyway?
            _db.close();
            _db = null;
        }
        super.close();
    }

    public void insert(String item) {
        ContentValues values = new ContentValues();
        values.put("item", item);
        _db.insertOrThrow(ITEMS_TABLE, null, values);
    }

    public void replace(String item) {
        ContentValues values = new ContentValues();
        values.put("item", item);
        _db.replaceOrThrow(ITEMS_TABLE, null, values);
    }

    public void deleteAll() {
        _db.delete(ITEMS_TABLE, null, null);
    }

    public List<String> getAll() throws JSONException {
        Cursor c = null;
        List<String> l = null;
        String[] cols = {"item"};
        try {
            c = _db.query(true, ITEMS_TABLE, cols, null, null, null, null, null, null);
            int DATA_FLD = c.getColumnIndex("item");
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; i++) {
                if (l == null)
                    l = new ArrayList<String>();
                String n = c.getString(DATA_FLD);
                l.add(n);
                c.moveToNext();
            }
        } catch (SQLException ex) {
            OeLog.e(ex.toString(), ex);
        } finally {
            if (c != null && !c.isClosed())
                c.close();
        }
        return l;
    }
}

