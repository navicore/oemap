package com.onextent.oemap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

public class OeMapFragment extends MapFragment implements GoogleMap.OnMapLongClickListener {

    public static final String SHARED_PREFERENCES = "com.onextent.oemap.map.SHARED_PREFERENCES";
    SharedPreferences mPrefs;
    SharedPreferences.Editor mZoomPref;

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
        mZoomPref = mPrefs.edit();
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

        int zoom = mPrefs.getInt(getString(R.string.pref_zoom_level), 15);
        map.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @Override
    public void onPause() {

        GoogleMap m = getMap();
        if (m != null) {

            mZoomPref.putInt(home.getString(R.string.pref_zoom_level), (int) m.getCameraPosition().zoom);
            mZoomPref.commit();
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

