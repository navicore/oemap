package com.onextent.test.android;

import android.database.SQLException;
import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.util.KeyValueDbHelper;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceDbHelper;
import com.onextent.oemap.presence.PresenceFactory;

import org.json.JSONException;

import java.util.Map;
import java.util.Set;

public class KeyValueDbHelperTestCase extends AndroidTestCase {

    private KeyValueDbHelper _dbHelper;

    @Override
    protected void setUp() {
        _dbHelper = new KeyValueDbHelper(getContext(), "testKeyValueDb");
    }

    @Override
    protected void tearDown() throws Exception {
        _dbHelper.close();
        super.tearDown();
    }

    public void testInsert() {

        _dbHelper.insert("one", "1");
    }

    public void testBadInsert() {

        _dbHelper.insert("two", "2");

        boolean success = false;
        try {
            _dbHelper.insert("two", "2");
        } catch (SQLException ex) {
            success = true;
        }
        assertTrue("double insert", success);
    }

    public void testReplace() {

        _dbHelper.replace("three", "3");

        boolean success = false;
        try {
            _dbHelper.insert("three", "3");
        } catch (SQLException ex) {
            success = true;
        }
        assertTrue("should have been a dupe", success);

        success = false;
        _dbHelper.replace("three", "3"); //should work now too
    }

    public void testGet() throws JSONException {

        String v = _dbHelper.get("four");
        assertNull("should not have found this value", v);

        _dbHelper.insert("four", "4");

        v = _dbHelper.get("four");
        assertNotNull("should have found this value");
        assertEquals("should have matching '4'", "4", v);

        _dbHelper.replace("four", "44");
        v = _dbHelper.get("four");
        assertNotNull("should have found this value");
        assertEquals("should have matching '44'", "44", v);
    }

    public void testGetAll() throws JSONException {

        _dbHelper.deleteAll();
        Map<String, String> l = _dbHelper.getAll();
        assertNull("should not have found these", l);

        _dbHelper.replace("five", "5");
        _dbHelper.replace("six", "6");
        _dbHelper.replace("seven", "7");

        l = _dbHelper.getAll();
        assertNotNull("should have found these", l);
        assertEquals("wrong number of getAll(map) results: " + l, 3, l.size());
    }

    public void testDelete() throws JSONException {

        _dbHelper.replace("nine", "9");

        String v = _dbHelper.get("nine");
        assertNotNull("should have found this", v);
        assertEquals("should have matching '9': " + v, "9", v);

        _dbHelper.delete("nine");
        v = _dbHelper.get("nine");
        assertNull("should not have found this", v);
    }
}

