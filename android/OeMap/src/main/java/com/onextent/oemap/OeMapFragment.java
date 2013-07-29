package com.onextent.oemap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceListener;

public class OeMapFragment extends MapFragment
        implements  SharedPreferences.OnSharedPreferenceChangeListener
{

    private String mName = null;

    private boolean mMapIsInit = false;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefEdit;

    private OeMapActivity home;
    public OeMapFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        home = (OeMapActivity) activity;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mPrefEdit = mPrefs.edit();
    }

    private void init() {

        GoogleMap map = getMap();
        if (map == null) {

            OeLog.w("no map in init");
            return;
        }

        UiSettings settings = map.getUiSettings();

        settings.setAllGesturesEnabled(true);

        settings.setCompassEnabled(true);

        settings.setMyLocationButtonEnabled(false);

        float zoom = mPrefs.getFloat(getString(R.string.state_zoom_level), 15);
        OeLog.d("zoom restored as " + zoom);
        double lat = (double) mPrefs.getFloat(getString(R.string.state_lat), 0);
        double lng = (double) mPrefs.getFloat(getString(R.string.state_lng), 0);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @Override
    public void onPause() {

        home.getPresenceBroadcaster().setListener(null);

        GoogleMap m = getMap();
        if (m != null) {

            float zoom = m.getCameraPosition().zoom;
            mPrefEdit.putFloat(getString(R.string.state_zoom_level), zoom);
            OeLog.d("zoom saved as " + zoom);
            mPrefEdit.putFloat(getString(R.string.state_lat), (float) m.getCameraPosition().target.latitude);
            mPrefEdit.putFloat(getString(R.string.state_lng), (float) m.getCameraPosition().target.longitude);
            mPrefEdit.commit();
        }
        super.onPause();
    }

    @Override
    public void onStop() {

        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        PresenceListener l = new PresenceListener() {
            @Override
            public void onPresenceUpdate(Presence p) {

                mCurrLoc = p.getLocation();
                setMyMarker();
                if (!mMapIsInit) {
                    setLocation();
                }
            }
        };
        home.getPresenceBroadcaster().setListener(l);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mName = savedInstanceState.getString(getString(R.string.bundle_mapname));
        home.setMapFragTag(getTag());

        mPrefs.registerOnSharedPreferenceChangeListener(this);
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
                        m.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 2:
                        m.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }
            }
        } catch (Throwable err) {
            OeLog.e(err.toString(), err);
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

    //private Location mCurrLoc;
    private LatLng mCurrLoc;

    private Marker mMyMarker;

    private void setMyMarker() {

        if (mCurrLoc == null) return;

        GoogleMap map = getMap();
        if (map == null) return;

        //LatLng latLng = new LatLng(mCurrLoc.getLatitude(), mCurrLoc.getLongitude());
        String uname = mPrefs.getString(getString(R.string.pref_username), "nobody");

        if (mMyMarker == null) {
            mMyMarker = map.addMarker(new MarkerOptions()
                    .position(mCurrLoc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(uname)
                    .snippet("Here I am."));
        } else {
            mMyMarker.setPosition(mCurrLoc);
        }

    }

    private void updateMyMarker() {
        if (mMyMarker != null) {
            String uname = mPrefs.getString(getString(R.string.pref_username), "nobody");
            OeLog.d("setting uname to " + uname);
            mMyMarker.setTitle(uname);
        }
    }

    public boolean setLocation() {

        if (mCurrLoc == null) return false; //not yet

        GoogleMap map = getMap();
        if (map == null)  return false;

        map.animateCamera(CameraUpdateFactory.newLatLng(mCurrLoc));
        map.setMyLocationEnabled(true);

        setMyMarker();

        if (!mMapIsInit) {
            mMapIsInit = true;
            setMapOptions();
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        OeLog.d(s);
        if (s.startsWith("pref_"))  {
            OeLog.d("..." + s);
            setMapOptions();
            updateMyMarker();
        }
    }
}

