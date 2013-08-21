package com.onextent.oemap;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
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
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.util.ListDbHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.presence.SearchDialog;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.PresenceHelper;
import com.onextent.oemap.provider.SpaceProvider;
import com.onextent.oemap.settings.OeMapPreferencesDialog;

import java.util.ArrayList;
import java.util.List;

public class OeMapActivity extends OeBaseActivity {

    public static final int NEW_PUBLIC_MAP  = 0;
    public static final int SHARE_MAP_POS   = 1;
    public static final int EDIT_MAP_POS    = 2;
    public static final int LIST_COHORTS_POS= 3;
    public static final int QUIT_MAP_POS    = 4;
    public static final int SEPARATOR_POS   = 5;

    private static final String MAP_FRAG_TAG = "oemap";
    private static final int MAX_HISTORY = 20;

    private DrawerLayout mDrawerLayout;
    private DrawerAdapter _drawerAdapter = null;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle = "na";
    private String[] mMenuNames;
    private ListView mDrawerList;
    private ArrayList<String> mDrawerNamesList;
    private String mMapFragTag;
    private PresenceHelper _prefHelper = null;
    private KvHelper _prefs = null;
    private ListDbHelper _history_store = null;
    private ArrayAdapter<String> _spacenames_adapter = null;
    private List<String> _spacenames = null;

    private boolean aMapIsActive() {
        String m = _prefs.get(getString(R.string.state_current_mapname), getString(R.string.null_map_name));
        return !getString(R.string.null_map_name).equals(m);
    }

    public GoogleMap getMap() {

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment fragment = (MapFragment) fragmentManager.findFragmentByTag(mMapFragTag);
        if (fragment != null && mMapFragTag != null) {
            GoogleMap map = fragment.getMap();
            return map;
        } else {
            OeLog.w("null map tag!!!");
        }
        return null;
    }

    public void setMapFragTag(String tag) {
        mMapFragTag = tag;
    }

    public OeMapFragment getMapFrag() {
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

        if (!_spacenames.contains(mapname)) {
            _spacenames.add(mapname);
            _spacenames_adapter.notifyDataSetChanged();
        }
        int pos = _spacenames.indexOf(mapname);
        getActionBar().setSelectedNavigationItem(pos);
        updateSpaceNames(mapname);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private void startSettingsDialog() {

        DialogFragment f = new OeMapPreferencesDialog();
        f.show(getFragmentManager(), "OeMap Preferences Dialog");
    }

    private void showNewPrivateMapDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewPrivateSpaceDialog();
        d.show(fm, "new_priv_map_dialog");
    }

