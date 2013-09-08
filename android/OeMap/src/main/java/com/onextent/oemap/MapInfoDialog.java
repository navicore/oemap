/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.provider.SpaceProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

public class MapInfoDialog extends Dialog {

    private final Uri _uri;
    private final OeMapActivity _activity;

    public MapInfoDialog(OeMapActivity activity, Uri uri) {
        super(activity);
        _uri = uri;
        _activity = activity;
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

    /**
     * Standard Android on create method that gets called when the activity initialized.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.map_info_dialog);

        if (_uri != null) {
            initByUri();
        } else {

            SpaceHelper h = new SpaceHelper(_activity);
            KvHelper prefs = new KvHelper(_activity);

            TextView typeEdit = (TextView) findViewById(R.id.map_info_map_type);
            String sid = _activity.getCurrentSpaceId();
            SpaceHelper.Space s = h.getSpace(sid);
            int type = s.getType();
            String typeTxt = getTypeText(type);
            typeEdit.setText(typeTxt);

            TextView mapnameEdit = (TextView) findViewById(R.id.map_info_mapname);
            String name = _activity.getSpaceName(_activity.getCurrentSpaceId());
            mapnameEdit.setText(name);

            TextView sharedByEdit = (TextView) findViewById(R.id.map_info_shareby);
            String username = "You";
            sharedByEdit.setText(username);

            TextView shareDateEdit = (TextView) findViewById(R.id.map_info_sharedate);
            Date datetime = new Date();
            if (datetime != null)
                shareDateEdit.setText(datetime.toString());
        }
    }
    private void initByUri() {

        TextView typeEdit = (TextView) findViewById(R.id.map_info_map_type);
        int type = Integer.valueOf(_uri.getQueryParameter(OeMapActivity.URI_PARAM_TYPE));
        String typeTxt = getTypeText(type);
        typeEdit.setText(typeTxt);

        TextView mapnameEdit = (TextView) findViewById(R.id.map_info_mapname);
        String name = decode(_uri.getQueryParameter(OeMapActivity.URI_PARAM_NAME));
        mapnameEdit.setText(name);

        TextView sharedByEdit = (TextView) findViewById(R.id.map_info_shareby);
        String username = decode(_uri.getQueryParameter(OeMapActivity.URI_PARAM_USERNAME));
        sharedByEdit.setText(username);

        TextView shareDateEdit = (TextView) findViewById(R.id.map_info_sharedate);
        String datetime = decode(_uri.getQueryParameter(OeMapActivity.URI_PARAM_DATETIME));
        if (datetime != null)
            shareDateEdit.setText(datetime);
    }

    private String decode(String id) {

        return _activity.decode(id);
    }
}

