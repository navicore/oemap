/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.settings.BaseSpaceSettingsDialog;

import java.io.UnsupportedEncodingException;

public class NewSpaceDialog extends BaseSpaceSettingsDialog {

    private EditText mEditText;
    private int _max_points = SpaceHelper.PRESENCE_PARAM_DEFAULT_MAX_COUNT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_space_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setupNameEdit(view);
        setupButton(view);
        setupSeekBar(view);
        setupMaxPresenceSeekBar(view);

        return view;
    }

    protected void setupMaxPresenceSeekBar(View view) {

        SeekBar seek = (SeekBar) view.findViewById(R.id.max_presence_SeekBar);
        final EditText maxText = (EditText) view.findViewById(R.id.max_presence_fld);
        ProgressCallback cb = new ProgressCallback() {
            @Override
            public void setProgress(int progress) {
                _max_points = progress;
            }
        };
        setMaxChangeListener(maxText, seek, SpaceHelper.PRESENCE_PARAM_DEFAULT_MAX_COUNT, cb);
    }

    private void setupSeekBar(View view) {

        SeekBar seek = (SeekBar) view.findViewById(R.id.quitTimeSeekBar);
        final EditText time = (EditText) view.findViewById(R.id.quitTime);
        seek.setProgress(DEFAULT_PROGRESS);
        setQuitDate(DEFAULT_PROGRESS);
        time.setText(_progressMsg);
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

                if (_quitDate != null) {

                    time.setText(_sdf.format(_quitDate));

                } else {

                    time.setText("Until I manually quit");
                }
            }
        });
    }

    private void setupNameEdit(View view) {
        mEditText = (EditText) view.findViewById(R.id.txt_map_name);

        mEditText.requestFocus();

        Bundle b = getArguments();
        if (b != null && b.containsKey(getString(R.string.bundle_spacename))) {
            String n = b.getString(getString(R.string.bundle_spacename));
            mEditText.setText(n);
        }
    }

    private void setupButton(View view) {

        Button b = (Button) view.findViewById(R.id.join_space_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OeMapActivity activity = (OeMapActivity) getActivity();
                String name = mEditText.getText().toString();
                if (name != null && name.length() > 0) {

                    SpaceHelper h = new SpaceHelper(getActivity());
                    SpaceHelper.Space s = h.getSpace(name);
                    if (s == null) {
                        try {
                            s = new SpaceHelper.Space(_quitDate, name,
                                    SpaceHelper.PRESENCE_PARAM_DEFAULT_DIST, _max_points);
                            h.insert(s);
                        } catch (UnsupportedEncodingException e) {
                            OeLog.e(e);
                        }
                    } else {
                        h.deleteSpacename(name);
                        s.setLease(_quitDate);
                        s.setMaxPoints(_max_points);
                        h.insert(s);
                    }
                    activity.onFinishNewSpaceDialog(name);
                    dismiss();
                } else {
                    //todo: this feedback is not working at all
                    Toast.makeText(getActivity(), "invalid map name", Toast.LENGTH_SHORT);
                    getDialog().getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
            }
        });
    }

    /*

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            OeMapActivity activity = (OeMapActivity) getActivity();
            activity.onFinishNewSpaceDialog(mEditText.getText().toString());
            //this.dismiss();
            return true;
        }

        return false;
    }
     */
}

