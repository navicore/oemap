package com.onextent.test.oemap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.provider.SpaceListContentProvider;

import org.json.JSONException;

public class SpaceListContentProviderTestCase extends AndroidTestCase {

    private static final String[] PROJECTION = SpaceListContentProvider.Spaces.PROJECTION_ALL;
    private static final String[] SPACENAMES = {"one", "two", "three"};

    private ContentResolver _resolver;

    @Override
    protected void setUp() {

        _resolver = getContext().getContentResolver();

        _resolver.delete(SpaceListContentProvider.CONTENT_URI, null, null);
        for (String s : SPACENAMES) {

            ContentValues values = new ContentValues();
            values.put(SpaceListContentProvider.Spaces.NAME, s);
            _resolver.insert(SpaceListContentProvider.CONTENT_URI, values);
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
        values.put(SpaceListContentProvider.Spaces.NAME, TEST_VAL);
        _resolver.insert(SpaceListContentProvider.CONTENT_URI, values);

        Cursor c = _resolver.query(SpaceListContentProvider.CONTENT_URI, null, null, null, null);
        int SPACENAME_IDX =  c.getColumnIndex(SpaceListContentProvider.Spaces.NAME);
        while (c.moveToNext()) {

            String space = c.getString(SPACENAME_IDX);
            OeLog.d("found " + space);
        }
    }

    public void testQueryOne() {

        final String TEST_VAL = "won";

        ContentValues values = new ContentValues();
        values.put(SpaceListContentProvider.Spaces.NAME, TEST_VAL);
        _resolver.insert(SpaceListContentProvider.CONTENT_URI, values);

        Cursor c = _resolver.query(SpaceListContentProvider.CONTENT_URI, PROJECTION, SpaceListContentProvider.Spaces.NAME + "='" + TEST_VAL + "'", null, null);
        assertEquals(c.getCount(), 1);
        c.moveToFirst();
        int i = c.getColumnIndex(SpaceListContentProvider.Spaces.NAME);
        assertTrue(i >= 0);
        String space = c.getString(i);

        assertEquals(space, TEST_VAL);
    }

    public void testDelete() throws JSONException {

        final String TEST_VAL = "one";

        Cursor c = _resolver.query(SpaceListContentProvider.CONTENT_URI, PROJECTION, SpaceListContentProvider.Spaces.NAME + "='" + TEST_VAL + "'", null, null);
        c.moveToFirst();
        int i = c.getColumnIndex(SpaceListContentProvider.Spaces.NAME);
        assertTrue(i >= 0);
        String space = c.getString(i);
        i = c.getColumnIndex(SpaceListContentProvider.Spaces._ID);
        assertTrue(i >= 0);
        int id = c.getInt(i);

        ContentValues values = new ContentValues();
        values.put(SpaceListContentProvider.Spaces.NAME, TEST_VAL);
        _resolver.delete(SpaceListContentProvider.CONTENT_URI, SpaceListContentProvider.Spaces._ID + "=" + id, null);

        c = _resolver.query(SpaceListContentProvider.CONTENT_URI, PROJECTION, SpaceListContentProvider.Spaces.NAME + "='" + TEST_VAL + "'", null, null);
        assertEquals(c.getCount(), 0);
    }
}

