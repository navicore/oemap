/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.android.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class AnimationHelper {

    private final Activity _activity;
    private final int _anim_id, _action_view_id;
    private MenuItem _refreshItem;
    private static long REFRESH_MIN_DUR = 1000;
    private Handler mHandler = new Handler();

    public AnimationHelper(Activity a, int action_view_id, int anim_id) {
        _activity = a;
        _anim_id = anim_id;
        _action_view_id = action_view_id;
    }

    /* Attach a rotating ImageView to the refresh item as an ActionView */
    public void refresh(MenuItem item) {

        LayoutInflater inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(_action_view_id, null);

        Animation rotation = AnimationUtils.loadAnimation(_activity, _anim_id);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        item.setActionView(iv);
        _refreshItem = item;
    }

    public void completeRefresh() {
        if (_refreshItem == null) return;
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (_refreshItem == null) return;
                View v = _refreshItem.getActionView();
                if (v != null) {
                    v.clearAnimation();
                }
                _refreshItem.setActionView(null);
                _refreshItem = null;
            }
        }, REFRESH_MIN_DUR);
    }
}

