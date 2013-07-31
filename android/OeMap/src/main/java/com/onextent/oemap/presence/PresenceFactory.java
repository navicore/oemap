package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.oemap.presence.rest.RestPresenceBroadcaster;

public class PresenceFactory {

    public static Presence createPresence(String pid, LatLng l, String lbl, String snippet, String spacename) {

        return new JsonPresence(pid, l, lbl, snippet, spacename);
    }

    public static PresenceBroadcaster createBroadcaster(OeBaseActivity a) {

        return new RestPresenceBroadcaster(a);
    }
}

