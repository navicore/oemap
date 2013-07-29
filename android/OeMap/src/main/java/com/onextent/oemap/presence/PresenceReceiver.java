package com.onextent.oemap.presence;

import com.onextent.android.activity.OeLifeCycle;

public interface PresenceReceiver extends OeLifeCycle {

    public void setListener(PresenceListener l);
}

