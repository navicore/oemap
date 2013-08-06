package com.onextent.oemap.presence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;

import com.onextent.android.util.OeLog;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PresenceDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 3;
    SQLiteDatabase _db;

    public PresenceDbHelper(Context context, String dbname) { //for tests
        super(context, dbname, null, DATABASE_VERSION);
        establishDb();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        createTables(sqLiteDatabase);
    }

    private void createTables(SQLiteDatabase sqLiteDatabase) {

        String qs = "CREATE TABLE presences (uid, spacename, data, PRIMARY KEY (uid, spacename));";
        sqLiteDatabase.execSQL(qs);
        qs = "CREATE TABLE spacenames (spacename, PRIMARY KEY (spacename));";  //todo: normal
        sqLiteDatabase.execSQL(qs);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS presences;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS spacenames;");
    }

    private void establishDb() {

        if (_db == null)
            _db = getWritableDatabase();
    }

    public void cleanup() {
        if (_db != null) {
            _db.close();
            _db = null;
        }
    }

    public void insertPresence(Presence presence) {
        ContentValues values = new ContentValues();
        values.put("uid", presence.getUID());
        values.put("spacename", presence.getSpaceName());
        values.put("data", presence.toString());
        _db.insertOrThrow("presences", null, values);
    }

    public void replaceSpacename(String n) {
        ContentValues values = new ContentValues();
        values.put("spacename", n);
        _db.replaceOrThrow("spacenames", null, values);
    }

    public void replacePresence(Presence presence) {
        ContentValues values = new ContentValues();
        values.put("uid", presence.getUID());
        values.put("spacename", presence.getSpaceName());
        values.put("data", presence.toString());
        _db.replaceOrThrow("presences", null, values);
    }

    public void deleteSpacename(String n) {
        _db.delete("spacenames", "spacename='" + n + "'", null);
    }

    public void deletePresence(Presence presence) {
        _db.delete("presences", "uid='" + presence.getUID() + "' AND spacename='" + presence.getSpaceName() + "'", null);
    }

    public void deletePresencesWithSpaceName(String spacename) {
        _db.delete("presences", "spacename='" + spacename + "'", null);
    }

    public Presence getPresence(String uid, String spacename) throws JSONException {
        Cursor c = null;
        Presence p = null;
        //String[] cols = {"_id", "uid", "spacename", "data"};
        String[] cols = {"uid", "spacename", "data"};
        try {

            c = _db.query(true, "presences", cols, "uid='" + uid + "' AND spacename='" + spacename + "'", null, null, null, null, null);
            int DATA_FLD = c.getColumnIndex("data");
            if (c.getCount() > 0) {
                c.moveToFirst();
                String json = c.getString(DATA_FLD);
                p = PresenceFactory.createPresence(json);
            }
        } catch (SQLException ex) {
            OeLog.e(ex.toString(), ex);
        } finally {
            if (c != null && !c.isClosed())
                c.close();
        }
        return p;
    }

    public Set<Presence> getAllPrecenses(String spacename) throws JSONException {
        Cursor c = null;
        Set<Presence> l = null;
        String[] cols = {"uid", "spacename", "data"};
        try {
            c = _db.query(true, "presences", cols, "spacename='" + spacename + "'", null, null, null, null, null);
            int DATA_FLD = c.getColumnIndex("data");
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; i++) {
                if (l == null)
                    l = new HashSet<Presence>();
                String json = c.getString(DATA_FLD);
                Presence p = PresenceFactory.createPresence(json);
                l.add(p);
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

    public Set<String> getAllSpacenames() throws JSONException {
        Cursor c = null;
        Set<String> l = null;
        String[] cols = {"spacename"};
        try {
            c = _db.query(true, "spacenames", cols, null, null, null, null, null, null);
            int DATA_FLD = c.getColumnIndex("spacename");
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; i++) {
                if (l == null)
                    l = new HashSet<String>();
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

