/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.presence;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface Presence {

    //ttl values
    public static int NONE   = 0; //delete presence
    public static int SHORT  = 1; // 5 min
    public static int MEDIUM = 2; // 1 hour
    public static int LONG   = 3; // 1 day

    static final long FIVE_MINUTES = 1000 * 60 * 5;
    static final long SHORT_IN_MILLIS = FIVE_MINUTES;
    static final long ONE_HOUR = 1000 * 60 * 60;
    static final long MEDIUM_IN_MILLIS = ONE_HOUR;
    static final long ONE_DAY = 24 * ONE_HOUR;
    static final long LONG_IN_MILLIS = ONE_DAY;

    public LatLng getLocation();

    public String getPID();

    public String getUID();

    public String getLabel();

    public String getSnippet();

    public String getSpaceName();

    public long getAgeInMillis();

    public int getTimeToLive();

    public static int PUSH_TYPE_NONE  = 0;
    public static int PUSH_TYPE_GCM   = 1; // gcm
    public static int PUSH_TYPE_APPLE = 2;

    public int getRemoteIdType();
    public int setRemoteIdType(int rid);
    public String getRemoteId();
    public void setRemoteid(String rid);

    public boolean isValid();
    public void resetCreateTime();
}

