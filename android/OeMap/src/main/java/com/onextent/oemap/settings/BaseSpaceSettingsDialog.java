/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.settings;

import android.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.SpaceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseSpaceSettingsDialog extends DialogFragment {

    protected static final int DEFAULT_PROGRESS = 34;
    protected static final int MAX_PROGRESS = 100;

    protected static final SimpleDateFormat _sdf =
            new SimpleDateFormat("MMMMM dd, hh:mm aaa");

    protected Date quiteDate = null;
    protected String progressMsg = null;

    protected int dateToProgress(Date date) {

        //ejs todo:
        // subtract date from now
        // if diff < 1 hour do {}
        // if diff < 1 day do {}
        // if diff < 2 weeks do {}
        // else 100
        return DEFAULT_PROGRESS;
    }

    protected void setSeekBarProgress(SeekBar seek, String space) {
        SpaceHelper h = new SpaceHelper(getActivity());
        Date l = h.getLease(space);
        if (l == null) {
            seek.setProgress(MAX_PROGRESS);
        } else {
            seek.setProgress(dateToProgress(l));
        }
    }

    protected void setEditTextDate(EditText time, String space) {

        SpaceHelper h = new SpaceHelper(getActivity());
        Date l = h.getLease(space);
        if (l == null) {
            OeLog.d("null lease");
            setQuitDate(MAX_PROGRESS);
            time.setText(progressMsg);
        } else {
            OeLog.d("found lease: " + _sdf.format(l));
            //setQuitDate(MAX_PROGRESS);
            time.setText(_sdf.format(l));
        }
    }

    protected void setQuitDate(int progress) {
        // 0 - 29 is minutes to 1 hour
        // 30 - 69 is 1 hours to 1 day
        // 70 - 99 is 1 day to 2 weeks
        // 100 is wait until I quit
        Date now = new Date();
        if (progress < 30) {

            float pct = ((float) progress) / 30;
            int minutes = (int) (pct * 60);
            progressMsg = minutes + " minutes";
            quiteDate = new Date(System.currentTimeMillis() + (minutes * 60 * 1000));

        } else if (progress < 70) {

            float pct = ((float) progress - 29) / 40;
            int hours = (int) (pct * 24);
            if (hours == 0) hours = 1;
            progressMsg = hours + " hours";
            quiteDate = new Date(System.currentTimeMillis() + (hours * 60 * 60 * 1000));

        } else if (progress < 99) {

            float pct = ((float) progress - 69) / 30;
            int days = (int) (pct * 14);
            if (days == 0) days = 1;
            progressMsg = days + " days";
            quiteDate = new Date(System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000));

        } else {
            progressMsg = "until I quit";
            quiteDate = null;
        }
    }

    private static final int DEFAULT_MAX_PRESENCE_PROGRESS = SpaceHelper.PRESENCE_PARAM_DEFAULT_MAX_COUNT;
    private int _max = DEFAULT_MAX_PRESENCE_PROGRESS;
    private String _max_txt = _max + " people";

    protected interface ProgressCallback {
        void setProgress(int progress);
    }
    protected void setMaxChangeListener(final EditText maxText, SeekBar seek, int progress, final ProgressCallback cb) {

        seek.setProgress(progress);
        setMax(progress);
        maxText.setText(_max_txt);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setMax(i);
                if (_max_txt != null) {

                    maxText.setText(_max_txt);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                maxText.setText(_max_txt);

                cb.setProgress(_max);
            }
        });
    }

    private void setMax(int i) {
        _max = i;
        _max_txt = _max + " people";
    }

}
