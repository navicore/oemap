package com.onextent.oemap.presence;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.location.LocationHelper;
import com.onextent.oemap.R;

import java.util.HashSet;
import java.util.Set;

public abstract class BasePresenceBroadcaster implements PresenceBroadcaster {

    protected final OeBaseActivity  activity;
    protected final Set<String>     mapNames;

    private PresenceListener        listener;
    private LocationHelper          mLocHelper;
    private Presence                currentPresence = null;

    public BasePresenceBroadcaster(OeBaseActivity activity) {
        this.activity = activity;
        this.mapNames = new HashSet<String>();
    }

    @Override
    public Set<String> getMapNames() {
        return mapNames;
    }

    @Override
    public void setListener(PresenceListener l) {

        listener = l;
        if (currentPresence != null && l != null)
            l.onPresenceUpdate(currentPresence);
    }

    abstract protected void broadcast(Presence l);

    private void broadcast(Location l) {

        LatLng latLng       = new LatLng(l.getLatitude(), l.getLongitude());
        String pid          = activity.id();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
        String label        = p.getString(activity.getString(R.string.pref_username), "nobody");
        String snippit      = p.getString(activity.getString(R.string.pref_snippit), "sigh...");
        currentPresence     = PresenceFactory.createPresence(pid, latLng, label, snippit, null); //template

        if (listener != null)
            listener.onPresenceUpdate(currentPresence);

        for (String m : mapNames) {
            currentPresence.setSpaceName(m);
            broadcast(currentPresence);
        }
    }

    @Override
    public void onCreate() {

        mLocHelper = new LocationHelper(new LocationHelper.LHContext() {
            @Override
            public Activity getActivity() {
                return activity;
            }
            @Override
            public void updateLocation(Location l) {
                broadcast(l);
            }
        });
        mLocHelper.onCreate();

    }

    @Override
    public void onResume() {
        mLocHelper.onResume();
    }

    @Override
    public void onPause() {
        mLocHelper.onPause();
    }

    @Override
    public void onStart() {
        mLocHelper.onStart();
    }

    @Override
    public void onStop() {
        mLocHelper.onStop();
    }
}
