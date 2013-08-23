package com.onextent.oemap;

import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.onextent.oemap.provider.SpaceHelper;

import java.text.SimpleDateFormat;

public class NewSpaceDialog extends BaseSpaceSettingsDialog {

    private EditText mEditText;

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
        View view = inflater.inflate(R.layout.new_space_dialog, container);
        setTitle();

        setupNameEdit(view);
        setupButton(view);
        setupSeekBar(view);

        return view;
    }

    private void setupSeekBar(View view) {

        SeekBar seek = (SeekBar) view.findViewById(R.id.quitTimeSeekBar);
        final EditText time = (EditText) view.findViewById(R.id.quitTime);
        seek.setProgress(DEFAULT_PROGRESS);
        setQuitDate(DEFAULT_PROGRESS);
        time.setText(progressMsg);
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

                if (quiteDate != null) {

                    time.setText(_sdf.format(quiteDate));

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
                    SpaceHelper.Space oldSpace = h.getSpace(name);
                    if (oldSpace != null) {
                        h.deleteSpacename(name);
                    }
                    h.insert(name, quiteDate);
                    activity.onFinishNewSpaceDialog(mEditText.getText().toString());
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

