package com.onextent.oemap;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class NewMapDialog extends DialogFragment implements TextView.OnEditorActionListener {

    private EditText mEditText;

    public NewMapDialog() {
        // Empty constructor required for DialogFragment
    }

    void setTitle() {
        setTitle(getString(R.string.new_map_dialog_title));
    }
    void setTitle(String t) {
        Dialog d = getDialog();
        if (d != null) d.setTitle(t);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_map_dialog, container);
        mEditText = (EditText) view.findViewById(R.id.txt_map_name);
        setTitle();

        // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            OeMapActivity activity = (OeMapActivity) getActivity();
            activity.onFinishNewMapDialog(mEditText.getText().toString());
            this.dismiss();
            return true;
        }

        return false;
    }
}

