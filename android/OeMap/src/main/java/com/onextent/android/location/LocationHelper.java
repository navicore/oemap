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
    final LHContext context;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    private boolean mSharingLoc;

    public LocationHelper(LHContext c) {
        this.context = c;
    }

    public static interface LHContext {
        public Context getContext();
        public void updateLocation(Location l);
    }


    @Override
    public void onCreate() {

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(context.getContext(), this, this);
    }


    ///////////////////////////////
    // begin location aware impl //
    ///////////////////////////////


    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(context.getContext());
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

    @Override
    public void onConnected(Bundle bundle) {
        OeLog.d("LocationHelper.onConnected");
        startUpdates();
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

        if (mLocationClient == null) throw new NullPointerException("no loc client");
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();
    }

    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onLocationChanged(Location location) {

        context.updateLocation(location);
    }

    private void startUpdates() {

        Location currentLocation = mLocationClient.getLastLocation();

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {

        mLocationClient.removeLocationUpdates(this);
    }
}

