package com.onextent.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

@Deprecated
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

    public void insertInt(String key, int value) {

        insert(key, Integer.toString(value));
    }

    public void insertFloat(String key, float value) {

        insert(key, Float.toString(value));
    }

    public void insertDouble(String key, double value) {

        insert(key, Double.toString(value));
    }

    public void insertLong(String key, long value) {

        insert(key, Long.toString(value));
    }

    public void insertBoolean(String key, boolean value) {

        insert(key, Boolean.toString(value));
    }

    public void insert(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        _db.insertOrThrow("pairs", null, values);
    }

    public void replaceInt(String key, int value) {

        replace(key, Integer.toString(value));
    }

    public void replaceFloat(String key, float value) {

        replace(key, Float.toString(value));
    }

    public void replaceLong(String key, long value) {

        replace(key, Long.toString(value));
    }

    public void replaceDouble(String key, double value) {

        replace(key, Double.toString(value));
    }

    public void replaceBoolean(String key, boolean value) {

        replace(key, Boolean.toString(value));
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

    public float getFloat(String key, float defaultValue) {
        String value = get(key);
        if (value == null)
            return defaultValue;
        else
            return Float.valueOf(value);
    }

    public double getDouble(String key, double defaultValue) {
        String value = get(key);
        if (value == null)
            return defaultValue;
        else
            return Double.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null)
            return defaultValue;
        else
            return Integer.valueOf(value);
    }

    public long getLong(String key, long defaultValue) {
        String value = get(key);
        if (value == null)
            return defaultValue;
        else
            return Long.valueOf(value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null)
            return defaultValue;
        else
            return Boolean.valueOf(value);
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null)
            return defaultValue;
        else
            return value;

    }
    public String get(String key) {
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

    public Map<String, String> getAll() {
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

