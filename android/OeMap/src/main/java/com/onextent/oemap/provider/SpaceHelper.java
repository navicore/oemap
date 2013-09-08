/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.provider;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.onextent.android.util.OeLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpaceHelper extends BaseProviderHelper {

    public static final int PRESENCE_PARAM_DEFAULT_MAX_COUNT = 30;
    public static final int PRESENCE_PARAM_DEFAULT_DIST = 1609 * 120;
    private static final String[] PROJECTION = SpaceProvider.Spaces.PROJECTION_ALL;

    private final Context _context;

    public SpaceHelper(Context context) {
        _context = context;
    }

    public void insert(Space space) {

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces._ID, space._id);  //use encoded inner value
        values.put(SpaceProvider.Spaces.NAME, space._name); //use encoded inner value
        values.put(SpaceProvider.Spaces.SIZE_IN_METERS, space.getNMeters());
        values.put(SpaceProvider.Spaces.SIZE_IN_POINTS, space.getMaxPoints());
        if (space.getLease() != null) {

            long l = space.getLease().getTime();
            values.put(SpaceProvider.Spaces.LEASE, l);
        }
        Uri r = _context.getContentResolver().insert(SpaceProvider.CONTENT_URI, values);
    }

    public List<String> getAllSpaceIds() {

        String[] proj = {SpaceProvider.Spaces._ID};
        List<String> l = new ArrayList<String>();
        Cursor c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,
                proj, null, null,
                SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
        int pos = c.getColumnIndex(SpaceProvider.Spaces._ID);
        while (c.moveToNext()) {
            String n = c.getString(pos);
            l.add(decode(n));
        }
        c.close();
        return l;
    }

    public List<String> getAllSpaceNames() {

        String[] proj = {SpaceProvider.Spaces.NAME};
        List<String> l = new ArrayList<String>();
        Cursor c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,
                proj, null, null,
                SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
        int pos = c.getColumnIndex(SpaceProvider.Spaces.NAME);
        while (c.moveToNext()) {
            String n = c.getString(pos);
            l.add(decode(n));
        }
        c.close();
        return l;
    }

    public Date getLease(String n) {

        String safe_id = encode(n);
        Cursor c = null;
        try {

            c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,SpaceProvider.Spaces.PROJECTION_ALL,
                    SpaceProvider.Spaces._ID + "='" + safe_id + "'", null, SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
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
        Date _lease;
        String _name, _id;
        int _nmeters, _max, _type;

        public Space(Date l, String id, String n, int dist, int max, int type) {
            if (n == null) throw new NullPointerException("name is null");
            _name = encode(n);
            if (id == null) {
                _id = _name;
            } else {
                _id = encode(id);
            }
            _type = type;
            _nmeters = dist;
            if (max <= 0)
                _max = PRESENCE_PARAM_DEFAULT_MAX_COUNT;
            else
                _max = max;
            setLease(l);
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

        public void setType(int t) {
            _type = t;
        }

        public int getType() {
            return _type;
        }

        public String getId() {
            return decode(_id);
        }
        public String getName() {
            return decode(_name);
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

    public Space getSpace(String id) {

        String safe_id = encode(id);
        Cursor c = null;
        try {

            c = _context.getContentResolver().query(SpaceProvider.CONTENT_URI,SpaceProvider.Spaces.PROJECTION_ALL,
                    SpaceProvider.Spaces._ID + "='" + safe_id + "'", null, SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
            if (c.getCount() <= 0) return null;
            c.moveToFirst();

            int namepos = c.getColumnIndex(SpaceProvider.Spaces.NAME);
            String name = decode(c.getString(namepos));

            int lpos = c.getColumnIndex(SpaceProvider.Spaces.LEASE);
            long l = c.getLong(lpos);
            Date d = new Date(l);

            int metpos = c.getColumnIndex(SpaceProvider.Spaces.SIZE_IN_METERS);
            int met = c.getInt(metpos);

            int maxpos = c.getColumnIndex(SpaceProvider.Spaces.SIZE_IN_POINTS);
            int max = c.getInt(maxpos);

            int tpos = c.getColumnIndex(SpaceProvider.Spaces.TYPE);
            int type = c.getInt(tpos);

            Space space = new Space(d, id, name, met, max, type);

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

        String safe_id = encode(n);
        Cursor c = null;
        try {

            _context.getContentResolver().delete(SpaceProvider.CONTENT_URI,
                    SpaceProvider.Spaces._ID + "='" + safe_id + "'", null);

        } catch (Exception ex) {
            OeLog.w(ex.toString(), ex);
            return false;
        } finally {
            if (c != null) c.close();
        }
        return true;
    }
}

