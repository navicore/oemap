package com.onextent.oemap.provider;


import android.content.Context;
import android.database.Cursor;

import com.onextent.android.util.OeLog;

import java.util.ArrayList;
import java.util.List;

public class SpaceHelper {

    private static final String[] PROJECTION = SpaceProvider.Spaces.PROJECTION_ALL;

    private final Context _context;

    public SpaceHelper(Context context) {
        _context = context;
    }

    public List<String> getAllSpaceNames() {

        List<String> l = new ArrayList<String>();
        Cursor c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,
                SpaceProvider.Spaces.PROJECTION_ALL, null, null,
                SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
        int pos = c.getColumnIndex(SpaceProvider.Spaces.NAME);
        while (c.moveToNext()) {
            String n = c.getString(pos);
            l.add(n);
        }
        c.close();
        return l;
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
            c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,
                    PROJECTION, SpaceProvider.Spaces.NAME + "='" + n + "'", null, null);
            c.moveToFirst();
            int i = c.getColumnIndex(SpaceProvider.Spaces.NAME);
            String space = c.getString(i);
            i = c.getColumnIndex(SpaceProvider.Spaces._ID);
            int id = c.getInt(i);

            _context.getContentResolver().delete(SpaceProvider.CONTENT_URI,
                    SpaceProvider.Spaces._ID + "=" + id, null);

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
            return false;
        } finally {
            if (c != null) c.close();
        }
        return true;
    }
}

