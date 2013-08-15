package com.onextent.oemap.presence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.location.LocationHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.KvHelper;
import com.onextent.oemap.provider.PresenceHelper;
import com.onextent.oemap.provider.SpaceHelper;
import com.onextent.oemap.provider.SpaceProvider;

public class OeMapPresenceService extends Service {

    private SpaceHelper _spaceHelper;
    private LocationHelper   mLocHelper;
    private Presence         currentPresence  = null;
    private PresenceHelper   _dbHelper        = null;
    private KvHelper _kvHelper        = null;

    private String CMD_POLL        = null;
    private String CMD_BOOT        = null;
    private String CMD_ADD_SPACE   = null;
    private String CMD_RM_SPACE    = null;
    private String KEY_REASON      = null;
    private String KEY_SPACENAME   = null;
    private String KEY_UID         = null;


    private boolean _running = false;

    private Notification _notification = null;

    private SharedPreferences _prefs = null;

    @Override
    public void onCreate() {
        super.onCreate();

        _spaceHelper = new SpaceHelper(this);
        _dbHelper = new PresenceHelper(this);
        _kvHelper = new KvHelper(this);

        CMD_POLL        = getString(R.string.presence_service_cmd_poll);
        CMD_BOOT        = getString(R.string.presence_service_cmd_boot);
        CMD_ADD_SPACE   = getString(R.string.presence_service_cmd_add_space);
        CMD_RM_SPACE    = getString(R.string.presence_service_cmd_rm_space);
        KEY_SPACENAME   = getString(R.string.presence_service_key_spacename);
        KEY_UID         = getString(R.string.presence_service_key_uid);
        KEY_REASON      = getString(R.string.presence_service_key_reason);

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

    private void storeLocalPresence(Location l, String s) {

        LatLng latLng       = new LatLng(l.getLatitude(), l.getLongitude());
        String pid          = OeBaseActivity.id(this.getApplicationContext());
        //todo: allow per-map overrides
        String label        = _kvHelper.get(getString(R.string.pref_username), "nobody");
        String snippit      = _kvHelper.get(getString(R.string.pref_snippit), "sigh...");
        currentPresence     = PresenceFactory.createPresence(pid, latLng, label, snippit, s);

        _dbHelper.replacePresence(currentPresence);
    }

    private void broadcast(Location l) {

        String uid = OeBaseActivity.id(this);
        OeLog.d("broadcast for uid: " + uid);
        for (String s : _spaceHelper.getAllSpaceNames()) {

            storeLocalPresence(l, s);

            Intent intent = new Intent(getString(R.string.presence_service_update_intent));
            intent.putExtra(KEY_UID, uid);
            intent.putExtra(KEY_SPACENAME, s);
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
        mLocHelper.onPause();
        mLocHelper.onStop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return Service.START_STICKY;
    }

    //@Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) return;
        Bundle extras = intent.getExtras();
        String reason = extras.getString(KEY_REASON);
        OeLog.d("cmd reason: " + reason);

        if (CMD_BOOT.equals(reason)) {

            OeLog.d("booting");
            handleBoot();

        } if (CMD_ADD_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);

            ContentValues values = new ContentValues();
            values.put(SpaceProvider.Spaces.NAME, spacename);
            Uri r = getContentResolver().insert(SpaceProvider.CONTENT_URI, values);

            startRunning();

        } if (CMD_RM_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);
            _spaceHelper.deleteSpacename(spacename);

            if (!_spaceHelper.hasSpaceNames()) {
                stopRunning();
            }

        } if (CMD_POLL.equals(reason)) {

            if (_spaceHelper.hasSpaceNames()) {
                wakeup();
            }

        } else {

            OeLog.w("unknown reason intent");
        }
    }

    private void poll() {

        Cursor c = getContentResolver().query(SpaceProvider.CONTENT_URI,
                SpaceProvider.Spaces.PROJECTION_ALL, null, null,
                SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
        int pos = c.getColumnIndex(SpaceProvider.Spaces.NAME);
        while (c.moveToNext()) {
            String s = c.getString(pos);
            OeLog.d("polling " + s + " ...");
        }
        c.close();
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

