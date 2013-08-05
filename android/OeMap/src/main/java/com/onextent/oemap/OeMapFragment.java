package com.onextent.oemap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.onextent.oemap.presence.PresenceDbHelper;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OeMapFragment extends MapFragment
        implements  SharedPreferences.OnSharedPreferenceChangeListener
{
    private Map<String, Holder> _markers = new HashMap<String, Holder>();
    private PresenceDbHelper _dbHelper = null;
    private LatLng mCurrLoc;
    private Marker mMyMarker;

    private String KEY_SPACENAME   = null;
    private String KEY_UID         = null;

    private boolean mMapIsInit = false;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefEdit;

    private OeMapActivity home;

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
        double lat = (double) mPrefs.getFloat(getString(R.string.state_lat), 0);
        double lng = (double) mPrefs.getFloat(getString(R.string.state_lng), 0);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    private String getName() {

        String mName = null;
        Bundle args = getArguments();
        if (args != null)
            mName = args.getString(getString(R.string.bundle_mapname));
        return mName;
    }

    private BroadcastReceiver _presenceReceiver = new PresenceReceiver();
    private IntentFilter _presenceReceiverFilter = null;
    private class PresenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String mName = getName();
            String uid = intent.getExtras().getString(KEY_UID);
            String spacename = intent.getExtras().getString(KEY_SPACENAME);
            if (mName != null && mName.equals(spacename)) {
                OeLog.d("PresenceReceiver.onReceive updating current map with uid: " + uid + " spacename: " + spacename );
                try {
                    Presence p = _dbHelper.get(uid, spacename);
                    if (p == null) {
                        OeLog.w("PresenceReceiver.onReceive presence not found: " + intent);
                    } else {
                        if (isMyPresence(p)) {
                            OeLog.d("PresenceReceiver.onReceive presence is my own");
                            mCurrLoc = p.getLocation();
                        }
                        setMarker(p);
                    }
                } catch (JSONException e) {
                    OeLog.e("PresenceReceiver.onReceive error: " + e, e);
                }
            }
        }
    }

    private boolean isMyPresence(Presence p) {

        return (home.id().equals(p.getUID()));
    }


    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onStop() {

        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    private void loadMarkers() {

        String spacename = getName();
        try {
            Set<Presence> markers = _dbHelper.getAll(spacename);
            if (markers != null)
            for (Presence p : markers) {
                setMarker(p);
            }
        } catch (JSONException e) {
            OeLog.e("loadMarkers error: " + e, e);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        loadMarkers();
        getActivity().registerReceiver(_presenceReceiver, _presenceReceiverFilter);
    }

    @Override
    public void onPause() {

        GoogleMap m = getMap();
        if (m != null) {

            float zoom = m.getCameraPosition().zoom;
            mPrefEdit.putFloat(getString(R.string.state_zoom_level), zoom);
            mPrefEdit.putFloat(getString(R.string.state_lat), (float) m.getCameraPosition().target.latitude);
            mPrefEdit.putFloat(getString(R.string.state_lng), (float) m.getCameraPosition().target.longitude);
            mPrefEdit.commit();
        }
        getActivity().unregisterReceiver(_presenceReceiver);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _dbHelper = new PresenceDbHelper(getActivity(), getString(R.string.presence_db_name));
        KEY_UID = getString(R.string.presence_service_key_uid);
        KEY_SPACENAME = getString(R.string.presence_service_key_spacename);
        home.setMapFragTag(getTag());

        mPrefs.registerOnSharedPreferenceChangeListener(this);

        _presenceReceiverFilter = new IntentFilter(getString(R.string.presence_service_update_intent));
        getActivity().registerReceiver(_presenceReceiver, _presenceReceiverFilter);
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

    private void setMyMarker() {

        if (mCurrLoc == null) return;
        if (mMyMarker == null) return;
        mMyMarker.setPosition(mCurrLoc);
    }

    private class Holder {
        public final long time;
        public final Marker marker;

        Holder(Marker m) {
            marker = m;
            time = System.currentTimeMillis();
        }
    }

    private void setMarker(Presence p) {

        boolean isMine = isMyPresence(p);
        GoogleMap map = getMap();
        if (map == null) return;

        Holder h = null;
        h = _markers.get(p.getUID());

        if (h == null) {
            float color;
            if (isMine) {
                color = BitmapDescriptorFactory.HUE_GREEN;
            } else {
                color = BitmapDescriptorFactory.HUE_BLUE;
            }
            Marker m = map.addMarker(new MarkerOptions()
                    .position(p.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .title(p.getLabel())
                    .snippet(p.getSnippet()));
            h = new Holder(m);
            _markers.put(p.getUID(), h);
            if (isMine) {
                mMyMarker = m;
            }
        } else {
            h.marker.setPosition(p.getLocation());
            h.marker.setTitle(p.getLabel());
            h.marker.setSnippet(p.getSnippet());
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

        if (s.startsWith("pref_"))  {
            setMapOptions();
        }
    }
}

