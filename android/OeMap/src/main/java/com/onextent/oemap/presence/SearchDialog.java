/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.presence;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.KvHelper;

public class SearchDialog extends DialogFragment {

    private View _view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Dialog d = getDialog();
        if (d != null) d.setTitle(getString(R.string.app_name) + " Search");
        _view = inflater.inflate(R.layout.search_dialog_layout, container, false);
        try {


        } catch (Exception e) {
            OeLog.e(e.toString(), e);
        }

        return _view;
    }
}

