package com.onextent.oemap;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.presence.PresenceDbHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OeMapActivity extends OeBaseActivity {
    public static final int NEW_PUBLIC_MAP = 0;
    public static final int NEW_PRIVATE_MAP = 1;
    public static final int LIST_COHORTS_POS = 2;
    public static final int SHARE_MAP_POS = 3;
    public static final int QUIT_MAP_POS = 4;
    public static final int SEPARATOR_POS = 5;
    private static final String MAP_FRAG_TAG = "oemap";
    private String KEY_SPACENAMES = null;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private GoogleMap mMap;
    private CharSequence mTitle = "na";
    private String mDrawerTitle = "na";
    private String[] mMenuNames;
    private ListView mDrawerList;
    private ArrayList mDrawerNamesList;
    private String mMapFragTag;
    private PresenceDbHelper _dbHelper = null;

    private boolean aMapIsActive() {
        String m = getDefaultPrefs().getString(getString(R.string.state_current_mapname), getString(R.string.null_map_name));
        return !getString(R.string.null_map_name).equals(m);
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
        //setTitle(mapname);
    }

    private OeMapFragment getMapFrag() {
        FragmentManager fragmentManager = getFragmentManager();
        if (mMapFragTag != null) {
            return (OeMapFragment) fragmentManager.findFragmentByTag(MAP_FRAG_TAG);
        }
        return null;
    }

    private void setMapFrag(String mapname) {

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
        SharedPreferences.Editor e = getDefaultPrefs().edit();
        e.clear();
        e.putString(getString(R.string.state_current_mapname), n);
        e.commit();
        if (!mDrawerNamesList.contains(n)) {
            mDrawerNamesList.add(SEPARATOR_POS + 1, n);
            mDrawerList.deferNotifyDataSetChanged();
        }
        //int position = mDrawerNamesList.indexOf(n);
        //mDrawerList.setItemChecked(position, true);
    }

    void onFinishNewMapDialog(String newMapName) {
        updateMapFrag(newMapName);
        Toast.makeText(this, "New map '" + newMapName + "' created.", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra("reason", R.string.presence_service_cmd_add_space);
        i.putExtra(getString(R.string.presence_service_key_reason), getString(R.string.presence_service_cmd_add_space));
        i.putExtra(getString(R.string.presence_service_key_spacename), newMapName);
        startService(i);
    }

    private void wakePresenceService() {
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(getString(R.string.presence_service_key_reason), getString(R.string.presence_service_cmd_poll));
        startService(i);
    }

    private void quitMap() {

        String spacename = getDefaultPrefs().getString(getString(R.string.state_current_mapname), null);
        if (mDrawerNamesList.contains(spacename)) {
            mDrawerNamesList.remove(spacename);
            mDrawerList.deferNotifyDataSetChanged();
        }
        setMapFrag(getString(R.string.null_map_name));
        updateMapFrag(getString(R.string.null_map_name));

        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(getString(R.string.presence_service_key_reason), getString(R.string.presence_service_cmd_rm_space));
        i.putExtra(getString(R.string.presence_service_key_spacename), spacename);
        startService(i);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        mDrawerList.setItemChecked(position, true);

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

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public void updateMapNamesFromHistory() {

        Set<String> h = null;
        try {
            h = _dbHelper.getAllSpacenames();
        } catch (JSONException e) {
            OeLog.e(e.toString(), e);
            return;
        }
        if (h != null) {

            while(mDrawerNamesList.size() > SEPARATOR_POS + 1) {
                mDrawerNamesList.remove(SEPARATOR_POS + 1);
            }

            for (String s : h) {
                if (!mDrawerNamesList.contains(s)) {
                    mDrawerNamesList.add(SEPARATOR_POS + 1, s);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        _dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.oe_map_activity);

        _dbHelper = new PresenceDbHelper(this, getString(R.string.presence_db_name));

        KEY_SPACENAMES = getString(R.string.presence_service_key_spacenames);

        mMenuNames = getResources().getStringArray(R.array.menu_names_array);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mTitle = getResources().getString(R.string.app_name);
        mDrawerTitle = getResources().getString(R.string.drawer_title);

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
        if (mDrawerLayout == null)
            throw new NullPointerException("drawer layout not found, inflate first!");
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                //bug?  doesn't show bar on lenovo
                getActionBar().setDisplayShowTitleEnabled(false);
                //getActionBar().setTitle(mTitle);
                int pos = mDrawerList.getCheckedItemPosition();
                mDrawerList.setItemChecked(pos, false);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                //bug?  doesn't show bar on lenovo
                getActionBar().setDisplayShowTitleEnabled(true);
                //getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getActionBar();
        String[] listItems = {"none"};
        //SpinnerAdapter adapter = new ArrayAdapter(this,  android.R.layout.simple_list_item_1, listItems);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, listItems);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                return false;
            }
        });

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    private void initMapTypeSpinner(Menu menu) {


        //final String[] listItems = {"Normal", "Terrain", "Satellite"};
        final String[] listItems = getResources().getStringArray(R.array.pref_map_type_entries);
        final int[] listValues = getResources().getIntArray(R.array.pref_map_type_values);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);

        Spinner spinner = (Spinner) menu.findItem(R.id.map_types).getActionView();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //int pos = Integer.valueOf(getDefaultPrefs().getString(getString(R.string.pref_map_type), "2"));
        int pos = Integer.valueOf(getDefaultPrefs().getString(getString(R.string.pref_map_type), "0"));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                OeLog.d("set map type to " + listItems[i]);
                //int t = Integer.valueOf(mPrefs.getString(getString(R.string.pref_map_type), "0"));
                SharedPreferences prefs = getDefaultPrefs();
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(getString(R.string.pref_map_type), Integer.toString(i));
                edit.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setSelection(pos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        initMapTypeSpinner(menu);

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
                    if (!success) {

                        if (getString(R.string.null_map_name).equals(f.getName())) {
                            Toast.makeText(this, getString(R.string.null_map_location_message), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, getString(R.string.msg_still_looking), Toast.LENGTH_SHORT).show();
                        }
                    }
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

        super.onStop();
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getDefaultPrefs();

        String mapname = prefs.getString(getString(R.string.state_current_mapname), getString(R.string.null_map_name));
        setMapFrag(mapname);
        updateMapFrag(mapname);
        wakePresenceService();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position != SEPARATOR_POS) //ejs skip separator todo: disable!!!
                selectItem(position);
        }
    }
}

