package com.onextent.oemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onextent.oemap.presence.OeMapPresenceService;

public class OeMapServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            context.startService(new Intent(context, OeMapPresenceService.class));
        }
    }
}
