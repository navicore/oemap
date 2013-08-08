package com.onextent.oemap.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.onextent.android.util.KeyValueDbHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;

public class OeMapPreferencesDialog extends DialogFragment {

    private KeyValueDbHelper _prefs;
    private View _view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        _prefs = new KeyValueDbHelper(getActivity(), getString(R.string.app_key_value_store_name));

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

            //setShowTrafficUICb();
            //setShowInDoorsCb();

        } catch (Exception e) {
            OeLog.e(e.toString(), e);
        }

        return _view;
    }

    /*
    private void setShowInDoorsCb() {

        boolean showInDoors = _prefs.getBoolean(getString(R.string.pref_show_indoors), false);
        CheckBox showInDoorsCheckBox = (CheckBox) _view.findViewById(R.id.pref_show_indoors);
        showInDoorsCheckBox.setChecked(showInDoors);

        showInDoorsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                _prefs.replaceBoolean(getString(R.string.pref_show_indoors), b);
                OeMapActivity a = (OeMapActivity) getActivity();
                a.getMap().setIndoorEnabled(b);
            }
        });

    }

    private void setShowTrafficUICb() {

        boolean showTraffic = _prefs.getBoolean(getString(R.string.pref_show_traffic), false);
        CheckBox showTrafficCheckBox = (CheckBox) _view.findViewById(R.id.pref_show_traffic);
        showTrafficCheckBox.setChecked(showTraffic);

        showTrafficCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                _prefs.replaceBoolean(getString(R.string.pref_show_traffic), b);
                OeMapActivity a = (OeMapActivity) getActivity();
                a.getMap().setTrafficEnabled(b);
            }
        });
    }
    */

    @Override
    public void onDismiss(DialogInterface dialog) {

        EditText labelEdit = (EditText) _view.findViewById(R.id.pref_username);
        String label = labelEdit.getText().toString();
        _prefs.replace(getString(R.string.pref_username), label);

        EditText snippitEdit = (EditText) _view.findViewById(R.id.pref_snippit);
        String snippit = snippitEdit.getText().toString();
        _prefs.replace(getString(R.string.pref_snippit), snippit);

        super.onDismiss(dialog);
    }

    @Override
    public void onDestroyView() {
        _prefs.close();
        super.onDestroyView();
    }
}

