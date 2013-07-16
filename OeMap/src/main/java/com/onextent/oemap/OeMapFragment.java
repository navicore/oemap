package com.onextent.oemap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

public class OeMapFragment extends MapFragment implements GoogleMap.OnMapLongClickListener {

    public static final String SHARED_PREFERENCES = "com.onextent.oemap.map.SHARED_PREFERENCES";
    SharedPreferences mPrefs;
    SharedPreferences.Editor mPrefEdit;

    private OeMapActivity home;
    public OeMapFragment() {
        super();
    }


    public void onMapLongClick(LatLng latLng) {

        //todo: let this set the current location until some menu is clicked
        //todo: ultimate goal is to make is easy to add a place manually

        if (home != null) home.setLocation();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        home = (OeMapActivity) activity;

        mPrefs = activity.getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mPrefEdit = mPrefs.edit();
    }

    private void init() {

        GoogleMap map = getMap();
        if (map == null) {

            Log.w("ejs", "no map in init");
            return;
        }

        UiSettings settings = map.getUiSettings();

        settings.setAllGesturesEnabled(true);

        settings.setCompassEnabled(true);

        settings.setMyLocationButtonEnabled(true);

        map.setOnMapLongClickListener(this);

        float zoom = mPrefs.getFloat(getString(R.string.pref_zoom_level), 15);
        Log.d("ejs", "zoom restored as " + zoom);
        double lat = (double) mPrefs.getFloat(getString(R.string.pref_lat), 0);
        double lng = (double) mPrefs.getFloat(getString(R.string.pref_lng), 0);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @Override
    public void onPause() {

        GoogleMap m = getMap();
        if (m != null) {

            float zoom = m.getCameraPosition().zoom;
            mPrefEdit.putFloat(home.getString(R.string.pref_zoom_level), zoom);
            Log.d("ejs", "zoom saved as " + zoom);
            mPrefEdit.putFloat(home.getString(R.string.pref_lat), (float) m.getCameraPosition().target.latitude);
            mPrefEdit.putFloat(home.getString(R.string.pref_lng), (float) m.getCameraPosition().target.longitude);
            mPrefEdit.commit();
        }
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        home.setMapFragTag(getTag());
    }

    /*
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //GoogleMap map = getMap();
        //Location l = map.getMyLocation();
        //String loc = l.toString();
        //Log.d("ejs", loc);
        Log.d("ejs", "hiya");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        return rootView;
    }
    */
}

