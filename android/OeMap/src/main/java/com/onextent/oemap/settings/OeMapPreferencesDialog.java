/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.KvHelper;

public class OeMapPreferencesDialog extends DialogFragment {

    private KvHelper _prefs;
    private View _view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        _prefs = new KvHelper(getActivity());

        Dialog d = getDialog();
        if (d != null) d.setTitle(getString(R.string.app_name) + " Preferences");
        _view = inflater.inflate(R.layout.preferences_dialog_layout, container, false);
        try {

            String label = _prefs.get(getString(R.string.pref_username), "nobody");
            EditText labelEdit = (EditText) _view.findViewById(R.id.pref_username);
            labelEdit.setText(label);

            String snippit = _prefs.get(getString(R.string.pref_snippit), "");
            EditText snippitEdit = (EditText) _view.findViewById(R.id.pref_snippit);
            snippitEdit.setText(snippit);

        } catch (Exception e) {
            OeLog.e(e.toString(), e);
        }

        return _view;
    }

    @Override
    public void onPause() {

        EditText edit = (EditText) _view.findViewById(R.id.pref_username);
        String s = edit.getText().toString();
        _prefs.replace(getString(R.string.pref_username), s);

        edit = (EditText) _view.findViewById(R.id.pref_snippit);
        s = edit.getText().toString();
        _prefs.replace(getString(R.string.pref_snippit), s);

        OeMapActivity a = (OeMapActivity) getActivity();
        a.wakePresenceBroadcastService();

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}

