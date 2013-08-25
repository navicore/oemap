/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.android.util.OeLog;

import java.util.HashMap;
import java.util.Map;

public class KvHelper {

    private final Context _context;

    public KvHelper(Context context) { //for tests

        _context = context;
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
        values.put(KvProvider.Kv.KEY, key);
        values.put(KvProvider.Kv.VALUE, value);
        _context.getContentResolver().insert(KvProvider.CONTENT_URI, values);
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
        delete(key);
        insert(key, value);
    }

    public boolean delete(String key) {

        Cursor c = null;
        try {
            c = _context.getContentResolver().query(KvProvider.CONTENT_URI,
                    KvProvider.Kv.PROJECTION_ALL, KvProvider.Kv.KEY + "='" + key + "'", null, null);
            if (c.getCount() <= 0) return false;
            c.moveToFirst();
            //int i = c.getColumnIndex(KvProvider.Kv.KEY);
            //String k = c.getString(i);
            int i = c.getColumnIndex(KvProvider.Kv._ID);
            int id = c.getInt(i);

            _context.getContentResolver().delete(KvProvider.CONTENT_URI,
                    KvProvider.Kv._ID + "=" + id, null);

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
            return false;
        } finally {
            if (c != null) c.close();
        }
        return true;


    }

    public void deleteAll() {
        try {
            _context.getContentResolver().delete(KvProvider.CONTENT_URI, null, null);

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
        }
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
        String v = null;
        try {

        c = _context.getContentResolver().query(KvProvider.CONTENT_URI,
                KvProvider.Kv.PROJECTION_ALL, KvProvider.Kv.KEY + "='" + key + "'", null, null);
        if (c.getCount() > 0) {

        c.moveToFirst();
        int i = c.getColumnIndex(KvProvider.Kv.VALUE);
        v = c.getString(i);
        }
        } finally {

            if (c != null)
            c.close();
        }
        return v;
    }

    public Map<String, String> getAll() {
        Map<String, String> l = null;
        Cursor c = _context.getContentResolver().query(KvProvider.CONTENT_URI,
                KvProvider.Kv.PROJECTION_ALL, null, null, null);
        if (c.getCount() > 0) {
        while (c.moveToNext()) {

            int i = c.getColumnIndex(KvProvider.Kv.KEY);
            String k = c.getString(i);
            i = c.getColumnIndex(KvProvider.Kv.VALUE);
            String v = c.getString(i);
            if (l == null) {
                l = new HashMap<String, String>();
            }
            l.put(k,v);
        }
        }
        c.close();

        return l;
    }
}

