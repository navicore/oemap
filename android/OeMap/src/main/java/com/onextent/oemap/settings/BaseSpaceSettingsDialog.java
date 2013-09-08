/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.settings;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.EditText;
import android.widget.SeekBar;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.SpaceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseSpaceSettingsDialog extends DialogFragment {

    protected static final int DEFAULT_PROGRESS = 34;
    protected static final int MAX_PROGRESS = 100;
    protected static final int DEFAULT_MAX_PRESENCE_PROGRESS = SpaceHelper.PRESENCE_PARAM_DEFAULT_MAX_COUNT;

    //protected static final SimpleDateFormat _sdf = new SimpleDateFormat("MMMMM dd, hh:mm aaa");

    protected Date _quitDate = null;
    protected String _progressMsg = null;

    private String MSG_TXT_NEAREST_PEOPLE = null;
    private String MSG_TXT_NEAREST_PEOPLE_0 = null;
    private String MSG_TXT_NEAREST_PEOPLE_1 = null;

    private int _max = DEFAULT_MAX_PRESENCE_PROGRESS;
    private String _max_txt = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MSG_TXT_NEAREST_PEOPLE = getString(R.string.msg_format_nearest_people);
        MSG_TXT_NEAREST_PEOPLE_0 = getString(R.string.msg_format_nearest_people_0);
        MSG_TXT_NEAREST_PEOPLE_1 = getString(R.string.msg_format_nearest_people_1);
    }

    protected int dateToProgress(Date date) {

        final long HOUR = 60 * 60 * 1000;
        final long DAY = 24 * HOUR;
        final long TWO_WEEKS = 14 * DAY;

        // subtract now from date
        long dur = date.getTime() - System.currentTimeMillis();
        //todo: get exact progress within each segment
        if (dur < HOUR) {

            return 15;

        } else if (dur < DAY) {

            return 50;

        } else if (dur < TWO_WEEKS) {

            return 85;

        } else {

            return 100;
        }
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
            time.setText(_progressMsg);
        } else if (l.getTime() == Long.MAX_VALUE) {
            time.setText( getString(R.string.msg_max_lease_time) );
        } else {
            OeLog.d("found lease: " + l);
            //setQuitDate(MAX_PROGRESS);
            //time.setText(_sdf.format(l));
            time.setText(DateUtils.getRelativeDateTimeString(getActivity(),
                    l.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
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
            _progressMsg = minutes + " minutes";
            _quitDate = new Date(System.currentTimeMillis() + (minutes * 60 * 1000));

        } else if (progress < 70) {

            float pct = ((float) progress - 29) / 40;
            int hours = (int) (pct * 24);
            if (hours == 0) hours = 1;
            _progressMsg = hours + " hours";
            _quitDate = new Date(System.currentTimeMillis() + (hours * 60 * 60 * 1000));

        } else if (progress < 99) {

            float pct = ((float) progress - 69) / 30;
            int days = (int) (pct * 14);
            if (days == 0) days = 1;
            _progressMsg = days + " days";
            _quitDate = new Date(System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000));

        } else {
            _progressMsg = getString(R.string.msg_max_lease_time);
            _quitDate = null;
        }
    }

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
        switch (i) {
            case 0:
                _max_txt = MSG_TXT_NEAREST_PEOPLE_0;
                break;
            case 1:
                _max_txt = MSG_TXT_NEAREST_PEOPLE_1;
                break;
            default:
                _max_txt = String.format(MSG_TXT_NEAREST_PEOPLE, i);
        }
    }
}

