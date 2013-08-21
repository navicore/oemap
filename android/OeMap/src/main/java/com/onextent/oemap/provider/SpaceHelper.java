package com.onextent.oemap.provider;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.onextent.android.util.OeLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SpaceHelper {

    private static final String[] PROJECTION = SpaceProvider.Spaces.PROJECTION_ALL;

    private final Context _context;

    public SpaceHelper(Context context) {
        _context = context;
    }

    public void insert(String space, Date lease) {

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces._ID, space);
        if (lease != null) {

            long l = lease.getTime();
            values.put(SpaceProvider.Spaces.LEASE, l);
        }
        Uri r = _context.getContentResolver().insert(SpaceProvider.CONTENT_URI, values);
    }

    public List<String> getAllSpaceNames() {

        List<String> l = new ArrayList<String>();
        Cursor c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,
                SpaceProvider.Spaces.PROJECTION_ALL, null, null,
                SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
        int pos = c.getColumnIndex(SpaceProvider.Spaces._ID);
        while (c.moveToNext()) {
            String n = c.getString(pos);
            l.add(n);
        }
        c.close();
        return l;
    }

    public void setLease(String n, Date d) {

        deleteSpacename(n);
        insert(n, d);
    }

    public Date getLease(String n) {

        Cursor c = null;
        try {

            c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,SpaceProvider.Spaces.PROJECTION_ALL,
                    SpaceProvider.Spaces._ID + "='" + n + "'", null, SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
            if (c.getCount() <= 0) return null;
            c.moveToFirst();
            int pos = c.getColumnIndex(SpaceProvider.Spaces.LEASE);
            long l = c.getLong(pos);
            if (l <= 0) return null;
            Date d = new Date(l);
            return d;

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
            return null;
        } finally {
            if (c != null) c.close();
        }
    }

    public boolean hasSpaceNames() {

        Cursor c = null;
        try {
            c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,
                    PROJECTION, null, null, null);
            if (c.getCount() > 0) return true;
            else return false;

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
            return false;
        } finally {
            if (c != null) c.close();
        }
    }

    public boolean deleteSpacename(String n) {

        Cursor c = null;
        try {

            _context.getContentResolver().delete(SpaceProvider.CONTENT_URI,
                    SpaceProvider.Spaces._ID + "='" + n + "'", null);

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
            return false;
        } finally {
            if (c != null) c.close();
        }
        return true;
    }
}

