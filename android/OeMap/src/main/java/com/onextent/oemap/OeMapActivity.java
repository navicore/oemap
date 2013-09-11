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
import com.onextent.android.activity.AboutDialog;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.ui.AnimationHelper;
import com.onextent.android.util.ListDbHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.presence.SearchDialog;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.provider.SpaceProvider;
import com.onextent.oemap.settings.OeMapPreferencesDialog;
import com.onextent.oemap.settings.SpaceSettingsDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OeMapActivity extends OeBaseActivity {

    static final String URI_PARAM_NAME = "name";
    static final String URI_PARAM_DATETIME = "date";
    static final String URI_PARAM_SID = "sid";
    static final String URI_PARAM_TYPE = "type";
    static final String URI_PARAM_USERNAME = "user";
    static final int NEW_PUBLIC_MAP = 0;
    static final int FIND_MAP_POS = 1;
    static final int QUIT_MAP_POS = 2;
    static final int MAP_INFO_POS = 3;
    static final int SEPARATOR_POS = 4;
    private static final String MAP_FRAG_TAG = "oemap";
    private static final int MAX_HISTORY = 20;
    private String MAP_SHARE_URL_BASE = null;
    private String OEMAP_INTENT_SUBJECT = null;
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
        String m = getCurrentSpaceId();
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

    public void setMapFrag(String sid, String name) {

        OeLog.d("setMapFrag: " + sid);
        MapFragment fragment = null;  //creating a new frag per map, optimise later

        fragment = new OeMapFragment();
        Bundle args = new Bundle();
        args.putString(getString(R.string.bundle_sid), sid);
        args.putString(getString(R.string.bundle_spacename), name);
        fragment.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(
                R.id.content_frame, fragment, MAP_FRAG_TAG
        ).commit();

        updateSpaceNames(sid);
        setShareIntent();
        setActionBarToCurrentMap();
    }

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
        String spacename = getCurrentSpaceId();
        args.putString(getString(R.string.bundle_spacename), spacename);
        d.setArguments(args);
        d.show(fm, "space_settings_dialog");
    }

    public void showMarkerDialog() {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new MarkerDialog();
        Bundle args = new Bundle();
        String spacename = getCurrentSpaceId();
        args.putString(getString(R.string.bundle_spacename), spacename);
        d.setArguments(args);
        d.show(fm, "list_markers_dialog");
    }

    private void showNewSharedSpaceDialog(Uri uri) {

        String name = decode(uri.getQueryParameter(URI_PARAM_NAME));
        String sid = decode(uri.getQueryParameter(URI_PARAM_SID));
        int type = Integer.valueOf(decode(uri.getQueryParameter(URI_PARAM_TYPE)));
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewSpaceDialog();
        if (name != null) {
            Bundle b = new Bundle();
            b.putString(getString(R.string.bundle_uri), uri.toString());
            b.putString(getString(R.string.bundle_spacename), name);
            b.putString(getString(R.string.bundle_sid), sid);
            b.putInt(getString(R.string.bundle_type), type);
            d.setArguments(b);
        }
        d.show(fm, "new_space_dialog");
    }

    private void showNewSpaceDialogWithName(String name) {
        FragmentManager fm = getFragmentManager();
        DialogFragment d = new NewSpaceDialog();
        if (name != null) {
            Bundle b = new Bundle();
            b.putString(getString(R.string.bundle_spacename), name);
            d.setArguments(b);
        }
        d.show(fm, "new_space_dialog");
    }

    private void showNewSpaceDialog() {
        showNewSpaceDialogWithName(null);
    }

    private String getSpaceId(String name) {

        String none = getString(R.string.null_map_name);
        if (none.equals(name)) return none;
        SpaceHelper h = new SpaceHelper(this);
        SpaceHelper.Space s = h.getSpaceByName(name);
        if (s == null) throw new NullPointerException("no space found for name " + name);
        return s.getId();
    }

    public String getSpaceName(String sid) {

        String none = getString(R.string.null_map_name);
        if (none.equals(sid)) return none;

        SpaceHelper h = new SpaceHelper(this);
        SpaceHelper.Space s = h.getSpace(sid);
        //if (s == null) throw new NullPointerException("no space found for sid " + sid);
        if (s == null) return null;

        return s.getName();
    }

    private void updateSpaceNames(String sid) {
        setCurrentSpaceId(sid);
        String n = getSpaceName(sid);
        _history_store.replace(n);

        if (_drawerNamesList.contains(n)) {
            _drawerNamesList.remove(n);
        }
        _drawerNamesList.add(SEPARATOR_POS + 1, n);
        notifyDrawList();
    }

    void enableNewSpace(String sid, String name) {
        OeLog.d("enableNewSpace:: " + sid);
        setMapFrag(sid, name);
        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_ADD_SPACE);
        i.putExtra(OeMapPresenceService.KEY_SPACE_ID, sid);
        startService(i);
    }

    void onFinishNewSpaceDialog(String sid, String name) {
        setCurrentSpaceId(sid);
        enableNewSpace(sid, name);
        String n = getSpaceName(sid);
        Toast.makeText(this, "New map '" + n + "' created.", Toast.LENGTH_SHORT).show();
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

        String sid = getCurrentSpaceId();
        String newname = getInactiveSpaceName(getSpaceName(sid));
        String newsid = getSpaceId(newname);
        setMapFrag(newsid, newname);
        //todo: helper method that sets a nice UI and location for none

        Intent i = new Intent(this, OeMapPresenceService.class);
        i.putExtra(OeMapPresenceService.KEY_REASON, OeMapPresenceService.CMD_RM_SPACE);
        i.putExtra(OeMapPresenceService.KEY_SPACE_ID, sid);
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
            case MAP_INFO_POS:
                showMapInfoDialog();
                break;
            default:
                String m = _drawerNamesList.get(position);
                SpaceHelper h = new SpaceHelper(this);
                SpaceHelper.Space space = h.getSpace(m);
                if (space == null) {
                    showNewSpaceDialogWithName(m);
                } else {
                    enableNewSpace(space.getId(), space.getName());
                }
                break;
        }

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(_drawerList);
    }

    private void showMapInfoDialog() {

        String sid = getCurrentSpaceId();
        SpaceHelper h = new SpaceHelper(this);
        SpaceHelper.Space s = h.getSpace(sid);
        Uri uri = null;
        String suri = s.getUri();
        if (suri != null)
            uri = Uri.parse(suri);

        FragmentManager fm = getFragmentManager();
        DialogFragment d = new MapInfoDialog(this, uri);
        d.show(fm, "directions dialog");
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
        OeLog.d("onDestroy: " + getCurrentSpaceId());
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
    public void onNewIntent(Intent i) {
        setupFromIntent(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        OEMAP_INTENT_SUBJECT = getString(R.string.app_name);
        MAP_SHARE_URL_BASE = getString(R.string.url_base_map_share);

        setContentView(R.layout.oe_map_activity);

        _animHelper = new AnimationHelper(this, R.layout.refresh_action_view, R.anim.clockwise_refresh);

        _prefs = new KvHelper(this);

        Intent i = getIntent();

        OeLog.d("onCreate: " + getCurrentSpaceId() + " with intent: " + i);

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
        String spaceId = getCurrentSpaceId();
        if (spaceId != null && !spaceId.equals("") && !spaceId.equals(getString(R.string.null_map_name))) {
            SpaceHelper h = new SpaceHelper(this);
            SpaceHelper.Space s = h.getSpace(spaceId);

            if (s == null) {

                OeLog.w("activeMapIsInStore not in store: " + getCurrentSpaceId());
                ret = false;

            } else if (s.getLease().getTime() < System.currentTimeMillis()) {

                //expired
                String msg = String.valueOf(DateUtils.getRelativeTimeSpanString(this, s.getLease().getTime()));
                OeLog.d("activeMapIsInStore expiring: " + getCurrentSpaceId() + " lease: " + msg);
                h.deleteSpacename(spaceId);
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
                String sid = getSpaceId(n);
                setMapFrag(sid, n);
                return true;
            }
        });
        setActionBarToCurrentMap();
    }

    private void setActionBarToCurrentMap() {

        boolean success = false;
        String sid = getCurrentSpaceId();
        if (sid == null) return;
        String name = getSpaceName(sid);
        for (int i = 0; i < _spaceNamesAdapter.getCount(); i++) {

            String n = (String) _spaceNamesAdapter.getItem(i);
            if (n != null && n.equals(name)) {

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

        String spaceId = getCurrentSpaceId();
        SpaceHelper h = new SpaceHelper(this);
        SpaceHelper.Space space = h.getSpace(spaceId);
        if (space == null) {
            OeLog.e("setShareIntent can not find space for sid: " + spaceId);
            return;
        }
        String spaceName = space.getName();
        String username = _prefs.get(getString(R.string.pref_username), "unknown");
        int type = space.getType();
        String enc_sid = encode(spaceId);
        String enc_name = encode(spaceName);
        String enc_username = encode(username);

        Date date = new Date();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, spaceName + " " + OEMAP_INTENT_SUBJECT);
        i.putExtra(Intent.EXTRA_TEXT, MAP_SHARE_URL_BASE + "?" + URI_PARAM_SID + "=" + enc_sid +
                "&" + URI_PARAM_NAME + "=" + enc_name + "&" + URI_PARAM_TYPE + "=" + type + "&" +
                URI_PARAM_USERNAME + "=" + enc_username + "&" + URI_PARAM_DATETIME + "=" + encode(date.toString()));

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

        _refreshMenuItem = menu.findItem(R.id.action_refresh);
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

        OeLog.d("onStop: " + getCurrentSpaceId());
        super.onStop();
    }

    @Override
    public void onStart() {

        OeLog.d("onStart: " + getCurrentSpaceId());
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        OeLog.d("onResume: " + getCurrentSpaceId());

        _spaceNamesAdapter.registerDataSetObserver(_mapSpinnerObserver);
        updateMapNamesFromHistory();
        wakePresenceService();
        wakePresenceBroadcastService();
        registerReceiver(_presenceReceiver, _presenceReceiverFilter);

        boolean done = setupFromIntent(getIntent());

        if (!done) {

            OeMapFragment f = getMapFrag();
            if (f != null) {
                String cname = getCurrentSpaceId();
                String fname = f.getName();
                if (!cname.equals(fname)) {
                    OeLog.w("onResume map fragment does not match current map name");
                    setMapFrag(cname, getSpaceName(cname));
                }
            }
        }
    }

    private boolean setupFromIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = intent.getData();
            if (uri != null) {
                String enc_name = uri.getQueryParameter(URI_PARAM_NAME);
                String enc_sid = uri.getQueryParameter(URI_PARAM_SID);
                if (enc_sid != null) {

                    String name = decode(enc_name);
                    String sid = decode(enc_sid);

                    //todo: check for differnet sids with the same name, in that chase
                    // number the names like "my map(2)" etc...

                    //check for name in spacedb, if there, call set frag
                    //else call new map dialog
                    SpaceHelper h = new SpaceHelper(this);
                    SpaceHelper.Space s = h.getSpace(sid);
                    if (s == null) {
                        showNewSharedSpaceDialog(uri);
                    } else {
                        setMapFrag(sid, name);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onPause() {

        _spaceNamesAdapter.unregisterDataSetObserver(_mapSpinnerObserver);
        OeLog.d("onPause: " + getCurrentSpaceId());
        unregisterReceiver(_presenceReceiver);
        saveHistory();
        clearMapNamesHistory();
        super.onPause();
    }

    public String getCurrentSpaceId() {
        return _prefs.get(getString(R.string.state_current_space_id), getString(R.string.null_map_name));
    }

    private void setCurrentSpaceId(String sid) {
        _prefs.replace(getString(R.string.state_current_space_id), sid);
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

    private MenuItem _refreshMenuItem = null;

    public void beginRefreshAnimation() {

        if (_refreshMenuItem != null)
            _animHelper.refresh(_refreshMenuItem);
    }
    public void completeRefreshAnimation() {

        _animHelper.completeRefresh();
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

            String spacename = intent.getExtras().getString(OeMapPresenceService.KEY_SPACE_ID);
            OeLog.d("QuitSpaceReceiver.onReceive: " + spacename);

            OeMapFragment f = getMapFrag();
            String currSpace = f.getName();

            if (spacename != null && spacename.equals(currSpace)) {
                setMapFrag(getString(R.string.null_map_name), getString(R.string.null_map_name));
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

