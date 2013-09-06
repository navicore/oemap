/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.util.ListDbHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.presence.SearchDialog;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.settings.OeMapPreferencesDialog;
import com.onextent.oemap.settings.SpaceSettingsDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class OeMapActivity extends OeBaseActivity {

    public static final int NEW_PUBLIC_MAP = 0;
    public static final int FIND_MAP_POS = 1;
    public static final int QUIT_MAP_POS = 2;
    public static final int SEPARATOR_POS = 3;
    private static final String MAP_SHARE_URL_BASE = "http://oemap.onextent.com/share?map=";
    private static final String OEMAP_INTENT_SUBJECT = "OeMap";
    private static final String MAP_FRAG_TAG = "oemap";
    private static final int MAX_HISTORY = 20;
    private ShareActionProvider _shareActionProvider;
    private ListView _drawerList;
    private ActionBarDrawerToggle _drawerToggle;
    private ArrayList<String> _drawerNamesList;
    private String _mapFragTag;
    private KvHelper _prefs = null;
    private ListDbHelper _history_store = null;
    private SpaceNamesAdapter _spaceNamesAdapter;
    private BroadcastReceiver _presenceReceiver = new QuitSpaceReceiver();
    private IntentFilter _presenceReceiverFilter = null;
    private boolean isRefreshing;
    private AnimationHelper _animHelper;
    private DataSetObserver _mapSpinnerObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            setActionBarToCurrentMap();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };

    private boolean aMapIsActive() {
        String m = getMapName();
        return !getString(R.string.null_map_name).equals(m);
    }

    public GoogleMap getMap() {

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment fragment = (MapFragment) fragmentManager.findFragmentByTag(_mapFragTag);
        if (fragment != null && _mapFragTag != null) {
            GoogleMap map = fragment.getMap();
            return map;
        } else {
            OeLog.w("null map tag!!!");
        }
        return null;
    }

    public void setMapFragTag(String tag) {
        _mapFragTag = tag;
    }

    public OeMapFragment getMapFrag() {
        FragmentManager fragmentManager = getFragmentManager();
        if (_mapFragTag != null) {
            return (OeMapFragment) fragmentManager.findFragmentByTag(MAP_FRAG_TAG);
        }
        return null;
    }

    public void setMapFrag(String mapname) {

        OeLog.d("setMapFrag: " + mapname);
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

        setShareIntent();
        updateSpaceNames(mapname);
        setActionBarToCurrentMap();
    }

    /*
    private void showNewPrivateMapDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewPrivateSpaceDialog();
        d.show(fm, "new_priv_map_dialog");
    }
     */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private void startSettingsDialog() {

        DialogFragment f = new OeMapPreferencesDialog();
        f.show(getFragmentManager(), "OeMap Preferences Dialog");
    }

    public void showLeaseDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new SpaceSettingsDialog();
        Bundle args = new Bundle();
        String spacename = getMapName();
        args.putString(getString(R.string.bundle_spacename), spacename);
        d.setArguments(args);
        d.show(fm, "space_settings_dialog");
    }

    public void showMarkerDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new MarkerDialog();
        Bundle args = new Bundle();
        String spacename = getMapName();
        args.putString(getString(R.string.bundle_spacename), spacename);
        d.setArguments(args);
        d.show(fm, "list_markers_dialog");
    }

    private void showNewSpaceDialogWithName(String space) {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewSpaceDialog();
        if (space != null) {
            Bundle b = new Bundle();
            b.putString(getString(R.string.bundle_spacename), space);
            d.setArguments(b);
        }
        d.show(fm, "new_space_dialog");
    }

    private void showNewSpaceDialog() {
        showNewSpaceDialogWithName(null);
    }

    private void updateSpaceNames(String n) {
        setMapName(n);
        _history_store.replace(n);

        if (_drawerNamesList.contains(n)) {
            _drawerNamesList.remove(n);
        }
        _drawerNamesList.add(SEPARATOR_POS + 1, n);
        notifyDrawList();
    }

    void enableNewSpace(String newMapName) {
        OeLog.d("enableNewSpace:: " + newMapName);
        setMapFrag(newMapName);
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_ADD_SPACE);
        i.putExtra(OeMapPresenceService.KEY_SPACENAME, newMapName);
        startService(i);
    }

    void onFinishNewSpaceDialog(String newMapName) {
        setMapName(newMapName);
        enableNewSpace(newMapName);
        Toast.makeText(this, "New map '" + newMapName + "' created.", Toast.LENGTH_SHORT).show();
    }

    public void wakePresenceBroadcastService() {
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_BCAST);
        startService(i);
    }

    public void wakePresenceService() {
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_POLL);
        startService(i);
    }

    private String getInactiveSpaceName(String curname) {
        for (int i = 0; i < _spaceNamesAdapter.getCount(); i++) {
            if (!curname.equals(_spaceNamesAdapter.getItem(i))) {
                return _spaceNamesAdapter.getItem(i).toString();
            }
        }
        return getString(R.string.null_map_name);
    }

    private void quitSpace() {

        String spacename = getMapName();
        //setMapFrag(getString(R.string.null_map_name));
        setMapFrag(getInactiveSpaceName(spacename));
        //todo: helper method that sets a nice UI and location for none

        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_RM_SPACE);
        i.putExtra(OeMapPresenceService.KEY_SPACENAME, spacename);
        startService(i);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {

        _drawerList.setItemChecked(position, true);

        switch (position) {
            case NEW_PUBLIC_MAP:
                showNewSpaceDialog();
                break;
            case FIND_MAP_POS:
                showSearchDialog();
                break;
            case QUIT_MAP_POS:
                quitSpace();
                break;
            default:
                String m = _drawerNamesList.get(position);
                SpaceHelper h = new SpaceHelper(this);
                SpaceHelper.Space space = h.getSpace(m);
                if (space == null) {
                    showNewSpaceDialogWithName(m);
                } else {
                    enableNewSpace(m);
                }
                break;
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(_drawerList);
    }

    public void clearMapNamesHistory() {
        //clear history
        int sz = _drawerNamesList.size();
        for (int i = SEPARATOR_POS + 1; i < sz; i++) {
            _drawerNamesList.remove(_drawerNamesList.size() - 1);
        }
        notifyDrawList();
    }

    private void notifyDrawList() {

        ((DrawerAdapter) _drawerList.getAdapter()).notifyDataSetChanged();
    }

    public void updateMapNamesFromHistory() {

        List<String> h = null;
        h = _history_store.getAll();
        if (h == null)
            h = new ArrayList<String>();

        for (String s : h) {
            if (!_drawerNamesList.contains(s)) {

                _drawerNamesList.add(s);
            }
        }
        notifyDrawList();
    }

    @Override
    protected void onDestroy() {
        OeLog.d("onDestroy: " + getMapName());
        _spaceNamesAdapter.onDestroy();
        _history_store.close();
        _animHelper.completeRefresh();
        super.onDestroy();
    }

    private void initDrawer() {
        String[] menuNames = getResources().getStringArray(R.array.menu_names_array);
        //mTitle = getResources().getString(R.string.app_name);

        _drawerNamesList = new ArrayList();
        for (String n : menuNames) {
            _drawerNamesList.add(n);
        }

        DrawerAdapter drawerAdapter = new DrawerAdapter(this, R.layout.drawer_list_item, _drawerNamesList);
        _drawerList = (ListView) findViewById(R.id.left_drawer);
        _drawerList.setAdapter(drawerAdapter);
        // Set the list's click listener
        _drawerList.setOnItemClickListener(new DrawerItemClickListener());
        _drawerList.setOnItemLongClickListener(new DrawerItemLongClickListener());

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout == null)
            throw new NullPointerException("drawer layout not found, inflate first!");
        _drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setDisplayShowTitleEnabled(false);
                int pos = _drawerList.getCheckedItemPosition();
                _drawerList.setItemChecked(pos, false);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setDisplayShowTitleEnabled(true);
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(_drawerToggle);
    }

    private void initActionBar() {

        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setHomeButtonEnabled(true);

        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setTitle("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.oe_map_activity);

        _animHelper = new AnimationHelper(this, R.layout.refresh_action_view, R.anim.clockwise_refresh);

        _prefs = new KvHelper(this);

        Intent i = getIntent();

        OeLog.d("onCreate: " + getMapName() + " with intent: " + i);

        _history_store = new ListDbHelper(this, "oemap_history_store");

        _spaceNamesAdapter = new SpaceNamesAdapter(getActionBar().getThemedContext());

        initDrawer();

        activeMapIsInStore();

        setActiveMapsSpinner();

        initActionBar();

        _presenceReceiverFilter = new IntentFilter(getString(R.string.presence_service_quit_space_intent));
    }

    @Override
    public void setTitle(CharSequence s) {
        super.setTitle("");
    }

    private boolean activeMapIsInStore() {

        boolean ret = false;
        //make sure the mapname is still in the spacedb it may get deleted on upgrade
        String currMapName = getMapName();
        if (currMapName != null && !currMapName.equals("") && !currMapName.equals(getString(R.string.null_map_name))) {
            SpaceHelper h = new SpaceHelper(this);
            SpaceHelper.Space s = h.getSpace(currMapName);

            if (s == null) {

                OeLog.w("activeMapIsInStore not in store: " + getMapName());
                ret = false;

            } else if (s.getLease().getTime() < System.currentTimeMillis()) {

                //expired
                String msg = String.valueOf(DateUtils.getRelativeTimeSpanString(this, s.getLease().getTime()));
                OeLog.d("activeMapIsInStore expiring: " + getMapName() + " lease: " + msg);
                h.deleteSpacename(currMapName);
                ret = false;

            } else {

                ret = true;
            }
        }
        return ret;
    }

    private void setActiveMapsSpinner() {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(_spaceNamesAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                String n = (String) _spaceNamesAdapter.getItem(i);
                OeLog.d("onNavigationItemSelcted pos " + i + " item id: " + l + " name: " + n);
                setMapFrag(n);
                return false;
            }
        });
        setActionBarToCurrentMap();
    }

    private void setActionBarToCurrentMap() {

        boolean success = false;
        String currName = getMapName();
        for (int i = 0; i < _spaceNamesAdapter.getCount(); i++) {

            String n = (String) _spaceNamesAdapter.getItem(i);
            if (n != null && n.equals(currName)) {

                getActionBar().setSelectedNavigationItem(i);
                success = true;
                break;
            }
        }
        if (success) OeLog.d("action bar set to current map");
        else
            OeLog.w("could not set action bar to current map"); //the problem must be that the notify isn't done yet
    }

    // Call to update the share intent
    private void setShareIntent() {

        String mapname = getMapName();
        String enc_mapname = null;
        try {
            enc_mapname = URLEncoder.encode(mapname, "UTF8");
        } catch (UnsupportedEncodingException e) {
            OeLog.e("setShareIntent " + e);
            return;
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, mapname + " " + OEMAP_INTENT_SUBJECT);
        i.putExtra(Intent.EXTRA_TEXT, MAP_SHARE_URL_BASE + enc_mapname);

        if (_shareActionProvider != null) {
            _shareActionProvider.setShareIntent(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        // Fetch and store ShareActionProvider
        _shareActionProvider = (ShareActionProvider) item.getActionProvider();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = _drawerLayout.isDrawerOpen(_drawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        _drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean handleMenuItem(MenuItem item) {
        boolean handled = false; //does not seem to matter what we return
        boolean checked;
        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.action_lease:
                showLeaseDialog();
                handled = true;
                break;
            case R.id.action_search:
                showMarkerDialog();
                handled = true;
                break;
            case R.id.action_refresh:
                _animHelper.refresh(item);
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
                    wakePresenceService();
                    wakePresenceBroadcastService();
                }
                handled = true;
                break;
            case R.id.action_help:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.onextent.com"));
                startActivity(browserIntent);
                handled = true;
                break;
            case R.id.action_settings:
                startSettingsDialog();
                handled = true;
                break;
            case R.id.action_map_features:
                showMapFeaturesDialog();
                handled = true;
                break;
            case R.id.action_about:
                showAboutDialog();
                handled = true;
            default:
        }

        return handled;
    }

    public AnimationHelper getAnimationHelper() {
        return _animHelper;
    }

    private void showMapFeaturesDialog() {

        Dialog d = new MapFeaturesDialog(this);
        d.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return handleMenuItem(item);
        }
    }

    private void showSearchDialog() {

        DialogFragment f = new SearchDialog();
        f.show(getFragmentManager(), "OeMap Search Dialog");
    }

    @Override
    public void onStop() {

        OeLog.d("onStop: " + getMapName());
        super.onStop();
    }

    @Override
    public void onStart() {

        OeLog.d("onStart: " + getMapName());
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        OeLog.d("onResume: " + getMapName());

        _spaceNamesAdapter.registerDataSetObserver(_mapSpinnerObserver);
        updateMapNamesFromHistory();
        wakePresenceService();
        wakePresenceBroadcastService();
        registerReceiver(_presenceReceiver, _presenceReceiverFilter);

        boolean done = setupFromIntent();

        if (!done) {

            OeMapFragment f = getMapFrag();
            if (f != null) {
                String cname = getMapName();
                String fname = f.getName();
                if (!cname.equals(fname)) {
                    OeLog.w("onResume map fragment does not match current map name");
                    setMapFrag(cname);
                }
            }
        }
    }

    private boolean setupFromIntent() {
        Intent i = getIntent();
        if (i.getAction() != null && i.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = i.getData();
            if (uri != null) {
                String enc_mapname = uri.getQueryParameter("map");
                if (enc_mapname != null) {

                    String mapname = null;
                    try {
                        mapname = URLDecoder.decode(enc_mapname, "UTF8");

                        //check for name in spacedb, if there, call set frag
                        //else call new map dialog
                        SpaceHelper h = new SpaceHelper(this);
                        OeLog.d("ejs looking for " + mapname);
                        SpaceHelper.Space s = h.getSpace(mapname);
                        if (s == null) {
                            showNewSpaceDialogWithName(mapname);
                        } else {
                            setMapFrag(mapname);
                        }

                        return true;
                    } catch (UnsupportedEncodingException e) {
                        OeLog.e(e);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onPause() {

        _spaceNamesAdapter.unregisterDataSetObserver(_mapSpinnerObserver);
        OeLog.d("onPause: " + getMapName());
        unregisterReceiver(_presenceReceiver);
        saveHistory();
        clearMapNamesHistory();
        super.onPause();
    }

    public String getMapName() {
        return _prefs.get(getString(R.string.state_current_mapname), getString(R.string.null_map_name));
    }

    private void setMapName(String newname) {
        _prefs.replace(getString(R.string.state_current_mapname), newname);
    }

    private void saveHistory() {
        try {
            _history_store.deleteAll();
            int sz = _drawerNamesList.size();
            for (int i = SEPARATOR_POS + 1; i < sz && i < MAX_HISTORY; i++) {
                _history_store.insert(_drawerNamesList.get(i));
            }
        } catch (SQLException ex) {
            OeLog.w(ex.toString(), ex);
        }
    }

    private void showRejoinDialog(final String space) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("You have left map '" + space + "'");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you wish to rejoin the map?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showNewSpaceDialogWithName(space);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showAboutDialog() {

        Dialog d = new AboutDialog(this);
        d.show();

    }

    private void showClearHistoryDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);

        // set title
        builder.setTitle("Clear Map Name History");

        // set dialog message
        builder.setCancelable(true);
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _history_store.deleteAll();
                int sz = _drawerList.getCount();
                while (_drawerNamesList.size() > SEPARATOR_POS + 1) {

                    _drawerNamesList.remove(SEPARATOR_POS + 1);
                }
                notifyDrawList();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // create alert dialog
        final AlertDialog d = builder.create();

        d.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    d.dismiss();
                }
                return true;
            }
        });

        // show it
        d.show();
    }

    private class DrawerAdapter extends ArrayAdapter {

        public DrawerAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int pos) {

            switch (pos) {
                //case SHARE_MAP_POS:
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
            if (position != SEPARATOR_POS) //ejs skip separator
                selectItem(position);
        }
    }

    private class QuitSpaceReceiver extends BroadcastReceiver {

        private boolean _loc_is_init = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            String uid = intent.getExtras().getString(OeMapPresenceService.KEY_UID);

            if (uid != null) {
                //this is not a space delete, bail
                return;
            }

            String spacename = intent.getExtras().getString(OeMapPresenceService.KEY_SPACENAME);
            OeLog.d("QuitSpaceReceiver.onReceive: " + spacename);

            OeMapFragment f = getMapFrag();
            String currSpace = f.getName();

            if (spacename != null && spacename.equals(currSpace)) {
                setMapFrag(getString(R.string.null_map_name));
                showRejoinDialog(spacename);
            }
        }
    }

    private class DrawerItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            if (position <= SEPARATOR_POS) return false;
            showClearHistoryDialog();

            return false;
        }
    }
}

