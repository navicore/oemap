/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceException;
import com.onextent.oemap.presence.PresenceFactory;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.PresenceHelper;

import org.json.JSONException;
import org.w3c.dom.Document;

import java.util.List;

public class OeMapFragment extends MapFragment {

    private PresenceHelper      _presenceHelper = null;
    private KvHelper            _prefs = null;
    private LatLng              _currLoc;
    private boolean _mapIsInit  = false;
    private OeMapActivity       _home;
    private BroadcastReceiver   _presenceReceiver = new PresenceReceiver();
    private IntentFilter        _presenceReceiverFilter = null;
    private MarkerHelper        _markerHelper;
    private Document directionsDoc;

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
        //settings.setMyLocationButtonEnabled(true);

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
            mName = args.getString(getString(R.string.bundle_sid));
        return mName;
    }

    public boolean isMyPresence(Presence p) {

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

        _markerHelper.clearMarkers();

        String spacename = getName();
        try {
            List<Presence> presences = _presenceHelper.getAllPrecenses(spacename);
            if (presences != null) {
                for (Presence p : presences) {
                    _markerHelper.setMarker(p, MarkerHelper.AnimationType.NONE, isMyPresence(p));
                }
            }
        } catch (JSONException e) {
            OeLog.e("loadMarkers error: " + e, e);
        } catch (PresenceException e) {
            OeLog.e("loadMarkers error: " + e, e);
        }
    }

    /*
    private void unsetPointOfInterest() {
        _prefs.delete("pref_poi_lat");
        _prefs.delete("pref_poi_lon");
    }
    private void setPointOfInterest(LatLng l) {

        _prefs.replaceDouble("pref_poi_lat", l.latitude);
        _prefs.replaceDouble("pref_poi_lon", l.longitude);
        getMap().animateCamera(CameraUpdateFactory.newLatLng(l));
    }
     */

    private void initMapTouchListeners() {

        getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                getDirections(marker);
            }
        });

        getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                OeLog.d("set point of interest to: " + latLng);
                //setPointOfInterest(latLng);
            }
        });

        /*
        getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                OeMapActivity a = (OeMapActivity) getActivity();
                a.showMarkerDialog();
            }
        });
        */

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MarkerHelper.InfoWindowHolder ih = _markerHelper.getInfoHolders().get(marker);
                if (ih != null && ih.showingInfo) {
                    //note: android bug, the marker won't know if it is showing info window
                    // so we never end up here if we don't keep track of it ourselves
                    marker.hideInfoWindow();
                    ih.showingInfo = false;
                } else {
                    marker.showInfoWindow();
                    ih.showingInfo = true;
                }
                return true;
            }
        });

        getMap().setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                setLocation();
                return true;
            }
        });
    }


    private void getDirections(Marker marker) {

        new DirectionTask(this).execute(marker.getPosition(), _currLoc);
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        _markerHelper = new MarkerHelper(getMap());
        setMapOptions();
        loadMarkers();
        getActivity().registerReceiver(_presenceReceiver, _presenceReceiverFilter);

        initMapTouchListeners();

        OeMapActivity a = (OeMapActivity) getActivity();
        a.wakePresenceService();
        a.wakePresenceBroadcastService(); //temp ejs todo: make servcie smart about ttl and distance
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onPause() {

        //unsetPointOfInterest();

        getActivity().unregisterReceiver(_presenceReceiver);
        _markerHelper.clearMarkers();

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
        }

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

    public void setLocation(Presence p) {

        GoogleMap map = getMap();
        if (map == null) return;

        map.animateCamera(CameraUpdateFactory.newLatLng(p.getLocation()));
    }

    /*
    private boolean allMarkersAreVisible() {
        GoogleMap m = getMap();
        if (m != null)
            for (Holder h : _markers.values()) {
                if (!m.getProjection().getVisibleRegion().latLngBounds.contains(h.marker.getPosition()))
                    return false;
            }
        return true;
    }
     */

    public boolean setLocation() {
        return setLocation(true);
    }

    public boolean setLocation(boolean tryAutoZoom) {

        if (_currLoc == null) return false;

        GoogleMap map = getMap();
        if (map == null) return false;

        boolean autozoom = _prefs.getBoolean(getString(R.string.pref_autozoom), true);

        if (tryAutoZoom && autozoom && !_markerHelper.getMarkers().isEmpty()) {

            LatLngBounds.Builder bc = new LatLngBounds.Builder();
            for (MarkerHelper.Holder h : _markerHelper.getMarkers().values()) {
                bc.include(h.marker.getPosition());
            }
            map.animateCamera(CameraUpdateFactory.zoomTo(1));
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
            //if (!allMarkersAreVisible()) { //this code seems to undo the above instead of just shift
            //    map.moveCamera(CameraUpdateFactory.newLatLng(_currLoc));
            //}

        } else {
            map.animateCamera(CameraUpdateFactory.newLatLng(_currLoc));
        }

        map.setMyLocationEnabled(true);

        _markerHelper.setMyMarker(_currLoc);

        if (!_mapIsInit) {
            _mapIsInit = true;
            setMapOptions();
        }
        return true;
    }

    public MarkerHelper get_markerHelper() {
        return _markerHelper;
    }

    public Document getDirectionsDoc() {
        return directionsDoc;
    }

    public void setDirectionsDoc(Document directionsDoc) {
        this.directionsDoc = directionsDoc;
    }

    private class PresenceReceiver extends BroadcastReceiver {

        private boolean _loc_is_init = false;

        @Override
        public void onReceive(Context context, Intent intent) {

            String mName = getName();
            String uid = intent.getExtras().getString(OeMapPresenceService.KEY_UID);

            if (uid != null) {
                //this is an update
                String spacename = intent.getExtras().getString(OeMapPresenceService.KEY_SPACE_ID);

                if (mName != null && mName.equals(spacename)) {
                    try {
                        Presence p = _presenceHelper.getPresence(uid, spacename);
                        if (p == null) {
                            _markerHelper.removeMarker(PresenceFactory.createPresence(uid, spacename), MarkerHelper.AnimationType.MOVE);
                        } else {
                            if (isMyPresence(p)) {
                                _currLoc = p.getLocation();
                                if (!_loc_is_init) {
                                    _loc_is_init = true;//set map the first time we get a loc
                                    setLocation(false);
                                }
                            }
                            _markerHelper.setMarker(p, MarkerHelper.AnimationType.MOVE, isMyPresence(p));
                        }
                    } catch (JSONException e) {
                        OeLog.e("PresenceReceiver.onReceive error: " + e, e);
                    } catch (PresenceException e) {
                        OeLog.e("PresenceReceiver.onReceive error: " + e, e);
                    }
                }

                final OeMapActivity a = (OeMapActivity) getActivity();
                a.getAnimationHelper().completeRefresh();
            }
        }
    }
}

