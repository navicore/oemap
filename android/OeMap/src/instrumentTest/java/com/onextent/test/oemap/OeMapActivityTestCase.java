package com.onextent.test.oemap;

import android.app.KeyguardManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;

public class OeMapActivityTestCase extends ActivityInstrumentationTestCase2<OeMapActivity> {

    private OeMapActivity mActivity;
    private ListView mDrawerList;
    private Adapter mDrawerData;

    public OeMapActivityTestCase() {
        super(OeMapActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);

        mActivity = getActivity();
        mDrawerList = (ListView) mActivity.findViewById(R.id.left_drawer);
        mDrawerData = mDrawerList.getAdapter();
    }

    public void testOne() {

        assertNotNull(mActivity);
        assertNotNull(mDrawerList);
        assertNotNull(mDrawerData);
    }
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testAddMap() {
        /*

        ISSUES:
        - sendKey up isn't working.  perhaps because of disabled items
        - new map dialog does not open

        //final int INIT_POS = OeMapActivity.SEPARATOR_POS + 1;
        final int INIT_POS = OeMapActivity.NEW_PUBLIC_MAP;
        final int NEW_POS = OeMapActivity.NEW_PUBLIC_MAP;
        //final int NEW_POS = OeMapActivity.SEPARATOR_POS + 1;
        //final int INIT_POS = OeMapActivity.NEW_PUBLIC_MAP;

        mActivity.runOnUiThread(
            new Runnable() {
                public void run() {
                    mDrawerList.requestFocus();
                    mDrawerList.setItemChecked(INIT_POS, true);
                }
            }
        );
        getInstrumentation().waitForIdleSync();
        sleep(2000);
        this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        sleep(2000);
        for (int i = INIT_POS; i > NEW_POS; i--) {
            sleep(2000);
            OeLog.d("sending an up key");
            this.sendKeys(KeyEvent.KEYCODE_DPAD_UP);
            getInstrumentation().waitForIdleSync();
            //this.sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        //if (true) return;

        this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        sleep(2000);

        int pos = mDrawerList.getSelectedItemPosition();
        assertEquals(pos, NEW_POS);
        String selection = (String)mDrawerList.getItemAtPosition(pos);
        assertEquals(selection, mActivity.getResources().getStringArray(R.array.menu_names_array)[0]);

        */
    }
}
