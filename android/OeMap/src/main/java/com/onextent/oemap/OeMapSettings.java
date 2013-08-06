package com.onextent.oemap;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class OeMapSettings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(getString(R.string.onextent_prefs_key));
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}

