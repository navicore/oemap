package com.onextent.oemap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class OeMapFragment extends MapFragment
        implements  SharedPreferences.OnSharedPreferenceChangeListener, GoogleMap.OnMapLongClickListener
{

    private LocationHelper mLocHelper;
    private boolean mMapIsInit = false;

    private static final String SHARED_PREFERENCES = "com.onextent.oemap.map.SHARED_PREFERENCES";
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefEdit;

    private OeMapActivity home;
    public OeMapFragment() {
        super();
    }


    public void onMapLongClick(LatLng latLng) {

        //ejs todo: get this out of here, use refresh icon on actionbar
        setLocation();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        home = (OeMapActivity) activity;

        mPrefs = activity.getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mPrefEdit = mPrefs.edit();
    }

    private void init() {

        GoogleMap map = getMap();
        if (map == null) {

            Log.w("ejs", "no map in init");
            return;
        }

        UiSettings settings = map.getUiSettings();

        settings.setAllGesturesEnabled(true);

        settings.setCompassEnabled(true);

        settings.setMyLocationButtonEnabled(true);

        map.setOnMapLongClickListener(this);

        float zoom = mPrefs.getFloat(getString(R.string.pref_zoom_level), 15);
        Log.d("ejs", "zoom restored as " + zoom);
        double lat = (double) mPrefs.getFloat(getString(R.string.pref_lat), 0);
        double lng = (double) mPrefs.getFloat(getString(R.string.pref_lng), 0);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @Override
    public void onPause() {

        mLocHelper.onPause();

        GoogleMap m = getMap();
        if (m != null) {

            float zoom = m.getCameraPosition().zoom;
            mPrefEdit.putFloat(home.getString(R.string.pref_zoom_level), zoom);
            Log.d("ejs", "zoom saved as " + zoom);
            mPrefEdit.putFloat(home.getString(R.string.pref_lat), (float) m.getCameraPosition().target.latitude);
            mPrefEdit.putFloat(home.getString(R.string.pref_lng), (float) m.getCameraPosition().target.longitude);
            mPrefEdit.commit();
        }
        super.onPause();
    }

    @Override
    public void onStop() {

        mLocHelper.onStop();

        super.onStop();
    }

    @Override
    public void onStart() {

        super.onStart();
        mLocHelper.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        mLocHelper.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        home.setMapFragTag(getTag());
        mLocHelper = new LocationHelper(new LocationHelper.LHContext() {
            @Override
            public Activity getActivity() {
                return home;
            }
            @Override
            public void updateLocation(Location l) {

                mCurrLoc = l;
                setMyMarker();
                if (!mMapIsInit) {
                    setLocation();
                }
            }
        });
        mLocHelper.onCreate(savedInstanceState);
    }

    private void setMapType() {

        try {

            int t = Integer.valueOf(mPrefs.getString(getString(R.string.pref_map_type), "0"));
            GoogleMap m = getMap();
            if (m != null) {
                switch (t) {
                    case 0:
                        m.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        m.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        m.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                }
            }
        } catch (Throwable err) {
            Log.e("ejs", err.toString(), err);
        }
    }

    private void setMapOptions() {
        setTrafficEnabled();
        setMapType();
        setIndoorsEnabled();
    }
    private void setIndoorsEnabled() {
        boolean show = mPrefs.getBoolean(getString(R.string.pref_show_indoors), false);
        GoogleMap m = getMap();
        if (m != null) {
            m.setIndoorEnabled(show);
        }
    }

    private void setTrafficEnabled() {
        boolean showTraffic = mPrefs.getBoolean(getString(R.string.pref_show_traffic), false);
        GoogleMap m = getMap();
        if (m != null) {
            m.setTrafficEnabled(showTraffic);
        }
    }

    private Location mCurrLoc;

    private Marker mMyMarker;

    private void setMyMarker() {

        if (mCurrLoc == null) return;

        GoogleMap map = getMap();
        if (map == null) return;

        LatLng latLng = new LatLng(mCurrLoc.getLatitude(), mCurrLoc.getLongitude());

        if (mMyMarker == null) {
            mMyMarker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))
                    .title("Me")
                    .snippet("Here I am."));
        } else {
            mMyMarker.setPosition(latLng);
        }

    }

    public void setLocation() {

        GoogleMap map = getMap();
        if (map == null) {

            Log.w("ejs", "Got location but NO MAP!!!");
            return;
        }

        LatLng latLng = new LatLng(mCurrLoc.getLatitude(), mCurrLoc.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        map.setMyLocationEnabled(true);

        setMyMarker();

        if (!mMapIsInit) {
            mMapIsInit = true;
            setMapOptions();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        setMapOptions();
    }
}

