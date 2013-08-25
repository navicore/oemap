/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.test.android;

import android.database.SQLException;
import android.test.AndroidTestCase;

import com.onextent.oemap.provider.KvHelper;

import org.json.JSONException;

import java.util.Map;

public class KvHelperTestCase extends AndroidTestCase {

    KvHelper _dbHelper;

    @Override
    protected void setUp() {
        _dbHelper = new KvHelper(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
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
        assertNull("should not have found these: " + l, l);

        _dbHelper.replace("five", "5");
        _dbHelper.replace("six", "6");
        _dbHelper.replace("seven", "7");

        l = _dbHelper.getAll();
        assertNotNull("should have found these", l);
        assertEquals("wrong number of getAll(map) results: " + l, 3, l.size());
    }

    public void testBoolean() throws JSONException {

        _dbHelper.insertBoolean("true", true);
        _dbHelper.replaceBoolean("false", false);
        assertTrue("wrong", _dbHelper.getBoolean("true", false));
        assertFalse("wrong", _dbHelper.getBoolean("false", true));
    }

    public void testFloat() throws JSONException {

        _dbHelper.insertFloat("101", 101);
        _dbHelper.replaceFloat("102", 102);
        assertEquals("wrong", _dbHelper.getFloat("101", 0), (float) 101.0);
        assertFalse("wrong", _dbHelper.getFloat("102", 0) == 103);
        assertEquals("wrong", _dbHelper.getFloat("102", 0), (float) 102);
    }

    public void testDouble() throws JSONException {

        _dbHelper.insertDouble("10.1", 10.1);
        _dbHelper.replaceDouble("10.2", 10.2);
        assertEquals("wrong", _dbHelper.getDouble("10.1", 0), 10.1);
        assertFalse("wrong", _dbHelper.getDouble("10.2", 0) == 10.3);
        assertEquals("wrong", _dbHelper.getDouble("10.2", 0), 10.2);
    }

    public void testDelete() throws JSONException {

        _dbHelper.replace("nine", "9");

        String v = _dbHelper.get("nine");
        assertNotNull("should have found this", v);
        assertEquals("should have matching '9': " + v, "9", v);

        _dbHelper.delete("nine");
        v = _dbHelper.get("nine");
        assertNull("should not have found this", v);
        v = _dbHelper.get("nine", "101010");
        assertEquals("should have matched default", "101010", v);
    }
}

