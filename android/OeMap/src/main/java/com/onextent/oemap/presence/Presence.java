package com.onextent.oemap.presence;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface Presence {

    public static int SHORT = 0;
    public static int MEDIUM = 1;
    public static int LONG = 1;

    public LatLng getLocation();

    public String getUID();

    public String getLabel();

    public String getSnippet();

    public String getSpaceName();
    //public void setSpaceName(String name);

    public long getAgeInMillis();

    public int getTimeToLive();
}

