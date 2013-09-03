/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.graphics.Color;
import android.view.Window;
import android.widget.TextView;

public class AboutDialog extends Dialog {

    private static Context mContext = null;

    public AboutDialog(Context context) {
        super(context);
        mContext = context;
    }

    public static String readRawTextFile(int id) {
        InputStream inputStream = mContext.getResources().openRawResource(id);


        InputStreamReader in = new InputStreamReader(inputStream);
        BufferedReader buf = new BufferedReader(in);
        String line;


        StringBuilder text = new StringBuilder();
        try {
            while ((line = buf.readLine()) != null) text.append(line);
        } catch (IOException e) {
            return null;
        }


        return text.toString();
    }

    /**
     * Standard Android on create method that gets called when the activity initialized.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.about_dialog);

        TextView tv = (TextView) findViewById(R.id.legal_text);

        tv.setText(readRawTextFile(R.raw.legal));

        tv = (TextView) findViewById(R.id.info_text);

        tv.setText(Html.fromHtml(readRawTextFile(R.raw.info)));

        //tv.setLinkTextColor(Color.WHITE);

        Linkify.addLinks(tv, Linkify.ALL);
    }
}

