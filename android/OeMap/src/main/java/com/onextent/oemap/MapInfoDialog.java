/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.provider.SpaceProvider;

import java.util.Date;

public class MapInfoDialog extends DialogFragment {

    private Uri _uri;
    private OeMapActivity _activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _activity = (OeMapActivity) activity;
    }

    private String getTypeText(int type) {

        switch (type) {
            case SpaceProvider.Spaces.PUBLIC:
                return "Public";
            case SpaceProvider.Spaces.PRIVATE:
                return "Private";
            default:
                return "Private Re-Shareable";
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.map_info_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Bundle b = getArguments();
        if (b == null) throw new NullPointerException("no args");
        String uri_str = b.getString("bundle_uri_string");
        _uri = Uri.parse(uri_str);

        if (_uri != null) {
            initByUri(view);
        } else {

            SpaceHelper h = new SpaceHelper(_activity);
            KvHelper prefs = new KvHelper(_activity);

            TextView typeEdit = (TextView) view.findViewById(R.id.map_info_map_type);
            String sid = _activity.getCurrentSpaceId();
            SpaceHelper.Space s = h.getSpace(sid);
            int type = s.getType();
            String typeTxt = getTypeText(type);
            typeEdit.setText(typeTxt);

            TextView mapnameEdit = (TextView) view.findViewById(R.id.map_info_mapname);
            String name = _activity.getSpaceName(_activity.getCurrentSpaceId());
            mapnameEdit.setText(name);

            TextView sharedByEdit = (TextView) view.findViewById(R.id.map_info_shareby);
            String username = "You";
            sharedByEdit.setText(username);

            TextView shareDateEdit = (TextView) view.findViewById(R.id.map_info_sharedate);
            Date datetime = new Date();
            if (datetime != null)
                shareDateEdit.setText(datetime.toString());
        }
        return view;
    }

    private void initByUri(View d) {

        TextView typeEdit = (TextView) d.findViewById(R.id.map_info_map_type);
        int type = Integer.valueOf(_uri.getQueryParameter(OeMapActivity.URI_PARAM_TYPE));
        String typeTxt = getTypeText(type);
        typeEdit.setText(typeTxt);

        TextView mapnameEdit = (TextView) d.findViewById(R.id.map_info_mapname);
        String name = decode(_uri.getQueryParameter(OeMapActivity.URI_PARAM_NAME));
        mapnameEdit.setText(name);

        TextView sharedByEdit = (TextView) d.findViewById(R.id.map_info_shareby);
        String username = decode(_uri.getQueryParameter(OeMapActivity.URI_PARAM_USERNAME));
        sharedByEdit.setText(username);

        TextView shareDateEdit = (TextView) d.findViewById(R.id.map_info_sharedate);
        String datetime = decode(_uri.getQueryParameter(OeMapActivity.URI_PARAM_DATETIME));
        if (datetime != null)
            shareDateEdit.setText(datetime);
    }

    private String decode(String id) {

        return _activity.decode(id);
    }
}

