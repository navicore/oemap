/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap;

import android.R;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.provider.SpaceProvider;

import java.util.List;

public class SpaceNamesAdapter extends ArrayAdapter {

    private List<String> _names;
    private final ContentObserver _observer;
    private final Context _context;

    public SpaceNamesAdapter(Context context) {

        super(context,  android.R.layout.simple_spinner_item, android.R.id.text1);

        setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        _context = context;

        final SpaceHelper helper = new SpaceHelper(_context);
        _names = helper.getAllSpaceNames();

        ContentResolver resolver = _context.getContentResolver();
        _observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                _names = helper.getAllSpaceNames();
                notifyDataSetChanged();
                super.onChange(selfChange);
            }
        };
        resolver.registerContentObserver(SpaceProvider.CONTENT_URI, false, _observer);
    }

    public void onDestroy() {

        ContentResolver resolver = _context.getContentResolver();
        resolver.unregisterContentObserver(_observer);
    }

    @Override
    public int getCount() {
        return _names.size();
    }

    @Override
    public Object getItem(int i) {
        return _names.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}

