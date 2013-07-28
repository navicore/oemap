package com.onextent.test.oemap;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceFactory;

import junit.framework.TestCase;

public class PresenceTestCase extends TestCase {

    protected Presence presence1;
    protected double fValue2;

    protected void setUp() {
       presence1 = PresenceFactory.createPresence("123", new LatLng(0,0), "my name", "my snippet", "mymap");
    }

    public void testP1() {
       assertEquals("my name", presence1.getLabel());
    }
}

