package com.onextent.oemap;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class OeMapActivity extends Activity implements LocationListener
{
    LocationHelper mLocHelper;

    private static final String MAPFRAGTAG = "oemap";
    private static final String SETFRAGTAG = "oeset";

    private boolean mSharingLoc;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle =  "na";
    private String mDrawerTitle = "na";
    private String[] mPlanetTitles;
    private ListView mDrawerList;
    private ArrayList mDrawerNamesList;

    private String mMapFragTag;

    private static class DrawerAdapter extends ArrayAdapter {

        public DrawerAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int pos) {

            if (pos == SEPARATOR_POS) {
                return false;
            }
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position != SEPARATOR_POS) //ejs skip separator todo: disable!!!
            selectItem(position);
        }
    }

    private GoogleMap getMap() {

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment fragment = (MapFragment) fragmentManager.findFragmentByTag(mMapFragTag);
        if (fragment != null && mMapFragTag != null) {
            return fragment.getMap();
        } else {
            Log.w("ejs", "null map tag!!!");
        }
        return null;
    }

    public static class OeMapFragment extends MapFragment {

        private OeMapActivity home;
        public OeMapFragment() {
            super();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            home = (OeMapActivity) activity;
        }

        private void init() {
            GoogleMap map = getMap();
            if (map == null) return;
            UiSettings settings = map.getUiSettings();
            //settings.setAllGesturesEnabled(false);
            settings.setMyLocationButtonEnabled(true);
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            home.setMapFragTag(getTag());
            init();
        }
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            //GoogleMap map = getMap();
            //Location l = map.getMyLocation();
            //String loc = l.toString();
            //Log.d("ejs", loc);
            Log.d("ejs", "hiya");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = super.onCreateView(inflater, container, savedInstanceState);

            return rootView;
        }
    }

    private void setSettingsFrag() {
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new OeMapSettings(), SETFRAGTAG)
                .commit();
    }

    public void setMapFragTag(String tag) {
        mMapFragTag = tag;
    }

    private void setMapFrag(int position) {

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment fragment = null;
        if (mMapFragTag != null) {
            fragment = (MapFragment) fragmentManager.findFragmentByTag(mMapFragTag);
        }
        if (fragment == null) {
            fragment = new OeMapFragment();
        }
        //Fragment fragment = new OeMapFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, MAPFRAGTAG)
                .commit();
    }

    private static int NEW_MAP_POS = 0;
    private static int SHARE_POS = 1;
    private static int HELP_POS = 2;
    private static int SETTINGS_POS = 3;
    private static int ABOUT_POS = 4;
    private static int SEPARATOR_POS = 5;

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        //todo: sets map overlay based on selection?
        if (position == SETTINGS_POS) {

            setSettingsFrag();

        } else {

            setMapFrag(position);
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle((String) mDrawerNamesList.get(position));
        mDrawerLayout.closeDrawer(mDrawerList);

        //test
        if (!mDrawerNamesList.contains(mPlanetTitles[position])) {
            mDrawerNamesList.add(mPlanetTitles[position]);
            mDrawerList.deferNotifyDataSetChanged();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.oe_map_activity);

        mLocHelper = new LocationHelper(this);
        mLocHelper.onCreate(savedInstanceState);

        mPlanetTitles = getResources().getStringArray(R.array.menu_names_array);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mTitle =  getResources().getString(R.string.app_name);
        mDrawerTitle =  getResources().getString(R.string.drawer_title);

        mDrawerNamesList = new ArrayList();
        for (String n : mPlanetTitles) {
            mDrawerNamesList.add(n);
        }
        ListAdapter a = new DrawerAdapter(this, R.layout.drawer_list_item, mDrawerNamesList);
        mDrawerList.setAdapter(a);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) throw new NullPointerException("drawer layout not found, inflate first!");
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mLocHelper.onActivityResult(requestCode, resultCode, data);
     }

    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {

        mLocHelper.onStop();

        super.onStop();
    }
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {

        mLocHelper.onPause();
        // Save the current setting for updates
        //mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        //mEditor.commit();

        super.onPause();
    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();
        mLocHelper.onStart();
    }

        @Override
    public void onResume() {
        super.onResume();
        mLocHelper.onResume();
    }

    private Location mCurrLoc;

    public void onLocationChanged(Location location) {

        mCurrLoc = location;
        Log.d("ejs", "Got location");

        // Report to the UI that the location was updated
        //mConnectionStatus.setText(R.string.location_updated);

        // In the UI, set the latitude and longitude to the value received
        //mLatLng.setText(LocationUtils.getLatLng(this, location));
        GoogleMap map = getMap();
        if (map == null) {

            Log.w("ejs", "Got location but NO MAP!!!");
            return;
        }

        LatLng latLng = new LatLng(mCurrLoc.getLatitude(), mCurrLoc.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        //map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.common_signin_btn_icon_dark)));
    }
}

