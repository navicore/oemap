package com.onextent.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceFactory;

import org.json.JSONException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class KeyValueDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 1;
    SQLiteDatabase _db;

    public KeyValueDbHelper(Context context, String dbname) { //for tests
        super(context, dbname, null, DATABASE_VERSION);
        establishDb();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        createTables(sqLiteDatabase);
    }

    private void createTables(SQLiteDatabase sqLiteDatabase) {

        String qs = "CREATE TABLE pairs (key, value, PRIMARY KEY (key));";
        sqLiteDatabase.execSQL(qs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS pairs;");
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

    public void insert(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        _db.insertOrThrow("pairs", null, values);
    }

    public void replace(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        _db.replaceOrThrow("pairs", null, values);
    }

    public void delete(String key) {
        _db.delete("pairs", "key='" + key + "'", null);
    }

    public void deleteAll() {
        _db.delete("pairs", null, null);
    }

    public String get(String key) throws JSONException {
        Cursor c = null;
        String value = null;

        String[] cols = {"value"};
        try {

            c = _db.query(true, "pairs", cols, "key='" + key + "'", null, null, null, null, null);
            int DATA_FLD = c.getColumnIndex("value");
            if (c.getCount() > 0) {
                c.moveToFirst();
                value = c.getString(DATA_FLD);
            }
        } catch (SQLException ex) {
            OeLog.e(ex.toString(), ex);
        } finally {
            if (c != null && !c.isClosed())
                c.close();
        }
        return value;
    }

    public Map<String, String> getAll() throws JSONException {
        Cursor c = null;
        Map<String, String> l = null;
        String[] cols = {"key", "value"};
        try {
            c = _db.query(true, "pairs", cols, null, null, null, null, null, null);
            int KEY_FLD = c.getColumnIndex("key");
            int VAL_FLD = c.getColumnIndex("value");
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; i++) {
                if (l == null)
                    l = new HashMap<String, String>();
                String key = c.getString(KEY_FLD);
                String val = c.getString(VAL_FLD);
                l.put(key, val);
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

