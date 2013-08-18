package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

public class PresenceFactory {

    public static Presence createPresence(String pid, LatLng l, String lbl, String snippet, String spacename, long ttl) {

        return new JsonPresence(pid, l, lbl, snippet, spacename, ttl);
    }

    public static Presence createPresence(String json) throws JSONException {
        return new JsonPresence(json);
    }
}

