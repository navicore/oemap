package com.onextent.oemap.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceFactory;

import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class PresenceHelper {

    private final Context _context;

    public PresenceHelper(Context context) {
        _context = context;
    }

    public void insertPresence(Presence presence) {
        ContentValues values = new ContentValues();
        values.put(PresenceProvider.IPresence.UID, presence.getUID());
        values.put(PresenceProvider.IPresence.SPACE, presence.getSpaceName());
        values.put(PresenceProvider.IPresence.DATA, presence.toString());
        _context.getContentResolver().insert(PresenceProvider.CONTENT_URI, values);
    }

    public void replacePresence(Presence presence) {
        deletePresence(presence);
        insertPresence(presence);
    }

    public void deletePresence(Presence presence) {
        _context.getContentResolver().delete(PresenceProvider.CONTENT_URI,
                    PresenceProvider.IPresence.UID + "='" + presence.getUID() + "'", null);
    }

    public void deletePresencesWithSpaceNameNotMine(String spacename) {
        String me = OeMapActivity.id(_context);
        _context.getContentResolver().delete(PresenceProvider.CONTENT_URI,
                PresenceProvider.IPresence.SPACE + "='" + spacename +
                        "' AND " + PresenceProvider.IPresence.UID + " != '" + me + "'", null);
    }

    public void deletePresencesWithSpaceName(String spacename) {
        _context.getContentResolver().delete(PresenceProvider.CONTENT_URI,
                PresenceProvider.IPresence.SPACE + "='" + spacename + "'", null);
    }

    public Presence getPresence(String uid, String spacename) throws JSONException {

        Cursor c = null;
        Presence p = null;
        try {

        c = _context.getContentResolver().query(PresenceProvider.CONTENT_URI,
                PresenceProvider.IPresence.PROJECTION_ALL,
                PresenceProvider.IPresence.UID + "='" + uid + "' AND " +
                        PresenceProvider.IPresence.SPACE + "='" + spacename + "'", null, null);
            if (c.getCount() > 0) {
                int col = c.getColumnIndex(PresenceProvider.IPresence.DATA);
                c.moveToFirst();
                String json = c.getString(col);
                p = PresenceFactory.createPresence(json);
            }
        } finally {
            if (c != null) c.close();
        }
        return p;
    }

    public Set<Presence> getAllPrecenses(String spacename) throws JSONException {
        Cursor c = null;
        Set<Presence> l = null;
        try {

            c = _context.getContentResolver().query(PresenceProvider.CONTENT_URI,
                    PresenceProvider.IPresence.PROJECTION_ALL,
                    PresenceProvider.IPresence.SPACE + "='" + spacename + "'", null, null);
            if (c.getCount() > 0) {
                int col = c.getColumnIndex(PresenceProvider.IPresence.DATA);
                while (c.moveToNext()) {

                    if (l == null)
                        l = new HashSet<Presence>();
                    String json = c.getString(col);
                    Presence p = PresenceFactory.createPresence(json);
                    l.add(p);
                }
            }
        } finally {
            if (c != null) c.close();
        }
        return l;
    }
}

