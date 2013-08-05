package com.onextent.oemap;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OeMapActivity extends OeBaseActivity
{
    private static final String MAP_FRAG_TAG = "oemap";
    private String KEY_SPACENAMES  = null;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    //private PresenceBroadcaster presenceBroadcaster = null;

    private GoogleMap mMap;
    private CharSequence mTitle =  "na";
    private String mDrawerTitle = "na";
    private String[] mMenuNames;
    private ListView mDrawerList;
    private ArrayList mDrawerNamesList;
    //private ArrayList mMapHistory;

    private String mMapFragTag;

    private static final int NEW_PUBLIC_MAP     = 0;
    private static final int NEW_PRIVATE_MAP    = 1;
    private static final int LIST_COHORTS_POS   = 2;
    private static final int SHARE_MAP_POS      = 3;
    private static final int QUIT_MAP_POS       = 4;
    private static final int SEPARATOR_POS      = 5;
    //private GcmHelper mGcmHelper;

    private class DrawerAdapter extends ArrayAdapter {

        public DrawerAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int pos) {

            switch (pos) {
                case NEW_PRIVATE_MAP:
                case LIST_COHORTS_POS:
                case SHARE_MAP_POS:
                case SEPARATOR_POS:
                    return false;
                case QUIT_MAP_POS:
                    return OeMapActivity.this.aMapIsActive();
                case NEW_PUBLIC_MAP:
                default:
                    return true;
            }
        }


        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
    }

    private boolean aMapIsActive() {
        String m = getPrefs().getString(getString(R.string.state_current_mapname), "none");
        return !"none".equals(m);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position != SEPARATOR_POS) //ejs skip separator todo: disable!!!
            selectItem(position);
        }
    }

    private GoogleMap getMap() {

        if (mMap != null) return mMap;
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment fragment = (MapFragment) fragmentManager.findFragmentByTag(mMapFragTag);
        if (fragment != null && mMapFragTag != null) {
            mMap = fragment.getMap();
            return mMap;
        } else {
            OeLog.w("null map tag!!!");
        }
        return null;
    }

    public void setMapFragTag(String tag) {
        mMapFragTag = tag;
    }

    private void updateMapFrag(String mapname) {
        Toast.makeText(this, "Showing '" + mapname + "'.", Toast.LENGTH_SHORT).show();
        setMapFrag(mapname);
        updateMapNames(mapname);
        setTitle(mapname);
    }

    private OeMapFragment getMapFrag() {
        FragmentManager fragmentManager = getFragmentManager();
        if (mMapFragTag != null) {
            return (OeMapFragment) fragmentManager.findFragmentByTag(MAP_FRAG_TAG);
        }
        return null;
    }
    private void setMapFrag(String mapname) {

        //MapFragment fragment = getMapFrag();
        MapFragment fragment = null;  //creating a new frag per map, optimise later

        if (fragment == null) {
            fragment = new OeMapFragment();
            Bundle args = new Bundle();
            args.putString("mapname", mapname);
            fragment.setArguments(args);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(
                    R.id.content_frame, fragment, MAP_FRAG_TAG
            ).commit();
        }
    }

    private void startSettingsDialog() {

        Intent intent = new Intent(this, OeMapSettingsActivity.class);
        startActivity(intent);
    }

    private void showNewPrivateMapDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewPrivateMapDialog();
        d.show(fm, "new_priv_map_dialog");
    }
    private void showNewMapDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewMapDialog();
        d.show(fm, "new_map_dialog");
    }

    private void updateMapNames(String n) {
        SharedPreferences.Editor e = getEditor();
        e.putString(getString(R.string.state_current_mapname), n);
        e.commit();
        if (!mDrawerNamesList.contains(n)) {
            mDrawerNamesList.add(SEPARATOR_POS + 1, n);
            mDrawerList.deferNotifyDataSetChanged();
        }
        //addHistory(n);
        int position = mDrawerNamesList.indexOf(n);
        mDrawerList.setItemChecked(position, true);
    }

    void onFinishNewMapDialog(String newMapName) {
        updateMapFrag(newMapName);
        Toast.makeText(this, "New map '" + newMapName + "' created.", Toast.LENGTH_SHORT).show();
        Intent i =  new Intent(this, OeMapPresenceService.class);
        i.putExtra("reason", R.string.presence_service_cmd_add_space);
        i.putExtra(getString(R.string.presence_service_key_reason), getString(R.string.presence_service_cmd_add_space));
        i.putExtra(getString(R.string.presence_service_key_spacename), newMapName);
        startService(i);
    }

    private void wakePresenceService() {
        Intent i =  new Intent(this, OeMapPresenceService.class);
        i.putExtra(getString(R.string.presence_service_key_reason), getString(R.string.presence_service_cmd_poll));
        startService(i);
    }

    private void quitMap() {

        String spacename = getPrefs().getString(getString(R.string.state_current_mapname), null);
        if (mDrawerNamesList.contains(spacename)) {
            mDrawerNamesList.remove(spacename);
            mDrawerList.deferNotifyDataSetChanged();
        }
        setMapFrag("none");
        updateMapFrag("none");

        Intent i =  new Intent(this, OeMapPresenceService.class);
        i.putExtra(getString(R.string.presence_service_key_reason), getString(R.string.presence_service_cmd_rm_space));
        i.putExtra(getString(R.string.presence_service_key_spacename), spacename);
        startService(i);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        //todo: sets map overlay based on selection?
        switch (position) {
            case NEW_PUBLIC_MAP:
                showNewMapDialog();
                break;
            case NEW_PRIVATE_MAP:
                showNewPrivateMapDialog();
                break;
            case SHARE_MAP_POS:
                break;
            case QUIT_MAP_POS:
                quitMap();
                break;
            default:
                updateMapFrag((String) mDrawerNamesList.get(position));
                break;
        }

        mDrawerList.setItemChecked(position, true);
        setTitle((String) mDrawerNamesList.get(position));
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private void updateMapNamesFromHistory() {

        //List<String> h = getHistory();
        Set<String> h =  getPrefs().getStringSet(KEY_SPACENAMES, null);
        if (h != null)
        for (String s : h) {
            if (!mDrawerNamesList.contains(s)) {
                mDrawerNamesList.add(SEPARATOR_POS + 1, s);
            }
        }
    }

    /*

    public PresenceBroadcaster getPresenceBroadcaster() {
        return presenceBroadcaster;
    }
     */

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.oe_map_activity);

        KEY_SPACENAMES  = getString(R.string.presence_service_key_spacenames);
        //presenceBroadcaster = PresenceFactory.createBroadcaster(this);
        //presenceBroadcaster.onCreate();

        //mGcmHelper = new GcmHelper(this);
        //mGcmHelper.onCreate();

        mMenuNames = getResources().getStringArray(R.array.menu_names_array);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mTitle =  getResources().getString(R.string.app_name);
        mDrawerTitle =  getResources().getString(R.string.drawer_title);

        mDrawerNamesList = new ArrayList();
        for (String n : mMenuNames) {
            mDrawerNamesList.add(n);
        }
        updateMapNamesFromHistory();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
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
        switch (item.getItemId()) {
            case R.id.action_refresh:
                OeMapFragment f = getMapFrag();
                if (f != null) {
                    boolean success = f.setLocation();
                    if (!success)
                        Toast.makeText(this, getString(R.string.msg_still_looking), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_help:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.onextent.com"));
                startActivity(browserIntent);
                break;
            case R.id.action_settings:
                startSettingsDialog();
                break;
            case R.id.action_about:
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {

        //presenceBroadcaster.onStop();
        super.onStop();
    }

    @Override
    public void onStart() {

        super.onStart();
        //presenceBroadcaster.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        //presenceBroadcaster.onResume();
        //mGcmHelper.onResume();
        SharedPreferences p = getPrefs();
        String mapname = p.getString(getString(R.string.state_current_mapname), "none");
        setMapFrag(mapname);
        updateMapFrag(mapname);
        wakePresenceService();
    }

    @Override
    public void onPause() {

        //mGcmHelper.onPause();
        //presenceBroadcaster.onPause();
        super.onPause();
    }
}

