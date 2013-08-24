package com.onextent.oemap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceException;
import com.onextent.oemap.presence.PresenceFactory;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.PresenceHelper;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OeMapFragment extends MapFragment  {

    Map<String, Holder> _markers = new HashMap<String, Holder>();

    private PresenceHelper _presenceHelper = null;
    private KvHelper        _prefs = null;
    private LatLng          _currLoc;
    private Marker          _myMarker;

    private boolean _mapIsInit = false;

    private OeMapActivity _home;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _home = (OeMapActivity) activity;
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

        float zoom = _prefs.getFloat(getString(R.string.state_zoom_level), 15);
        double lat = (double) _prefs.getFloat(getString(R.string.state_lat), 0);
        double lng = (double) _prefs.getFloat(getString(R.string.state_lng), 0);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));

        boolean zoomCtl = _prefs.getBoolean(_home.getString(R.string.pref_show_zoom_ctl), true);
        settings.setZoomControlsEnabled(zoomCtl);
    }

    public String getName() {
        if (isDetached()) return null;

        String mName = null;
        Bundle args = getArguments();
        if (args != null)
            mName = args.getString(getString(R.string.bundle_spacename));
        return mName;
    }

    private BroadcastReceiver _presenceReceiver = new PresenceReceiver();
    private IntentFilter _presenceReceiverFilter = null;
    private class PresenceReceiver extends BroadcastReceiver {

        private boolean _loc_is_init = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            String mName = getName();
            String uid = intent.getExtras().getString(OeMapPresenceService.KEY_UID);

            if (uid == null) {
                //this is not an update, bail
                return;
            }

            String spacename = intent.getExtras().getString(OeMapPresenceService.KEY_SPACENAME);

            if (mName != null && mName.equals(spacename)) {
                try {
                    Presence p = _presenceHelper.getPresence(uid, spacename);
                    if (p == null) {
                        removeMarker(PresenceFactory.createPresence(uid, spacename));
                    } else {
                        if (isMyPresence(p)) {
                            _currLoc = p.getLocation();
                            if (!_loc_is_init) {
                                _loc_is_init = true;//set map the first time we get a loc
                                setLocation(false);
                            }
                        }
                        setMarker(p);
                    }
                } catch (JSONException e) {
                    OeLog.e("PresenceReceiver.onReceive error: " + e, e);
                } catch (PresenceException e) {
                    OeLog.e("PresenceReceiver.onReceive error: " + e, e);
                }
            }
        }
    }

    private boolean isMyPresence(Presence p) {

        return (_home.id().equals(p.getUID()));
    }


    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    private void loadMarkers() {

        /**
         *
         *
         * ejs todo:
         *
         * THE BUG IS that I can read them one by one when the bcast comes in
         * but the getAll isn't finding them just by map name alone.  Huh.
         *
         * need a map-only index?
         * need a map-only index?
         * need a map-only index?
         * need a map-only index?
         * need a map-only index?
         *
         * bug is only when you join multiple maps big hint for index fix
         * bug is only when you join multiple maps big hint for index fix
         * bug is only when you join multiple maps big hint for index fix
         * bug is only when you join multiple maps big hint for index fix
         *
         *
         *
         */

        String spacename = getName();
        try {
            List<Presence> markers = _presenceHelper.getAllPrecenses(spacename);
            if (markers != null) {
                for (Presence p : markers) {
                    setMarker(p);
                }
            }
        } catch (JSONException e) {
            OeLog.e("loadMarkers error: " + e, e);
        } catch (PresenceException e) {
            OeLog.e("loadMarkers error: " + e, e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        setMapOptions();
        loadMarkers();
        getActivity().registerReceiver(_presenceReceiver, _presenceReceiverFilter);
        getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                //_home.showLeaseDialog();
                _home.showMarkerDialog();
            }
        });
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onPause() {

        getActivity().unregisterReceiver(_presenceReceiver);

        GoogleMap m = getMap();
        if (m != null) {

            float zoom = m.getCameraPosition().zoom;
            _prefs.replaceFloat(getString(R.string.state_zoom_level), zoom);
            _prefs.replaceFloat(getString(R.string.state_lat), (float) m.getCameraPosition().target.latitude);
            _prefs.replaceFloat(getString(R.string.state_lng), (float) m.getCameraPosition().target.longitude);
        }
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _presenceHelper = new PresenceHelper(getActivity());
        _prefs = new KvHelper(getActivity());

        _home.setMapFragTag(getTag());

        _presenceReceiverFilter = new IntentFilter(getString(R.string.presence_service_update_intent));

    }

    private void setMapType() {

        try {

            int t = _prefs.getInt(getString(R.string.pref_map_type), GoogleMap.MAP_TYPE_NORMAL);
            GoogleMap m = getMap();
            if (m != null) {
                m.setMapType(t);
            }
        } catch (Throwable err) {
            OeLog.e(err.toString(), err);
        }
    }

    private void setMapOptions() {

        if (_currLoc == null) {
            //not yet
            float lng = _prefs.getFloat(getString(R.string.state_lng), 0);
            float lat = _prefs.getFloat(getString(R.string.state_lat), 0);
            _currLoc = new LatLng(lat, lng);
        } else {
            _currLoc = _currLoc;
        };

        setTrafficEnabled();
        setMapType();
        setIndoorsEnabled();
    }
    private void setIndoorsEnabled() {
        boolean show = _prefs.getBoolean(getString(R.string.pref_show_indoors), false);
        GoogleMap m = getMap();
        if (m != null) {
            m.setIndoorEnabled(show);
        }
    }

    private void setTrafficEnabled() {
        boolean showTraffic = _prefs.getBoolean(getString(R.string.pref_show_traffic), false);
        GoogleMap m = getMap();
        if (m != null) {
            m.setTrafficEnabled(showTraffic);
        }
    }

    private void setMyMarker() {

        if (_currLoc == null) return;
        if (_myMarker == null) return;
        _myMarker.setPosition(_currLoc);
    }

    public static class Holder {
        public final long time;
        public final Marker marker;

        Holder(Marker m) {
            marker = m;
            time = System.currentTimeMillis();
        }
    }

    private void removeMarker(Presence p) {
        Holder h = _markers.remove(p.getUID());
        if (h != null) {
            h.marker.remove();
        }
    }

    private void setMarker(Presence p) {
        if (p.getTimeToLive() == Presence.NONE) {

            removeMarker(p);

        } else {

            updateMarker(p);
        }
    }

    public Marker updateMarker(Presence p) {

        boolean isMine = isMyPresence(p);
        GoogleMap map = getMap();
        if (map == null) return null;

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
                _myMarker = m;
            }
        } else {
            h.marker.setPosition(p.getLocation());
            h.marker.setTitle(p.getLabel());
            h.marker.setSnippet(p.getSnippet());
        }
        return h.marker;
    }

    public void setLocation(Presence p) {

        GoogleMap map = getMap();
        if (map == null)  return;

        map.animateCamera(CameraUpdateFactory.newLatLng(p.getLocation()));
    }

    public boolean setLocation() {
        return setLocation(true);
    }

    private boolean allMarkersAreVisible() {
        GoogleMap m = getMap();
        if (m != null)
        for (Holder h : _markers.values()) {
            if (!m.getProjection().getVisibleRegion().latLngBounds.contains(h.marker.getPosition())) return false;
        }
        return true;
    }

    public boolean setLocation(boolean tryAutoZoom) {

        if (_currLoc == null) return false;

        GoogleMap map = getMap();
        if (map == null)  return false;

        boolean autozoom = _prefs.getBoolean(getString(R.string.pref_autozoom), true);
        if (tryAutoZoom && autozoom && !_markers.isEmpty()) {

            LatLngBounds.Builder bc = new LatLngBounds.Builder();
            for (Holder h : _markers.values()) {
                bc.include(h.marker.getPosition());
            }
            map.animateCamera(CameraUpdateFactory.zoomTo(1));
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(),50));
            //if (!allMarkersAreVisible()) { //this code seems to undo the above instead of just shift
            //    map.moveCamera(CameraUpdateFactory.newLatLng(_currLoc));
            //}

        } else {
            map.animateCamera(CameraUpdateFactory.newLatLng(_currLoc));
        }

        map.setMyLocationEnabled(true);

        setMyMarker();

        if (!_mapIsInit) {
            _mapIsInit = true;
            setMapOptions();
        }
        return true;
    }

    public Map<String, Holder> getMarkers() {
        return _markers;
    }
}

