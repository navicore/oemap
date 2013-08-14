package com.onextent.test.oemap;

import android.test.AndroidTestCase;

import com.onextent.android.util.ListDbHelper;
import com.onextent.oemap.store.SpaceListContentProvider;

import org.json.JSONException;

public class SpaceListContentProviderTestCase extends AndroidTestCase {

    private ListDbHelper _dbHelper;
    private SpaceListContentProvider _provider;

    @Override
    protected void setUp() {
        //_provider = new SpaceListContentProvider();
        //_provider.onCreate();

        //_dbHelper = _provider.getDbHelper();

        //_dbHelper.insert("one");
        //_dbHelper.insert("two");
        //_dbHelper.insert("three");
    }

    @Override
    protected void tearDown() throws Exception {
        //_dbHelper.close();
        //super.tearDown();
    }

    public void testQueryAll() {

        //Cursor c = _provider.query(SpaceListContentProvider.CONTENT_URI, null, null, null, null);

    }

    public void testQueryOne() {

    }

    public void testDelete() throws JSONException {

    }
}

