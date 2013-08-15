package com.onextent.test.oemap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.provider.KvProvider;
import com.onextent.oemap.provider.KvProvider;

import org.json.JSONException;

public class KvProviderTestCase extends AndroidTestCase {

    private static final String[] PROJECTION = KvProvider.Kv.PROJECTION_ALL;

    private ContentResolver _resolver;

    private String[][] TEST_VALUES = {{"k1", "v1"},{"k2", "v2"}, {"k3", "v3"}};

    @Override
    protected void setUp() {

        _resolver = getContext().getContentResolver();

        _resolver.delete(KvProvider.CONTENT_URI, null, null);
        for (String[] s : TEST_VALUES) {

            ContentValues values = new ContentValues();
            values.put(KvProvider.Kv.KEY, s[0]);
            values.put(KvProvider.Kv.VALUE, s[1]);
            _resolver.insert(KvProvider.CONTENT_URI, values);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        //noop
        super.tearDown();
    }

    public void testQueryAll() {

        final String[] TEST_VAL = {"we", "won!"};

        ContentValues values = new ContentValues();
        values.put(KvProvider.Kv.KEY, TEST_VAL[0]);
        values.put(KvProvider.Kv.VALUE, TEST_VAL[1]);
        Uri r = _resolver.insert(KvProvider.CONTENT_URI, values);
        OeLog.d("insert uri: " + r);

        Cursor c = _resolver.query(KvProvider.CONTENT_URI, null, null, null, null);
        int key_col =  c.getColumnIndex(KvProvider.Kv.KEY);
        int val_col =  c.getColumnIndex(KvProvider.Kv.VALUE);
        while (c.moveToNext()) {

            String key = c.getString(key_col);
            String val = c.getString(val_col);
            OeLog.d("found key: " + key + " value: " + val);
        }
        c.close();
    }

    public void testQueryOneWithSelectionArgs() {

        final String[] TEST_VAL = {"you", "lost!"};

        ContentValues values = new ContentValues();
        values.put(KvProvider.Kv.KEY, TEST_VAL[0]);
        values.put(KvProvider.Kv.VALUE, TEST_VAL[1]);
        _resolver.insert(KvProvider.CONTENT_URI, values);

        String[] args = {TEST_VAL[0]};
        Cursor c = _resolver.query(KvProvider.CONTENT_URI,
                PROJECTION, KvProvider.Kv.KEY + "=?", args, null);
        assertEquals(c.getCount(), 1);
        c.moveToFirst();

        int kcol = c.getColumnIndex(KvProvider.Kv.KEY);
        assertTrue(kcol >= 0);
        String key = c.getString(kcol);
        assertEquals(key, TEST_VAL[0]);

        int vcol = c.getColumnIndex(KvProvider.Kv.VALUE);
        assertTrue(kcol >= 0);
        String val = c.getString(vcol);
        assertEquals(val, TEST_VAL[1]);

        c.close();
    }

    public void testUriQuery() {

        final String[] TEST_VAL = {"go", "home!"};

        ContentValues values = new ContentValues();
        values.put(KvProvider.Kv.KEY, TEST_VAL[0]);
        values.put(KvProvider.Kv.VALUE, TEST_VAL[1]);
        Uri r = _resolver.insert(KvProvider.CONTENT_URI, values);

        Cursor c = _resolver.query(r, null, null, null, null);
        assertEquals(c.getCount(), 1);
        c.moveToFirst();
        int kcol = c.getColumnIndex(KvProvider.Kv.KEY);
        int vcol = c.getColumnIndex(KvProvider.Kv.VALUE);
        assertTrue(kcol >= 0);
        String key = c.getString(kcol);
        String val = c.getString(vcol);

        assertEquals(key, TEST_VAL[0]);
        assertEquals(val, TEST_VAL[1]);
        c.close();
    }

    public void testDelete() throws JSONException {

        final String TEST_VAL = "k1";

        Cursor c = _resolver.query(KvProvider.CONTENT_URI,
                PROJECTION, KvProvider.Kv.KEY + "='" + TEST_VAL + "'", null, null);
        c.moveToFirst();
        int i = c.getColumnIndex(KvProvider.Kv.KEY);
        assertTrue(i >= 0);
        String key = c.getString(i);
        assertEquals(key, TEST_VAL);
        i = c.getColumnIndex(KvProvider.Kv._ID);
        assertTrue(i >= 0);
        int id = c.getInt(i);
        c.close();

        ContentValues values = new ContentValues();
        values.put(KvProvider.Kv.KEY, TEST_VAL);
        _resolver.delete(KvProvider.CONTENT_URI, KvProvider.Kv._ID + "=" + id, null);

        c = _resolver.query(KvProvider.CONTENT_URI,
                PROJECTION, KvProvider.Kv.KEY + "='" + TEST_VAL + "'", null, null);
        assertEquals(c.getCount(), 0);
        c.close();
    }
}

