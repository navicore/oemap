package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class PresenceFactory {

    public static Presence createPresence(String uid, LatLng l, String lbl, String snippet, String spacename, int ttl) {

        return new JsonPresence(uid, l, lbl, snippet, spacename, ttl);
    }

    public static Presence createPresence(JSONObject jobj) throws JSONException {
        return new JsonPresence(jobj);
    }
    public static Presence createPresence(String json) throws JSONException {
        return new JsonPresence(json);
    }
}

