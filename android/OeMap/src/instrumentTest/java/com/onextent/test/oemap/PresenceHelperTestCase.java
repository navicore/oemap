package com.onextent.test.oemap;

import android.database.SQLException;
import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceFactory;
import com.onextent.oemap.provider.PresenceHelper;

import org.json.JSONException;

import java.util.Set;

public class PresenceHelperTestCase extends AndroidTestCase {

    private PresenceHelper _dbHelper;

    @Override
    protected void setUp() {
        _dbHelper = new PresenceHelper(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInsert() {

        Presence p = PresenceFactory.createPresence("myUid1", new LatLng(30.1, 40.1), "my label", "my snippit", "my map");

        _dbHelper.insertPresence(p);
    }

    public void testBadInsert() {

        Presence p = PresenceFactory.createPresence("myUid2", new LatLng(30.1, 40.1), "my label", "my snippit", "my map");

        _dbHelper.insertPresence(p);

        boolean success = false;
        try {
            _dbHelper.insertPresence(p); //should fail as dupe
        } catch (SQLException ex) {
            success = true;
        }
        assertTrue("double insert", success);
    }

    public void testReplace() {

        Presence p = PresenceFactory.createPresence("myUid3", new LatLng(30.1, 40.1), "my label", "my snippit", "my map");
        _dbHelper.replacePresence(p);

        boolean success = false;
        try {
            _dbHelper.insertPresence(p);
        } catch (SQLException ex) {
            success = true;
        }
        assertTrue("should have been a dupe", success);

        success = false;
        _dbHelper.replacePresence(p); //should work now too
    }

    public void testGet() throws JSONException {

        Presence p = _dbHelper.getPresence("myUid4", "my map");
        assertNull("should not have found this presence", p);

        Presence p1 = PresenceFactory.createPresence("myUid4", new LatLng(30.1, 40.1), "my label", "my snippit", "my map");
        _dbHelper.insertPresence(p1);

        p = _dbHelper.getPresence("myUid4", "my map");
        assertNotNull("should have found this presence", p);
        assertEquals("should have matching 'my label': " + p.toString(), p.getLabel(), "my label");
        assertEquals("should have matching 'my snippit': " + p.toString(), p.getSnippet(), "my snippit");

        Presence p2 = PresenceFactory.createPresence("myUid4", new LatLng(30.1, 40.1), "my new label", "my new snippit", "my map");
        _dbHelper.replacePresence(p2);
        p = _dbHelper.getPresence("myUid4", "my map");
        assertNotNull("should have found this presence", p);
        assertEquals("should have matching 'my new label': " + p.toString(), p.getLabel(), "my new label");
        assertEquals("should have matching 'my new snippit': " + p.toString(), p.getSnippet(), "my new snippit");
    }

    public void testGetAll() throws JSONException {

        Presence p = PresenceFactory.createPresence("myUid1", new LatLng(30.1, 40.1), "my label", "my snippit", "my new map");
        _dbHelper.replacePresence(p);
        p = PresenceFactory.createPresence("myUid2", new LatLng(30.1, 40.1), "my label", "my snippit", "my new map");
        _dbHelper.replacePresence(p);
        p = PresenceFactory.createPresence("myUid3", new LatLng(30.1, 40.1), "my label", "my snippit", "my new map");
        _dbHelper.replacePresence(p);
        p = PresenceFactory.createPresence("myUid4", new LatLng(30.1, 40.1), "my label", "my snippit", "my new map");
        _dbHelper.replacePresence(p);

        Set<Presence> l = _dbHelper.getAllPrecenses("my new map");
        assertNotNull("should have found these", l);
        assertEquals("wrong number of getAll(map) results: " + l, 4, l.size());
    }

    public void testDelete() throws JSONException {

        Presence p1 = PresenceFactory.createPresence("myUid9", new LatLng(30.1, 40.1), "my newest label", "my snippit", "my newest map");
        _dbHelper.replacePresence(p1);

        Presence p = _dbHelper.getPresence("myUid9", "my newest map");
        assertNotNull("should have found this", p);
        assertEquals("should have matching 'my newest label': " + p.toString(), p.getLabel(), "my newest label");

        _dbHelper.deletePresence(p1);
        p = _dbHelper.getPresence("myUid9", "my newest map");
        assertNull("should not have found this", p);
    }
}

