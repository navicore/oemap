package com.onextent.test.oemap;

import android.test.ActivityInstrumentationTestCase2;

import com.onextent.oemap.OeMapActivity;

public class OeMapActivityTestCase extends ActivityInstrumentationTestCase2<OeMapActivity> {

    private OeMapActivity mActivity;

    public OeMapActivityTestCase() {
        super(OeMapActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        mActivity = getActivity();

    } // end of setUp() method definition

    public void testOne() {

        assertNotNull(mActivity);
    }
}
