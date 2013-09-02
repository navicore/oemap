/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.presence;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface Presence {

    public static int NONE   = 0; //delete presence
    public static int SHORT  = 1; // 5 min
    public static int MEDIUM = 2; // 1 hour
    public static int LONG   = 3; // 1 day

    public LatLng getLocation();

    public String getPID();

    public String getUID();

    public String getLabel();

    public String getSnippet();

    public String getSpaceName();
    //public void setSpaceName(String name);

    public long getAgeInMillis();

    public int getTimeToLive();
}

