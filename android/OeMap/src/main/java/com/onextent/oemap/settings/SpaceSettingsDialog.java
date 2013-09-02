/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.settings;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.SpaceHelper;

public class SpaceSettingsDialog extends BaseSpaceSettingsDialog {

    private String _space = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null)
            _space = args.getString(getString(R.string.bundle_spacename));

        View view = inflater.inflate(R.layout.space_settings_dialog, container);

        setupQuitTimeSeekBar(view);
        setupMaxPresenceSeekBar(view);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setTitle(_space);

        OeMapActivity a = (OeMapActivity) getActivity();
        if (_space == null || (!_space.equals(a.getMapName()))) {
            OeLog.w("map name for dialog and active map do not match: " + _space + " vs " + a.getMapName()); //tmp todo: ejs
        }

        return view;
    }

    protected void setupMaxPresenceSeekBar(View view) {

        SeekBar seek = (SeekBar) view.findViewById(R.id.max_presence_edit_SeekBar);
        final EditText maxText = (EditText) view.findViewById(R.id.max_presence_edit_fld);
        final SpaceHelper h = new SpaceHelper(getActivity());
        final SpaceHelper.Space s = h.getSpace(_space);
        int max = SpaceHelper.PRESENCE_PARAM_DEFAULT_MAX_COUNT;
        if (s != null) {
            max = s.getMaxPoints();
        }
        ProgressCallback cb = new ProgressCallback() {
            @Override
            public void setProgress(int progress) {
                if (s != null) {
                    s.setMaxPoints(progress);
                    h.deleteSpacename(_space);
                    h.insert(s);
                    OeMapActivity a = (OeMapActivity) getActivity();
                    a.wakePresenceService();
                }
            }
        };
        setMaxChangeListener(maxText, seek, max, cb);
    }

    @Override
    public void onPause() {
        OeMapActivity a = (OeMapActivity) getActivity();
        super.onPause();
    }

    private void setupQuitTimeSeekBar(View view) {

        SeekBar seek = (SeekBar) view.findViewById(R.id.space_settings_quitTimeSeekBar);
        final EditText time = (EditText) view.findViewById(R.id.space_settings_quitTime);

        setEditTextDate(time, _space);
        setSeekBarProgress(seek, _space);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setQuitDate(i);
                if (_progressMsg != null) {

                    time.setText(_progressMsg);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                SpaceHelper h = new SpaceHelper(getActivity());
                h.deleteSpacename(_space);
                if (_quitDate != null) {

                    //time.setText(_sdf.format(_quitDate));
                    time.setText(DateUtils.getRelativeDateTimeString(getActivity(),
                            _quitDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
                    h.insert(_space, _quitDate);

                } else {

                    time.setText("Until I manually quit");
                    h.insert(_space, null);
                }
                OeMapActivity a = (OeMapActivity) getActivity();
                a.wakePresenceService();
            }
        });
    }
}

