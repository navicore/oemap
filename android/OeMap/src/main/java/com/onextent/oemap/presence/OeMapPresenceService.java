package com.onextent.oemap.presence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.location.LocationHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;

import java.util.HashSet;
import java.util.Set;

public class OeMapPresenceService extends Service {

    private LocationHelper mLocHelper;
    private Presence                currentPresence = null;

    private String CMD_POLL        = null;
    private String CMD_BOOT        = null;
    private String CMD_ADD_SPACE   = null;
    private String CMD_RM_SPACE    = null;
    private String KEY_REASON      = null;
    private String KEY_RUNNING     = null;
    private String KEY_SPACENAMES  = null;
    private String KEY_SPACENAME   = null;
    private String KEY_UID         = null;


    private boolean _running = false;
    private Set<String> _spacenames = null;

    private Notification _notification = null;

    @Override
    public void onCreate() {
        super.onCreate();
        CMD_POLL        = getString(R.string.presence_service_cmd_poll);
        CMD_BOOT        = getString(R.string.presence_service_cmd_boot);
        CMD_ADD_SPACE   = getString(R.string.presence_service_cmd_add_space);
        CMD_RM_SPACE    = getString(R.string.presence_service_cmd_rm_space);
        KEY_SPACENAME   = getString(R.string.presence_service_key_spacename);
        KEY_UID         = getString(R.string.presence_service_key_uid);
        KEY_REASON      = getString(R.string.presence_service_key_reason);
        KEY_RUNNING     = getString(R.string.presence_service_key_running);
        KEY_SPACENAMES  = getString(R.string.presence_service_key_spacenames);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefEdit = mPrefs.edit();

        _running = mPrefs.getBoolean(KEY_RUNNING, false);
        _spacenames = mPrefs.getStringSet(KEY_SPACENAMES, null);
        if (_spacenames == null) {
            _spacenames = new HashSet<String>();
        }
        createNotification();
        mLocHelper = new LocationHelper(new LocationHelper.LHContext() {
            @Override
            public Context getContext() {
                return OeMapPresenceService.this;
            }
            @Override
            public void updateLocation(Location l) {
                broadcast(l);
            }
        });
        mLocHelper.onCreate();
        mLocHelper.onStart();
        mLocHelper.onResume();
    }

    private void broadcast(Location l) {

        String uid = OeBaseActivity.id(this);
        OeLog.d("broadcast for uid: " + uid);
        for (String s : _spacenames) {
            //todo:
            //  write my own pres to db for each map
            Intent intent = new Intent(getString(R.string.presence_service_update_intent));
            intent.putExtra(KEY_UID, uid);
            intent.putExtra(KEY_SPACENAME, s);
            //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            OeLog.d("    sending action: " + intent.getAction() + " for uid: " + uid + " and space: " + s);
            sendBroadcast(intent);
        }
    }

    private void createNotification() {
        Intent intent = new Intent(this, OeMapActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        _notification = new Notification.Builder(this)
                .setContentIntent(pIntent)
                .setContentTitle("OeMap").setContentText("OeMap is broadcasting your location.")
                .setSmallIcon(R.drawable.ic_launcher).getNotification();

        _notification.flags |= Notification.FLAG_NO_CLEAR;
    }

    private void showNotification() {
        //todo: list the maps in the notification
        NotificationManager notificationManager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, _notification);
    }

    private void hideNotification() {
        NotificationManager notificationManager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    @Override
    public void onDestroy() {
        saveSpacenames();
        mLocHelper.onPause();
        mLocHelper.onStop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveSpacenames() {
        mPrefEdit.putBoolean(KEY_RUNNING, _running);
        mPrefEdit.putStringSet(KEY_SPACENAMES, _spacenames);
        mPrefEdit.commit();
    }

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefEdit;

    //public OeMapPresenceService() {
    //    super("OeMap Presence Service");
    //}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return Service.START_STICKY;
    }

    //@Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        String reason = extras.getString(KEY_REASON);
        OeLog.d("cmd reason: " + reason);

        if (CMD_BOOT.equals(reason)) {

            OeLog.d("booting");
            handleBoot();

        } if (CMD_ADD_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);
            _spacenames.add(spacename);
            saveSpacenames();
            startRunning();

        } if (CMD_RM_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);
            _spacenames.remove(spacename);
            saveSpacenames();

            //if no more maps, stop running
            if (_spacenames.isEmpty()) {
                stopRunning();
            }

        } if (CMD_POLL.equals(reason)) {

            if (!_spacenames.isEmpty())
                wakeup();

        } else {

            OeLog.w("unknown reason intent");

        }
    }

    private void poll() {

        for (String map : _spacenames) {

            OeLog.d("polling " + map + " ...");
        }
        //for each map:
        //  do a getall rest call
        //  write to db
        //  bcast intent for map
    }

    private void stopRunning() {
        hideNotification();
        _running = false;
        //stopSelf(); //todo:
    }

    private void wakeup() {
        showNotification();
        poll(); //do one now
    }
    private void startRunning() {
        if (!_running) {
            _running = true;
            //   use TimerManager setrecurring
        }
        wakeup();
    }

    private void handleBoot() {
        if (_running) {
            startRunning();
        }
    }
}
