/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.OeMapPresenceService;

import android.net.Uri;

public class OeMapAutostart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            Intent i =  new Intent(context, OeMapPresenceService.class);
            i.putExtra("reason", "boot");
            context.startService(i);

        } else {

            OeLog.w("unexpected intent: " + intent);
        }
    }
}

