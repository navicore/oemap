package com.onextent.oemap.presence;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public interface Presence {

    public LatLng getLocation();

    public String getPID();

    public String getLabel();

    public String getSnippet();

    public String getSpaceName();
    public void setSpaceName(String name);

}

