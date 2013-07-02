package com.onexent.oemap;

import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OeMapActivity extends Activity {

    private boolean mSharingLoc;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle =  "na";
    private String mDrawerTitle = "na";
    private String[] mPlanetTitles;
    private ListView mDrawerList;
    private ArrayList mDrawerNamesList;

    private class DrawerAdapter extends ArrayAdapter {

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
        /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class OeMapFragment extends Fragment {
        public static final String ARG_DRAWER_ITEM_NUMBER = "menu_item_number";

        public OeMapFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_map, container, false);

            int i = getArguments().getInt(ARG_DRAWER_ITEM_NUMBER);
            String planet = getResources().getStringArray(R.array.menu_names_array)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(imageId);
            getActivity().setTitle(planet);

            //getActivity().setTitle("see me");
            return rootView;
        }
    }

    private void setSettingsFrag() {
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new OeMapSettings())
                .commit();
    }

    private void setMapFrag(int position) {

        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new OeMapFragment();
        Bundle args = new Bundle();
        args.putInt(OeMapFragment.ARG_DRAWER_ITEM_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
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

}
