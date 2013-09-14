/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap.presence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.PresenceHelper;

/**
 * Created by esweeney on 9/13/13.
 */
public class OeMapGcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public OeMapGcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                try {
                    Presence p = new JsonPresence(extras);
                    PresenceHelper h = new PresenceHelper(this);
                    h.replacePresence(p);
                    OeLog.d("updated presences from gcm push");
                    Intent i = new Intent(getString(R.string.presence_service_update_intent));
                    i.putExtra(OeMapPresenceService.KEY_UID, p.getUID());
                    i.putExtra(OeMapPresenceService.KEY_SPACE_ID, p.getSpaceName());
                    sendBroadcast(i);

                } catch (PresenceException e) {
                    OeLog.e(e.toString(), e);
                } catch (Throwable e) {
                    OeLog.e(e.toString(), e);
                }
                // Post notification of received message.
                //sendNotification("Received: " + extras.toString());
                //OeLog.i("Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        OeMapGcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, OeMapActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
