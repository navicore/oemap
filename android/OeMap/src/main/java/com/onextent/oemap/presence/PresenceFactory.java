package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;

public class PresenceFactory {

    public static Presence createPresence(String pid, LatLng l, String lbl, String snippet, String spacename) {

        return new JsonPresence(pid, l, lbl, snippet, spacename);
    }
}

