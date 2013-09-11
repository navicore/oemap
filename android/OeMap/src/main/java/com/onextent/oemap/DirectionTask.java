/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.onextent.android.util.OeLog;

import org.w3c.dom.Document;

import java.util.ArrayList;

class DirectionTask extends AsyncTask<LatLng, Void, Document> {

    static private Polyline _curLine;
    private OeMapFragment oeMapFragment;
    GoogleMap mMap;
    Directions md = new Directions();
    Document _doc;

    public DirectionTask(OeMapFragment f) {
        oeMapFragment = f;
        mMap = oeMapFragment.getMap();
    }

    public Document getDocument() {
        return _doc;
    }

    @Override
    protected void onPostExecute(Document doc) {
        super.onPostExecute(doc);
        _doc = doc;
        oeMapFragment.setDirectionsDoc(doc);
        if (_curLine != null) {
            _curLine.remove();
            _curLine = null;
        }
        ArrayList<LatLng> directionPoints = md.getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++) {
            rectLine.add(directionPoints.get(i));
        }

        _curLine = mMap.addPolyline(rectLine); //save to clear later


        FragmentManager fm = oeMapFragment.getActivity().getFragmentManager();
        DirectionsDialog d = new DirectionsDialog();
        d.show(fm, "directions dialog");
    }

    @Override
    protected Document doInBackground(LatLng... params) {
        LatLng toPosition = params[0];
        LatLng fromPosition = params[1];
        if (fromPosition == null || toPosition == null) {
            OeLog.e("can not get starting or ending point");
            return null;
        }

        Document doc = md.getDocument(fromPosition, toPosition, Directions.MODE_WALKING);
        if (doc == null) {
            OeLog.w("can not get directrions");
            return null;
        }
        return doc;
    }
}

