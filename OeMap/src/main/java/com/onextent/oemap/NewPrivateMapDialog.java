package com.onextent.oemap;

import android.app.Dialog;

public class NewPrivateMapDialog extends NewMapDialog {
    @Override
    void setTitle() {
        setTitle(getString(R.string.new_priv_map_dialog_title));
    }
}

