package com.onextent.oemap;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by esweeney on 7/1/13.
 */
public class OeMapSettings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}

