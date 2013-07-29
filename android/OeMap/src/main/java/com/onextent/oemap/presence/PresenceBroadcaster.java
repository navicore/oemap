package com.onextent.oemap.presence;

import com.onextent.android.activity.OeLifeCycle;

import java.util.Set;

public interface PresenceBroadcaster extends OeLifeCycle {

    //set map names that we to be part of
    Set<String> getMapNames();

    //listen for changes to device user's presence, usually
    // the map fragment is listening
    void setListener(PresenceListener l);
}

