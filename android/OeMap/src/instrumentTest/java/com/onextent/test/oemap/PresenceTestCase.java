package com.onextent.test.oemap;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceFactory;

import junit.framework.TestCase;

public class PresenceTestCase extends TestCase {

    static final double LATITUDE  = 30.1;
    static final double LONGITUDE = 40.2;
    static final String PID       = "my presence id 123";
    static final String MAPNAME   = "my map";
    static final String LABEL     = "my name";
    static final String SNIPPIT   = "my snippit";
    static final String SPACENAME = "my space";

    protected Presence presence1;
    protected double fValue2;

    protected void setUp() {
       presence1 = PresenceFactory.createPresence(PID, new LatLng(LATITUDE,LONGITUDE), LABEL, SNIPPIT, SPACENAME);
    }

    public void testP1() {
        assertEquals(PID, presence1.getUID());
        assertEquals(LABEL, presence1.getLabel());
        assertEquals(SNIPPIT, presence1.getSnippet());
        assertEquals(SPACENAME, presence1.getSpaceName());
        assertEquals(new LatLng(LATITUDE,LONGITUDE), presence1.getLocation());
        assertFalse((new LatLng(LONGITUDE, LATITUDE)).equals(presence1.getLocation()));
    }
}

