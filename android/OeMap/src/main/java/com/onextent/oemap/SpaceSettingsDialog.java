package com.onextent.oemap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.onextent.oemap.provider.SpaceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SpaceSettingsDialog extends BaseSpaceSettingsDialog {

    private String _space = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null)
            _space = args.getString(getString(R.string.bundle_spacename));
        //if (space != null) {
        //    Dialog d = getDialog();
        //    if (d != null) d.setTitle(space + " quite time");
        //}
        View view = inflater.inflate(R.layout.space_settings_dialog, container);

        setupSeekBar(view);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onPause() {
        //ejs todo: save lease
        super.onPause();
    }

    private void setupSeekBar(View view) {

        SeekBar seek = (SeekBar) view.findViewById(R.id.space_settings_quitTimeSeekBar);
        final EditText time = (EditText) view.findViewById(R.id.space_settings_quitTime);
        //ejs todo: look up quit time and try to guess the progress value

        seek.setProgress(DEFAULT_PROGRESS);

        setEditTextDate(time, _space);
        setSeekBarProgress(seek, _space);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setQuitDate(i);
                if (progressMsg != null) {

                    time.setText(progressMsg);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                SpaceHelper h = new SpaceHelper(getActivity());
                h.deleteSpacename(_space);
                if (quiteDate != null) {

                    //time.setText(_sdf.format(quiteDate));
                    time.setText(DateUtils.getRelativeDateTimeString(getActivity(),
                            quiteDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
                    h.insert(_space, quiteDate);

                } else {

                    time.setText("Until I manually quit");
                    h.insert(_space, null);
                }
            }
        });
    }
}

