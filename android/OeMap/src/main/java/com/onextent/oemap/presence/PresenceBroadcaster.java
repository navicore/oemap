package com.onextent.oemap.presence;

import android.location.Location;

public interface PresenceBroadcaster {

    public void setLocation(Location l);

    public void setLabel(String lbl);

    public void setSnippet(String snip);

    public String getSpaceName();

    public void start();

    public void stop();

}

