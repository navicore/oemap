package com.onextent.oemap.presence;

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

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.location.LocationHelper;
import com.onextent.android.util.KeyValueDbHelper;
import com.onextent.android.util.ListDbHelper;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.OeMapActivity;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.PresenceDbHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class OeMapPresenceService extends Service {

    private LocationHelper   mLocHelper;
    private Presence         currentPresence  = null;
    private PresenceDbHelper _dbHelper        = null;
    private KeyValueDbHelper _kvHelper        = null;
    private ListDbHelper _spacenameDbHelper = null;

    private String CMD_POLL        = null;
    private String CMD_BOOT        = null;
    private String CMD_ADD_SPACE   = null;
    private String CMD_RM_SPACE    = null;
    private String KEY_REASON      = null;
    private String KEY_SPACENAME   = null;
    private String KEY_UID         = null;


    private boolean _running = false;
    private List<String> _spacenames = null;

    private Notification _notification = null;

    private SharedPreferences _prefs = null;

    @Override
    public void onCreate() {
        super.onCreate();

        _dbHelper = new PresenceDbHelper(this, getString(R.string.presence_db_name));
        _kvHelper = new KeyValueDbHelper(this, getString(R.string.app_key_value_store_name));
        _spacenameDbHelper = new ListDbHelper(this, "oemap_spacename_store");

        CMD_POLL        = getString(R.string.presence_service_cmd_poll);
        CMD_BOOT        = getString(R.string.presence_service_cmd_boot);
        CMD_ADD_SPACE   = getString(R.string.presence_service_cmd_add_space);
        CMD_RM_SPACE    = getString(R.string.presence_service_cmd_rm_space);
        KEY_SPACENAME   = getString(R.string.presence_service_key_spacename);
        KEY_UID         = getString(R.string.presence_service_key_uid);
        KEY_REASON      = getString(R.string.presence_service_key_reason);

        try {
            _spacenames = _spacenameDbHelper.getAll();
        } catch (JSONException e) {
            OeLog.e("error reading spacenames: " + e, e);
        }
        if (_spacenames == null)
            _spacenames = new ArrayList<String>();

        int sz = _spacenames == null ? 0 : _spacenames.size();
        OeLog.d("loaded " + sz + " spacenames");
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
        for (String s : _spacenames) {

            storeLocalPresence(l, s);

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
        _dbHelper.close();
        _kvHelper.close();
        _spacenameDbHelper.close();
        mLocHelper.onPause();
        mLocHelper.onStop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveSpacenames() {
        for (String s : _spacenames) {
            _spacenameDbHelper.replace(s);
        }
        OeLog.d("saved " + _spacenames.size() + " spacenames");
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
            if (!_spacenames.contains(spacename)) {
                _spacenames.add(spacename);
                saveSpacenames();
            }
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

