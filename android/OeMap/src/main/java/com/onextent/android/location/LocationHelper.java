package com.onextent.android.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.onextent.android.activity.OeLifeCycle;
import com.onextent.android.util.OeLog;

public class LocationHelper implements
        OeLifeCycle,
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener
{
    final LHContext _context;

    // A request to connect to Location Services
    private LocationRequest _LocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient _locationClient;

    private boolean _sharingLoc;

    public LocationHelper(LHContext c) {
        this._context = c;
    }

    public static interface LHContext {
        public Context getContext();
        public void updateLocation(Location l);
    }


    @Override
    public void onCreate() {

        // Create a new global location parameters object
        _LocationRequest = LocationRequest.create();

        _LocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        _LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        _LocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        _locationClient = new LocationClient(_context.getContext(), this, this);
    }

    @Override
    public void onDestroy() {

    }


    ///////////////////////////////
    // begin location aware impl //
    ///////////////////////////////


    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(_context.getContext());
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates",
                    "Google Play services is available.");
            return true;
        // Google Play services was not available for some reason
        } else {
            OeLog.e("location services connect error: " + resultCode);
            //tddo: send error via intent
        }
        return false;

    }

    private Callback _connCallback = null;
    public void setCallback(Callback cb) {
        _connCallback = cb;
    }

    public interface Callback {
        void onConnected();
    }

    @Override
    public void onConnected(Bundle bundle) {
        OeLog.d("LocationHelper.onConnected");
        startUpdates();
        if (_connCallback != null)
            _connCallback.onConnected();
    }

    @Override
    public void onDisconnected() {
        OeLog.d("location services disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        OeLog.e("location services connect failed: " + connectionResult);
    }

    @Override
    public void onStart() {

        if (_locationClient == null) throw new NullPointerException("no loc client");
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        _locationClient.connect();
    }

    @Override
    public void onStop() {

        // If the client is connected
        if (_locationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        _locationClient.disconnect();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onLocationChanged(Location location) {

        _context.updateLocation(location);
    }

    public Location getLastLocation() {

        if (!_locationClient.isConnected()) return null;

        return _locationClient.getLastLocation();
    }

    private void startUpdates() {

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        _locationClient.requestLocationUpdates(_LocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {

        _locationClient.removeLocationUpdates(this);
    }
}

