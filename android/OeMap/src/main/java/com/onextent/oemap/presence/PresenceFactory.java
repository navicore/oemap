package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class PresenceFactory {

    public static Presence createPresence(String uid, LatLng l,
                                          String lbl, String snippet,
                                          String spacename, int ttl,
                                          Date lease) throws PresenceException {

        return new JsonPresence(uid, l, lbl, snippet, spacename, ttl, lease);
    }

    public static Presence createPresence(JSONObject jobj) throws PresenceException {
        return new JsonPresence(jobj);
    }

    public static Presence createPresence(String uid, String spacename) throws PresenceException {
        return new JsonPresence(uid, spacename);
    }

    public static Presence createPresence(String json) throws PresenceException {
        try {
            return new JsonPresence(json);
        } catch (JSONException e) {
            throw new PresenceException(e);
        }
    }
}

