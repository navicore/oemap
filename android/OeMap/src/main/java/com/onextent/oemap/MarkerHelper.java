/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.onextent.android.map.LatLngInterpolator;
import com.onextent.android.map.MarkerAnimation;
import com.onextent.oemap.presence.Presence;

import java.util.HashMap;
import java.util.Map;

public class MarkerHelper {

    private Marker _myMarker;

    private final Map<String, Holder> _markers;
    private final Map<Marker, InfoWindowHolder> _info;

    private final GoogleMap _map;

    public MarkerHelper(GoogleMap map) {
        _map = map;
        _markers = new HashMap<String, Holder>();
        _info = new HashMap<Marker, InfoWindowHolder>();
    }

    public void setMyMarker(LatLng l) {

        if (_myMarker != null) _myMarker.setPosition(l);
    }

    private GoogleMap getMap() {
        return _map;
    }

    public Map<String, Holder> getMarkers() {
         return _markers;
    }
    public Map<Marker, InfoWindowHolder> getInfoHolders() {
        return _info;
    }

    public void clearMarkers() {
        for (Holder h : _markers.values()) {
            h.marker.remove();
        }
        _markers.clear();
        _info.clear();
    }

    public static class InfoWindowHolder {

        public boolean showingInfo = false;
        //todo: add sound and image helpers

        InfoWindowHolder() {
        }
    }

    public static class Holder {
        public final long time;
        public final Marker marker;
        public final String pid;

        public boolean showingInfo = false;

        Holder(Marker m, String pid) {
            this.pid = pid;
            marker = m;
            time = System.currentTimeMillis();
        }
    }

    public Marker removeMarker(Presence p, AnimationType animation) {

        Holder h = _markers.remove(p.getPID());
        if (h == null) return null;
        _info.remove(h.marker);

        if (animation == AnimationType.MOVE) {

            animateMarkerRemoval(p, h.marker);

        } else {

            h.marker.remove();
        }

        return h.marker;
    }

    public static enum AnimationType {NONE, BOUNCE, MOVE};
    public Marker setMarker(Presence p, AnimationType animation, boolean isMine) {

        if (p.getTimeToLive() == Presence.NONE) {

            return removeMarker(p, animation);

        } else {

            return updateMarker(p, animation, isMine);
        }
    }

    private Marker updateMarker(Presence p, AnimationType animation, boolean isMine) {

        final GoogleMap map = getMap();
        if (map == null) return null;

        Holder h = null;
        h = _markers.get(p.getPID());

        boolean isNew = false;

        if (h == null) {
            float color;
            if (isMine) {
                color = BitmapDescriptorFactory.HUE_GREEN;
            } else {
                color = BitmapDescriptorFactory.HUE_BLUE;
            }
            Marker m = map.addMarker(new MarkerOptions()
                    .position(getEdgeOfMap(p))
                    .icon(BitmapDescriptorFactory.defaultMarker(color)
                    ));
            h = new Holder(m, p.getPID());
            _markers.put(p.getPID(), h);
            _info.put(m, new InfoWindowHolder());
            isNew = true;
            if (isMine) {
                _myMarker = m;
            }
        }

        h.marker.setTitle(p.getLabel());
        h.marker.setSnippet(p.getSnippet());

        if (isNew && animation == AnimationType.MOVE) {
            animateNewMarkerPos(map, h.marker, p.getLocation());
        } else if (animation == AnimationType.BOUNCE) {
            h.marker.setPosition(p.getLocation());
            MarkerAnimation.bounceMarker(map, h.marker, null);
        } else {
            h.marker.setPosition(p.getLocation());
        }

        return h.marker;
    }

    private void animateNewMarkerPos(final GoogleMap map, final Marker marker, LatLng position) {

        //animate move into visible map and bounce at the end
        MarkerAnimation.animateMarkerToICS(marker, position,
                new LatLngInterpolator.Spherical(), new Runnable() {
            @Override
            public void run() {

                MarkerAnimation.bounceMarker(map, marker, null);
            }
        });
    }

    private LatLng getEdgeOfMap(Marker m) {
        GoogleMap map = getMap();
        if (map == null) return null;
        LatLngBounds curScreen = map.getProjection().getVisibleRegion().latLngBounds;

        //return curScreen.northeast;
        //return new LatLng(p.getLocation().latitude, curScreen.northeast.longitude); //from the right
        return new LatLng(curScreen.northeast.latitude, m.getPosition().longitude); //from the top
    }
    private LatLng getEdgeOfMap(Presence p) {
        GoogleMap map = getMap();
        if (map == null) return null;
        LatLngBounds curScreen = map.getProjection().getVisibleRegion().latLngBounds;

        //return curScreen.northeast;
        //return new LatLng(p.getLocation().latitude, curScreen.northeast.longitude); //from the right
        return new LatLng(curScreen.northeast.latitude, p.getLocation().longitude); //from the top
    }

    private void animateMarkerRemoval(final Presence p, final Marker m) {

        MarkerAnimation.bounceMarker(getMap(), m, new Runnable() {

            @Override
            public void run() {

                LatLng l;
                LatLng pos = p.getLocation();
                if (pos == null)
                    l = getEdgeOfMap(m);
                else
                    l = getEdgeOfMap(p);
                MarkerAnimation.animateMarkerToICS(m, l,
                        new LatLngInterpolator.Spherical(), new Runnable() {

                    @Override
                    public void run() {

                        m.remove();
                    }
                });
            }
        });
    }
}

