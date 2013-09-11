/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class DirectionsDialog extends DialogFragment {

    Directions md = new Directions();

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.directions_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        OeMapActivity a = (OeMapActivity) getActivity();
        Document doc = a.getMapFrag().getDirectionsDoc();

        TextView dist_text = (TextView) view.findViewById(R.id.distance_text);
        dist_text.setText(md.getDistanceText(doc));
        TextView dur_text = (TextView) view.findViewById(R.id.duration_text);
        dur_text.setText(md.getDurationText(doc));

        final ArrayList<String> directions = md.getDirectionText(doc);
        ListView list = (ListView) view.findViewById(R.id.step_by_step_text);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, directions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;

                if (null == convertView) {
                    row = inflater.inflate(android.R.layout.simple_list_item_1, null);
                } else {
                    row = convertView;
                }

                TextView tv = (TextView) row.findViewById(android.R.id.text1);
                tv.setText(Html.fromHtml(getItem(position)));
                //tv.setText(getItem(position));

                return row;
            }

        };
        list.setAdapter(adapter);

        return view;
    }
}
