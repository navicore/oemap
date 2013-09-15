/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;
import com.onextent.oemap.provider.KvHelper;

public class GlobalSettingsDialog extends Dialog {

    private static OeMapActivity _activity = null;

    private KvHelper _prefs;

    public GlobalSettingsDialog(Context context) {
        super(context);
        _activity = (OeMapActivity) context;
        _prefs = new KvHelper(context);
    }

    /**
     * Standard Android on create method that gets called when the activity initialized.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.global_settings_dialog);

        initTrafficCheckBox();
        initShowIndoorsCheckBox();
        initShowZoomControlsCheckBox();
        initAutoZoom();
        initMapTypeRadioButtons();
    }

    private void initMapTypeRadioButtons() {

        RadioGroup g = (RadioGroup) findViewById(R.id.pref_map_type);

        int t = _prefs.getInt(_activity.getString(R.string.pref_map_type), GoogleMap.MAP_TYPE_NORMAL);
        switch (t) {
            case (GoogleMap.MAP_TYPE_SATELLITE):
                g.check(R.id.pref_feature_satellite);
                break;
            case (GoogleMap.MAP_TYPE_TERRAIN):
                g.check(R.id.pref_feature_terrain);
                break;
            default:
                g.check(R.id.pref_feature_normal);
        }

        //g.check(id);
        g.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int t;

                switch (checkedId) {
                    case (R.id.pref_feature_normal):
                        t = GoogleMap.MAP_TYPE_NORMAL;
                        break;
                    case (R.id.pref_feature_satellite):
                        t = GoogleMap.MAP_TYPE_SATELLITE;
                        break;
                    case (R.id.pref_feature_terrain):
                        t = GoogleMap.MAP_TYPE_TERRAIN;
                        break;
                    default:
                        throw new IllegalArgumentException("unknown id");
                }

                getMap().setMapType(t);
                _prefs.replaceInt(_activity.getString(R.string.pref_map_type), t);
            }
        });
    }

    private void initShowZoomControlsCheckBox() {

        CheckBox cb = (CheckBox) findViewById(R.id.pref_show_zoom_controls);

        cb.setChecked(_prefs.getBoolean(_activity.getString(R.string.pref_show_zoom_ctl), true));

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setShowZoomControlOption(isChecked);
            }
        });
    }

    private void setShowZoomControlOption(boolean checked) {

        GoogleMap m = getMap();
        if (m != null) {
            m.getUiSettings().setZoomControlsEnabled(checked);
        }
        _prefs.replaceBoolean(_activity.getString(R.string.pref_show_zoom_ctl), checked);
    }

    private void initAutoZoom() {

        CheckBox cb = (CheckBox) findViewById(R.id.pref_autozoom);

        cb.setChecked(_prefs.getBoolean(_activity.getString(R.string.pref_autozoom), true));

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _prefs.replaceBoolean(_activity.getString(R.string.pref_autozoom), isChecked);
            }
        });
    }

    private void initShowIndoorsCheckBox() {

        CheckBox cb = (CheckBox) findViewById(R.id.pref_show_indoors);

        cb.setChecked(_prefs.getBoolean(_activity.getString(R.string.pref_show_indoors), false));

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setShowInDoorsOption(isChecked);
            }
        });
    }
    private void setShowInDoorsOption(boolean b) {

        GoogleMap m = getMap();
        if (m != null) {
            m.setIndoorEnabled(b);
        }
        _prefs.replaceBoolean(_activity.getString(R.string.pref_show_indoors), b);
    }


    private void initTrafficCheckBox() {

        CheckBox cb = (CheckBox) findViewById(R.id.pref_show_traffic);

        cb.setChecked(_prefs.getBoolean(_activity.getString(R.string.pref_show_traffic), false));

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setShowTrafficOption(isChecked);
            }
        });
    }
    private void setShowTrafficOption(boolean b) {

        GoogleMap m = getMap();
        if (m != null) {
            m.setTrafficEnabled(b);
        }
        _prefs.replaceBoolean(_activity.getString(R.string.pref_show_traffic), b);
    }


    private GoogleMap getMap() {
        return _activity.getMap();
    }
}

