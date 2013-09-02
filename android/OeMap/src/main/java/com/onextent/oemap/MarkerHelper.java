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

    private final GoogleMap _map;

    public MarkerHelper(GoogleMap map) {
        _map = map;
        _markers = new HashMap<String, Holder>();
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

    public void clearMarkers() {
        for (Holder h : _markers.values()) {
            h.marker.remove();
        }
    }

    public static class Holder {
        public final long time;
        public final Marker marker;

        Holder(Marker m) {
            marker = m;
            time = System.currentTimeMillis();
        }
    }

    public void removeMarker(Presence p, boolean animate) {

        Holder h = _markers.remove(p.getUID());

        if (h != null) {

            if (animate) {

                animateMarkerRemoval(p, h.marker);

            } else {

                h.marker.remove();
            }
        }
    }

    public void setMarker(Presence p, boolean animate, boolean isme) {

        if (p.getTimeToLive() == Presence.NONE) {

            removeMarker(p, animate);

        } else {

            updateMarker(p, animate, isme);
        }
    }

    public Marker updateMarker(Presence p, boolean animate, boolean isMine) {

        final GoogleMap map = getMap();
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
                    .position(getEdgeOfMap(p))
                    .icon(BitmapDescriptorFactory.defaultMarker(color)
                    ));
            h = new Holder(m);
            _markers.put(p.getUID(), h);
            if (isMine) {
                _myMarker = m;
            }
        }

        h.marker.setTitle(p.getLabel());
        h.marker.setSnippet(p.getSnippet());

        if (animate)
            animateNewMarkerPos(map, h.marker, p.getLocation());
        else {
            h.marker.setPosition(p.getLocation());
            MarkerAnimation.bounceMarker(map, h.marker, null);
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

