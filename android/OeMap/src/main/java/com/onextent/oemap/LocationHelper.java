package com.onextent.oemap;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class LocationHelper implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener
{
    LHContext context;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    private boolean mSharingLoc;

    LocationHelper(LHContext c) {
        this.context = c;
    }

    public static interface LHContext {
        public Activity getActivity();
        public void updateLocation(Location l);
    }


    public void onCreate(Bundle savedInstanceState) {

        //location

        // Get handles to the UI view objects
        //mLatLng = (TextView) findViewById(R.id.lat_lng);
        //mAddress = (TextView) findViewById(R.id.address);
        //mActivityIndicator = (ProgressBar) findViewById(R.id.address_progress);
        //mConnectionState = (TextView) findViewById(R.id.text_connection_state);
        //mConnectionStatus = (TextView) findViewById(R.id.text_connection_status);

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(context.getActivity(), this, this);
    }

    /*

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Decide what to do based on the original request code
        switch (requestCode) {
            //...
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    break;
                }
        }
     }
    */


    ///////////////////////////////
    // begin location aware impl //
    ///////////////////////////////


    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(context.getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Get the error code
            //int errorCode = connectionResult.getErrorCode();
            int errorCode = resultCode; //ejs???
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    context.getActivity(),
                    LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(context.getActivity().getFragmentManager(), "Location Updates");
                //ejs todo set content pane?
            }
        }
        return false;

    }

    @Override
    public void onConnected(Bundle bundle) {
        startUpdates();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(context.getActivity(), "Disconnected from Location Service.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        context.getActivity(),
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
        public void getLocation(View v) {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Display the current location in the UI
            //mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
        }
    }

        private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            context.getActivity(),
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(context.getActivity().getFragmentManager(), "ejs");
            //ejs todo set content pane?
        }
    }

    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
    }
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    public void onPause() {

    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    public void onStart() {

        if (mLocationClient == null) throw new NullPointerException("no loc client");
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();
    }

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

