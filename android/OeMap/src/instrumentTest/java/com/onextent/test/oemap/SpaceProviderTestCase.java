package com.onextent.test.oemap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.provider.SpaceProvider;

import org.json.JSONException;

import android.net.Uri;

public class SpaceProviderTestCase extends AndroidTestCase {

    private static final String[] PROJECTION = SpaceProvider.Spaces.PROJECTION_ALL;
    private static final String[] SPACENAMES = {"one", "two", "three"};

    private ContentResolver _resolver;

    @Override
    protected void setUp() {

        _resolver = getContext().getContentResolver();

        _resolver.delete(SpaceProvider.CONTENT_URI, null, null);
        for (String s : SPACENAMES) {

            ContentValues values = new ContentValues();
            values.put(SpaceProvider.Spaces.NAME, s);
            _resolver.insert(SpaceProvider.CONTENT_URI, values);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        //noop
        super.tearDown();
    }

    public void testQueryAll() {

        final String TEST_VAL = "won!";

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces.NAME, TEST_VAL);
        Uri r = _resolver.insert(SpaceProvider.CONTENT_URI, values);
        OeLog.d("insert uri: " + r);

        Cursor c = _resolver.query(SpaceProvider.CONTENT_URI, null, null, null, null);
        int SPACENAME_IDX =  c.getColumnIndex(SpaceProvider.Spaces.NAME);
        while (c.moveToNext()) {

            String space = c.getString(SPACENAME_IDX);
            OeLog.d("found " + space);
        }
        c.close();
    }

    public void testQueryOneWithSelectionArgs() {

        final String TEST_VAL = "won";

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces.NAME, TEST_VAL);
        _resolver.insert(SpaceProvider.CONTENT_URI, values);

        String[] args = {TEST_VAL};
        Cursor c = _resolver.query(SpaceProvider.CONTENT_URI,
                PROJECTION, SpaceProvider.Spaces.NAME + "=?", args, null);
        assertEquals(c.getCount(), 1);
        c.moveToFirst();
        int i = c.getColumnIndex(SpaceProvider.Spaces.NAME);
        assertTrue(i >= 0);
        String space = c.getString(i);

        assertEquals(space, TEST_VAL);
        c.close();
    }

    public void testUriQuery() {

        final String TEST_VAL = "ahh";

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces.NAME, TEST_VAL);
        Uri r = _resolver.insert(SpaceProvider.CONTENT_URI, values);

        Cursor c = _resolver.query(r, null, null, null, null);
        assertEquals(c.getCount(), 1);
        c.moveToFirst();
        int i = c.getColumnIndex(SpaceProvider.Spaces.NAME);
        assertTrue(i >= 0);
        String space = c.getString(i);

        assertEquals(space, TEST_VAL);
        c.close();
    }

    public void testDelete() throws JSONException {

        final String TEST_VAL = "one";

        Cursor c = _resolver.query(SpaceProvider.CONTENT_URI,
                PROJECTION, SpaceProvider.Spaces.NAME + "='" + TEST_VAL + "'", null, null);
        c.moveToFirst();
        int i = c.getColumnIndex(SpaceProvider.Spaces.NAME);
        assertTrue(i >= 0);
        String space = c.getString(i);
        assertEquals(space, TEST_VAL);
        i = c.getColumnIndex(SpaceProvider.Spaces._ID);
        assertTrue(i >= 0);
        int id = c.getInt(i);
        c.close();

        ContentValues values = new ContentValues();
        values.put(SpaceProvider.Spaces.NAME, TEST_VAL);
        _resolver.delete(SpaceProvider.CONTENT_URI, SpaceProvider.Spaces._ID + "=" + id, null);

        c = _resolver.query(SpaceProvider.CONTENT_URI,
                PROJECTION, SpaceProvider.Spaces.NAME + "='" + TEST_VAL + "'", null, null);
        assertEquals(c.getCount(), 0);
        c.close();
    }
}

