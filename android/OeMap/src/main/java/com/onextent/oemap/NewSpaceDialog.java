/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.provider.SpaceProvider;
import com.onextent.oemap.settings.BaseSpaceSettingsDialog;

public class NewSpaceDialog extends BaseSpaceSettingsDialog {

    private EditText mEditText;
    private int _max_points = SpaceHelper.PRESENCE_PARAM_DEFAULT_MAX_COUNT;
    private KvHelper _prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_space_dialog, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        _prefs = new KvHelper(getActivity());
        int type = getTypeArg();
        if (type == -1) {

            setupPublicCheckBox(view);
            setupSharableCheckBox(view);
        } else {
            CheckBox isSharCheckBox = (CheckBox) view.findViewById(R.id.space_settings_resharable);
            CheckBox isPublicBox = (CheckBox) view.findViewById(R.id.space_settings_public);
            switch (type) {
                case SpaceProvider.Spaces.PUBLIC:
                    setPublic(true);
                    setSharable(true);

                    isPublicBox.setChecked(true);
                    isSharCheckBox.setChecked(true);
                    isSharCheckBox.setVisibility(View.INVISIBLE);
                    break;
                case SpaceProvider.Spaces.PRIVATE:
                    setPublic(false);
                    isPublicBox.setChecked(false);

                    setSharable(false);
                    isSharCheckBox.setChecked(false);
                    isSharCheckBox.setVisibility(View.VISIBLE);
                    break;
                default:
                    setPublic(false);
                    isPublicBox.setChecked(false);

                    setSharable(true);
                    isSharCheckBox.setChecked(true);
                    isSharCheckBox.setVisibility(View.VISIBLE);
                    break;
            }
            isSharCheckBox.setEnabled(false);
            isPublicBox.setEnabled(false);
        }
        setupNameEdit(view);
        setupButton(view);
        setupSeekBar(view);
        //setupMaxPresenceSeekBar(view);

        return view;
    }

    private boolean isPublic() {
        return _prefs.getBoolean("prefs_new_map_is_public", false);
    }
    private void setPublic(boolean v) {
        _prefs.replaceBoolean("prefs_new_map_is_public", v);
    }
    private boolean isSharable() {
        return _prefs.getBoolean("prefs_new_map_is_sharable", true);
    }
    private void setSharable(boolean v) {
        _prefs.replaceBoolean("prefs_new_map_is_sharable", v);
    }

    private void setupPublicCheckBox(final View view) {

        CheckBox cb = (CheckBox) view.findViewById(R.id.space_settings_public);
        cb.setChecked(isPublic()); //init from last use
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                CheckBox isSharCheckBox = (CheckBox) view.findViewById(R.id.space_settings_resharable);
                if (isChecked) {
                    isSharCheckBox.setVisibility(View.INVISIBLE);
                    setPublic(true);
                } else {
                    isSharCheckBox.setVisibility(View.VISIBLE);
                    setPublic(false);
                }
            }
        });
    }

    private void setupSharableCheckBox(final View view) {

        CheckBox cb = (CheckBox) view.findViewById(R.id.space_settings_resharable);
        if (isPublic()) {
            cb.setChecked(true); //all public maps are sharable
            cb.setVisibility(View.INVISIBLE);
        } else {
            cb.setVisibility(View.VISIBLE);
            cb.setChecked(isSharable()); //init from last use
        }
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                setSharable(isChecked);
            }
        });
    }

    /*

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
     */

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

                    time.setText(DateUtils.getRelativeDateTimeString(getActivity(),
                            _quitDate.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
                    //time.setText(_sdf.format(_quitDate));

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

    private int getType(boolean isPublic, boolean isSharable) {
        if (!isPublic) {
            if (isSharable) {
                return SpaceProvider.Spaces.PRIVATE_SHARABLE;
            } else {
                return SpaceProvider.Spaces.PRIVATE;
            }
        } else { //all public maps are sharable
            return SpaceProvider.Spaces.PUBLIC;
        }
    }

    private int getTypeArg() {

        Bundle b = getArguments();
        if (b != null && b.containsKey(getString(R.string.bundle_type))) {
            return b.getInt(getString(R.string.bundle_type));
        }
        return -1;
    }
    private String getUriArg() {

        Bundle b = getArguments();
        if (b != null && b.containsKey(getString(R.string.bundle_uri))) {
            return b.getString(getString(R.string.bundle_uri));
        }
        return null;
    }
    private String getSidArg() {

        Bundle b = getArguments();
        if (b != null && b.containsKey(getString(R.string.bundle_sid))) {
            return b.getString(getString(R.string.bundle_sid));
        }
        return null;
    }

    private void setupButton(View view) {

        Button b = (Button) view.findViewById(R.id.join_space_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OeMapActivity activity = (OeMapActivity) getActivity();
                String name = mEditText.getText().toString();

                int type = getType(isPublic(), isSharable());
                String sid = getSidArg();
                String uriString = getUriArg();
                if (sid == null) {
                    if (type == SpaceProvider.Spaces.PUBLIC)
                        sid = name.toLowerCase(); //i18n
                    else
                        sid = activity.createUUID();
                }

                if (name != null && name.length() > 0) {

                    SpaceHelper h = new SpaceHelper(getActivity());
                    SpaceHelper.Space s = h.getSpace(name);
                    if (s == null) {
                        s = new SpaceHelper.Space(_quitDate, sid, name,
                                SpaceHelper.PRESENCE_PARAM_DEFAULT_DIST, _max_points, type, uriString);
                        h.insert(s);
                    } else {
                        h.deleteSpacename(name);
                        s.setLease(_quitDate);
                        s.setMaxPoints(_max_points);
                        h.insert(s);
                    }
                    activity.onFinishNewSpaceDialog(sid, name);
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
}

