package com.onextent.oemap;

import android.app.Activity;
import android.os.Bundle;

public class OeMapSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new OeMapSettings())
                .commit();
    }
}

