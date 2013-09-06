/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.provider;


import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;

import com.onextent.android.util.OeLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SpaceHelper {

    public static final int PRESENCE_PARAM_DEFAULT_MAX_COUNT = 30;
    public static final int PRESENCE_PARAM_DEFAULT_DIST = 1609 * 120;
    private static final String[] PROJECTION = SpaceProvider.Spaces.PROJECTION_ALL;

    private final Context _context;

    public SpaceHelper(Context context) {
        _context = context;
    }

    public void insert(Space space) {

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces._ID, space.getName());
        values.put(SpaceProvider.Spaces.SIZE_IN_METERS, space.getNMeters());
        values.put(SpaceProvider.Spaces.SIZE_IN_POINTS, space.getMaxPoints());
        if (space.getLease() != null) {

            long l = space.getLease().getTime();
            values.put(SpaceProvider.Spaces.LEASE, l);
        }
        Uri r = _context.getContentResolver().insert(SpaceProvider.CONTENT_URI, values);
    }
    public void insert(String space, Date lease) throws UnsupportedEncodingException {

        Space s = new Space(lease, space, PRESENCE_PARAM_DEFAULT_DIST, PRESENCE_PARAM_DEFAULT_MAX_COUNT);
        insert(s);
    }
    public void insert(String space) throws UnsupportedEncodingException {

        Space s = new Space(null, space, PRESENCE_PARAM_DEFAULT_DIST, PRESENCE_PARAM_DEFAULT_MAX_COUNT);
        insert(s);
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

    public void setLease(String n, Date d) throws UnsupportedEncodingException {

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
    public static class Space {
        private Date _lease;
        private final String _name;
        private int _nmeters, _max;
        public Space(Date l, String n, int dist, int max) throws UnsupportedEncodingException {
            setLease(l);
            _name = URLEncoder.encode(n, "UTF8");
            _nmeters = dist;
            if (max <= 0)
                _max = PRESENCE_PARAM_DEFAULT_MAX_COUNT;
            else
                _max = max;
        }

        public Date getLease() {
            return _lease;
        }
        public void setLease(Date l) {
            if (l != null)
                _lease = l;
            else
                _lease = new Date(Long.MAX_VALUE);
        }
        public String getName() {
            try {
                return URLDecoder.decode(_name, "UTF8");
            } catch (UnsupportedEncodingException e) {
                OeLog.e(_name);
                return _name;
            }
        }
        public int getNMeters() {
            return _nmeters;
        }
        //public void setNMeters(int n) {
        //    _nmeters = n;
        //}
        public int getMaxPoints() {
            return _max;
        }
        public void setMaxPoints(int max) {
            _max = max;
        }
    }

    public Space getSpace(String n) {

        Cursor c = null;
        try {

            c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,SpaceProvider.Spaces.PROJECTION_ALL,
                    SpaceProvider.Spaces._ID + "='" + n + "'", null, SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
            if (c.getCount() <= 0) return null;
            c.moveToFirst();

            int lpos = c.getColumnIndex(SpaceProvider.Spaces.LEASE);
            long l = c.getLong(lpos);
            Date d = new Date(l);

            int metpos = c.getColumnIndex(SpaceProvider.Spaces.SIZE_IN_METERS);
            int met = c.getInt(metpos);

            int maxpos = c.getColumnIndex(SpaceProvider.Spaces.SIZE_IN_POINTS);
            int max = c.getInt(maxpos);

            Space space = new Space(d, n, met, max);

            return space;

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

