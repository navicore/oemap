package com.onextent.oemap.presence;

import java.util.List;

public interface PresenceReceiver {

    public interface Listener {

        public void setPresences(List<Presence> p);
    }

    public void setListener(Listener l);

    public String getSpaceName();

    public void start();

    public void stop();
}