    public void showSpaceSettingsDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new SpaceSettingsDialog();
        Bundle args = new Bundle();
        String spacename = _prefs.get(getString(R.string.state_current_mapname), null);
        args.putString(getString(R.string.bundle_spacename), spacename);
        d.setArguments(args);
        d.show(fm, "space_settings_dialog");
    }
    public void showMarkerDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new MarkerDialog();
        Bundle args = new Bundle();
        String spacename = _prefs.get(getString(R.string.state_current_mapname), null);
        args.putString(getString(R.string.bundle_spacename), spacename);
        d.setArguments(args);
        d.show(fm, "list_markers_dialog");
    }

    private void showNewSpaceDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewSpaceDialog();
        d.show(fm, "new_space_dialog");
    }

    private void updateSpaceNames(String n) {
        _prefs.replace(getString(R.string.state_current_mapname), n);
        _history_store.replace(n);

        if (mDrawerNamesList.contains(n)) {
            mDrawerNamesList.remove(n);
        }
        mDrawerNamesList.add(SEPARATOR_POS + 1, n);
        _drawerAdapter.notifyDataSetChanged();
    }

    void enableNewSpace(String newMapName) {
        setMapFrag(newMapName);
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_ADD_SPACE);
        i.putExtra(OeMapPresenceService.KEY_SPACENAME, newMapName);
        startService(i);
    }

    void onFinishNewSpaceDialog(String newMapName) {
        enableNewSpace(newMapName);
        Toast.makeText(this, "New map '" + newMapName + "' created.", Toast.LENGTH_SHORT).show();
    }

    private void wakePresenceService() {
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_POLL);
        startService(i);
    }

    private void quitSpace() {

        String spacename = _prefs.get(getString(R.string.state_current_mapname), null);
        setMapFrag(getString(R.string.null_map_name));
        _spacenames.remove(spacename);
        _spacenames_adapter.notifyDataSetChanged();
        //todo: helper method that sets location for none

        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_RM_SPACE);
        i.putExtra(OeMapPresenceService.KEY_SPACENAME, spacename);
        startService(i);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        mDrawerList.setItemChecked(position, true);

        switch (position) {
            case NEW_PUBLIC_MAP:
                showNewSpaceDialog();
                break;
            case SHARE_MAP_POS:
                break;
            case EDIT_MAP_POS:
                showSpaceSettingsDialog();
                break;
            case LIST_COHORTS_POS:
                showMarkerDialog();
                break;
            case QUIT_MAP_POS:
                quitSpace();
                break;
            default:
                //setMapFrag((String) mDrawerNamesList.get(position));
                String m = mDrawerNamesList.get(position);
                enableNewSpace(m);
                break;
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void clearMapNamesHistory() {
        //clear history
        int sz = mDrawerNamesList.size();
        for (int i = SEPARATOR_POS + 1; i < sz; i++) {
            mDrawerNamesList.remove(mDrawerNamesList.size() - 1);
        }
        _drawerAdapter.notifyDataSetChanged();
    }

    public void updateMapNamesFromHistory() {

        List<String> h = null;
        h = _history_store.getAll();
        if (h == null)
            h = new ArrayList<String>();

        for (String s : h) {
            if (!mDrawerNamesList.contains(s)) {

                mDrawerNamesList.add(s);
            }
        }
        _drawerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        _history_store.close();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.oe_map_activity);

        _prefHelper = new PresenceHelper(this);
        _prefs = new KvHelper(this);
        _history_store = new ListDbHelper(this, "oemap_history_store");

        mMenuNames = getResources().getStringArray(R.array.menu_names_array);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mTitle = getResources().getString(R.string.app_name);

        mDrawerNamesList = new ArrayList();
        for (String n : mMenuNames) {
            mDrawerNamesList.add(n);
        }

        _drawerAdapter = new DrawerAdapter(this, R.layout.drawer_list_item, mDrawerNamesList);
        mDrawerList.setAdapter(_drawerAdapter);
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

        setActiveMapsSpinner();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("");
    }

    @Override
    public void setTitle(CharSequence s) {
        super.setTitle("");
    }


    private void setActiveMapsSpinner() {

        ActionBar actionBar = getActionBar();
        _spacenames = null;
        _spacenames = new ArrayList<String>();
        Cursor c = getContentResolver().query(SpaceProvider.CONTENT_URI,
                SpaceProvider.Spaces.PROJECTION_ALL, null, null,
                SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
        int pos = c.getColumnIndex(SpaceProvider.Spaces._ID);
        while (c.moveToNext()) {
            String n = c.getString(pos);
            _spacenames.add(n);
        }
        c.close();
        _spacenames_adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, _spacenames);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        _spacenames_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(_spacenames_adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                String n = _spacenames.get(i);
                setMapFrag(n);
                return false;
            }
        });
    }

    private void setMapTyp(int t) {

        GoogleMap m = getMap();
        if (m != null) {
            m.setMapType(t);
        }
        _prefs.replaceInt(getString(R.string.pref_map_type), t);
    }

    private void initMapTypeMenu(Menu menu) {


        MenuItem typeItem = null;
        int t = _prefs.getInt(getString(R.string.pref_map_type), GoogleMap.MAP_TYPE_NORMAL);
        switch (t) {
            case GoogleMap.MAP_TYPE_NORMAL:
                typeItem = menu.findItem(R.id.map_type_normal);
                break;
            case GoogleMap.MAP_TYPE_TERRAIN:
                typeItem = menu.findItem(R.id.map_type_terrain);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                typeItem = menu.findItem(R.id.map_type_satellite);
                break;
            default:
                OeLog.e("unknown map type: " + t);
                break;
        }
        if (typeItem != null)
            typeItem.setChecked(true);

        boolean showTraffic = _prefs.getBoolean(getString(R.string.pref_show_traffic), false);
        MenuItem trafficItem = menu.findItem(R.id.menu_show_traffic);
        trafficItem.setChecked(showTraffic);

        boolean showIndoors = _prefs.getBoolean(getString(R.string.pref_show_indoors), false);
        MenuItem inDoorsItem = menu.findItem(R.id.menu_show_indoors);
        inDoorsItem.setChecked(showIndoors);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        initMapTypeMenu(menu);

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
        boolean checked;
        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.action_search:
                startSearchDialog();
                break;
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
            case R.id.map_type_normal:
                item.setChecked(true);
                setMapTyp(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_type_terrain:
                item.setChecked(true);
                setMapTyp(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.map_type_satellite:
                item.setChecked(true);
                setMapTyp(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.menu_show_traffic:
                checked = item.isChecked();
                item.setChecked(!checked);
                setShowTrafficOption(!checked);
                break;
            case R.id.menu_show_indoors:
                checked = item.isChecked();
                item.setChecked(!checked);
                setShowInDoorsOption(!checked);
                break;
            case R.id.action_about:
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSearchDialog() {

        DialogFragment f = new SearchDialog();
        f.show(getFragmentManager(), "OeMap Search Dialog");
    }

    private void setShowTrafficOption(boolean b) {

        GoogleMap m = getMap();
        if (m != null) {
            m.setTrafficEnabled(b);
        }
        _prefs.replaceBoolean(getString(R.string.pref_show_traffic), b);
    }

    private void setShowInDoorsOption(boolean b) {

        GoogleMap m = getMap();
        if (m != null) {
            m.setIndoorEnabled(b);
        }
        _prefs.replaceBoolean(getString(R.string.pref_show_indoors), b);
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

        String mapname = _prefs.get(getString(R.string.state_current_mapname), getString(R.string.null_map_name));
        setMapFrag(mapname);
        updateMapNamesFromHistory();
        wakePresenceService();
    }

    @Override
    public void onPause() {

        saveHistory();
        clearMapNamesHistory();
        super.onPause();
    }

    private void saveHistory() {
        try {
            _history_store.deleteAll();
            int sz = mDrawerNamesList.size();
            for (int i = SEPARATOR_POS + 1; i < sz && i < MAX_HISTORY; i++) {
                _history_store.insert(mDrawerNamesList.get(i));
            }
        } catch (SQLException ex) {
            OeLog.w(ex.toString(), ex);
        }
    }

    private class DrawerAdapter extends ArrayAdapter {

        public DrawerAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int pos) {

            switch (pos) {
                case SHARE_MAP_POS:
                case SEPARATOR_POS:
                    return false;
                case QUIT_MAP_POS:
                    return OeMapActivity.this.aMapIsActive();
                case LIST_COHORTS_POS:
                case NEW_PUBLIC_MAP:
                case EDIT_MAP_POS:
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

