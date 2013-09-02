/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.presence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OeMapPresenceService extends Service {

    public static final int DEFAULT_TTL = Presence.MEDIUM;
    public static final String CMD_BCAST = "bcast";
    public static final String CMD_POLL = "poll";
    public static final String CMD_BOOT = "boot";
    public static final String CMD_ADD_SPACE = "add_space";
    public static final String CMD_ADD_INTEREST = "add_interest";
    public static final String CMD_RM_INTEREST = "add_interest";
    public static final String CMD_RM_SPACE = "rm_space";
    public static final String KEY_REASON = "reason";
    public static final String KEY_SPACENAME = "spacename";
    public static final String KEY_UID = "uid";

    private static final long DUR_15_MINUTES = 1000 * 60 * 60 * 15;
    private static final long DUR_15_SECONDS = 1000 * 15;
    private static final long QUIET_TIME = DUR_15_MINUTES;

    //private static final String PRESENCE_URL =  "http://10.0.0.2:8080/presence";
    private static final String PRESENCE_URL = "http://oemap.onextent.com:8080/presence";
    private static final String PRESENCE_PARAM_SPACE = "space";
    private static final String PRESENCE_PARAM_LATITUDE = "lat";
    private static final String PRESENCE_PARAM_LONGITUDE = "lon";
    private static final String PRESENCE_PARAM_MAX = "max";
    private SpaceHelper _spaceHelper;
    private LocationHelper _locHelper;
    private PresenceHelper _presenceHelper = null;
    private KvHelper _kvHelper = null;
    private AsyncTask _currentTask = null;
    private AsyncTask _currentPollTask = null;
    private boolean _running = false;
    private Notification _notification = null;
    private Location _lastLocation = null;
    private LatLng _interestPoint = null;
    private Location _lastBCastLocation = null;
    private long _lastPollTime = 0;
    private long _lastPushTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        OeLog.d("OeMapPresenceService.onCreate");

        _spaceHelper = new SpaceHelper(this);
        _presenceHelper = new PresenceHelper(this);
        _kvHelper = new KvHelper(this);

        _locHelper = new LocationHelper(new LocationHelper.LHContext() {
            @Override
            public Context getContext() {
                return OeMapPresenceService.this;
            }

            @Override
            public void updateLocation(Location l) {
                _lastLocation = l;
                broadcast(l);
                poll();  //todo: put on timer?
            }
        });

        _locHelper.setCallback(new LocationHelper.Callback() {

            @Override
            public void onConnected() {
                wakeup();
            }
        });
        _locHelper.onCreate();
        _locHelper.onStart();
        _locHelper.onResume();

        updateNotification();
    }

    private Presence createPresence(Location l, String spacename) throws PresenceException {
        int ttl = _kvHelper.getInt(getString(R.string.pref_ttl), DEFAULT_TTL);
        return createPresence(l, spacename, ttl);
    }

    private Presence createPresence(Location l, String spacename, int ttl) throws PresenceException {

        LatLng latLng = null;
        String label = null;
        String snippit = null;

        if (l != null) {

            latLng = new LatLng(l.getLatitude(), l.getLongitude());

            label = _kvHelper.get(getString(R.string.pref_username), "nobody");

            snippit = _kvHelper.get(getString(R.string.pref_snippit), "sigh...");
        }

        Date lease = _spaceHelper.getLease(spacename);
        String uid = OeBaseActivity.id(this.getApplicationContext());
        Presence p = PresenceFactory.createPresence(uid, latLng, label, snippit, spacename, ttl, lease);

        return p;
    }

    private void quitSpace(String s) {

        OeLog.d("OeMapPresenceService.quitSpace: " + s);

        _spaceHelper.deleteSpacename(s);

        _presenceHelper.deletePresencesWithSpaceName(s);

        _kvHelper.replace(getString(R.string.state_current_mapname), getString(R.string.null_map_name));

        broadcastQuitSpaceIntent(s);

        try {

            Presence p = createPresence(null, s, Presence.NONE);

            new Send().execute(p);

        } catch (PresenceException e) {
            OeLog.e(e);
        }
    }

    private void updatePresenceEverywhere(String s, Location l) {

        OeLog.d("OeMapPresenceService.updatePresenceEverywhere: " + s);
        Presence p = null;
        try {
            p = createPresence(l, s);
            _presenceHelper.replacePresence(p);

            broadcastIntent(p);

            new Send().execute(p);

        } catch (PresenceException e) {
            OeLog.e(e);
        }
    }

    private void broadcast(Location l) {

        OeLog.d("OeMapPresenceService.broadcast (location)");

        if (_lastPushTime < System.currentTimeMillis() - QUIET_TIME) {
            _lastPushTime = System.currentTimeMillis();

            //todo: test to see how old and far away the prev loc was

            _lastBCastLocation = l;

            for (String s : _spaceHelper.getAllSpaceNames()) {

                Date lease = _spaceHelper.getLease(s);
                if (lease != null && lease.getTime() < System.currentTimeMillis()) {

                    quitSpace(s);

                } else {

                    updatePresenceEverywhere(s, l);
                }
            }
        }
    }

    private void broadcastQuitSpaceIntent(String s) {

        Intent intent = new Intent(getString(R.string.presence_service_quit_space_intent));
        intent.putExtra(KEY_SPACENAME, s);
        sendBroadcast(intent);
    }

    private void broadcastIntent(Presence p) {

        Intent intent = new Intent(getString(R.string.presence_service_update_intent));
        intent.putExtra(KEY_UID, p.getUID());
        intent.putExtra(KEY_SPACENAME, p.getSpaceName());
        sendBroadcast(intent);
    }

    private void createNotification(String msg) {
        Intent intent = new Intent(this, OeMapActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        _notification = new Notification.Builder(this)
                .setContentIntent(pIntent)
                .setContentTitle("OeMap is Broadcasting").setContentText(msg)
                .setSmallIcon(R.drawable.ic_launcher).getNotification();

        _notification.flags |= Notification.FLAG_NO_CLEAR;
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, _notification);
    }

    private void hideNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    @Override
    public void onDestroy() {
        _locHelper.onPause();
        _locHelper.onStop();
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

        if (CMD_BOOT.equals(reason)) {

            handleBoot();

        } else if (CMD_ADD_SPACE.equals(reason)) {

            startRunning();
            wakeup();
            updateNotification();

        } else if (CMD_RM_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);
            OeLog.d(" *** ejs rm space: " + spacename);

            _spaceHelper.deleteSpacename(spacename);
            _presenceHelper.deletePresencesWithSpaceName(spacename);

            Presence p = null;
            try {

                p = createPresence(null, spacename, Presence.NONE);
                OeLog.d(" *** ejs rm space 2: " + spacename);
                new Send().execute(p);

            } catch (PresenceException e) {
                OeLog.e(e);
            }

            if (!_spaceHelper.hasSpaceNames()) {
                stopRunning();
            }
            updateNotification();
            wakeup();

        } else if (CMD_POLL.equals(reason)) {

            wakeup();

        } else if (CMD_BCAST.equals(reason)) {

            broadcast();

        } else {

            OeLog.w("unknown reason intent");
        }
    }

    private void updateNotification() {

        hideNotification();
        List<String> l = _spaceHelper.getAllSpaceNames();
        if (l != null && l.size() > 0) {

            StringBuffer b = new StringBuffer("posting your location to:");
            int sz = l.size();
            for (int i = 0; i < sz; i++) {
                String s = l.get(i);
                b.append(" ");
                b.append(s);
                if (i < (sz - 1)) b.append(",");
            }

            createNotification(b.toString());
            showNotification();
        }
    }

    private void pollSpace(String s) {

        LatLng point;

        if (_interestPoint != null) {
            point = _interestPoint;

        } else if (_lastLocation != null) {
            point = new LatLng(_lastLocation.getLatitude(), _lastLocation.getLongitude());

        } else {
            return; // not yet
        }

        if (_lastLocation == null) return; //not yet

        OeLog.d("pollSpace space: " + s);
        /*
        - get a list of all uids already in store
        - add a new presence from the server, removing the uid from the old list as you go
        - any uids left in the list are deletes, create a delete presence and bcast it

        todo: this protocol is insane, clean it up so that it self cleans and is always
        in-sync with the server
         */

        SpaceHelper.Space space = _spaceHelper.getSpace(s);
        int max = space.getMaxPoints();
        max += 1;    //don't forget you are the nearest person to yourself in oemap logic.
        // if you say 1 person it'll just be you coming back unless the server
        // learns to filter out self presence

        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(PRESENCE_URL + "?" +
                    PRESENCE_PARAM_SPACE + "=" + URLEncoder.encode(s, "UTF8") +
                    "&" + PRESENCE_PARAM_LATITUDE + "=" + URLEncoder.encode(String.valueOf(point.latitude), "UTF8") +
                    "&" + PRESENCE_PARAM_LONGITUDE + "=" + URLEncoder.encode(String.valueOf(point.longitude), "UTF8") +
                    "&" + PRESENCE_PARAM_MAX + "=" + max
            );
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Accept", "application/json");
            Set<String> oldUids = null;
            List<Presence> prepres = _presenceHelper.getAllPrecenses(s);
            if (prepres != null) {
                oldUids = new HashSet<String>();
                for (Presence p : prepres) {
                    oldUids.add(p.getUID());
                }
            }
            HttpResponse response = client.execute(get);
            InputStream content = response.getEntity().getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = buffer.readLine()) != null) {
                sb.append(line + "\n");
            }
            String json = sb.toString();
            int code = response.getStatusLine().getStatusCode();
            switch (code) {
                case 200:
                    processPollJson(s, json, oldUids);
                    break;
                case 404: //none found
                    break;
                default:
                    OeLog.w("got status line status code: " + response.getStatusLine().getStatusCode() + "/n" + json);
            }
        } catch (UnsupportedEncodingException e) {
            OeLog.e(e);
        } catch (IOException e) {
            OeLog.e(e);
        } catch (JSONException e) {
            OeLog.e(e);
        } catch (PresenceException e) {
            OeLog.e(e);
        }
    }

    private void processPollJson(String space, String json, Set<String> oldUids) throws JSONException, PresenceException {

        OeLog.d("processPollJson space: " + space);
        //_presenceHelper.deletePresencesWithSpaceNameNotMine(space); //todo: too expensive and insanely clumsy
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {

            Presence p = PresenceFactory.createPresence(array.getJSONObject(i));

            if (oldUids != null) {
                oldUids.remove(p.getUID());
            }

            if (!OeMapActivity.id(this).equals(p.getUID())) {
                // got someone else's presence
                _presenceHelper.replacePresence(p);
                broadcastIntent(p);
            }
        }
        if (oldUids != null) {
            OeLog.d("processPollJson cleanup old uids");
            for (String uid : oldUids) {
                Presence p = PresenceFactory.createPresence(uid, null, null, null, space, Presence.NONE, null);
                _presenceHelper.deletePresence(p);
                broadcastIntent(p); //causes current map to remove markers
            }
        }
    }

    private void poll() {

        if (_lastPollTime > System.currentTimeMillis() - QUIET_TIME) {
            return;
        }
        OeLog.d("poll");
        _lastPollTime = System.currentTimeMillis();

        if (_currentPollTask == null) //don't queue up if server is slow or down
            new Poll().execute();
    }

    private void stopRunning() {
        hideNotification();
        _running = false;
        //stopSelf(); //todo:
    }

    private void wakeup() {

        if (!_spaceHelper.hasSpaceNames()) {
            return;
        }

        _lastPollTime = 0;

        poll();
    }

    private void broadcast() {

        _lastPushTime = 0;
        Location l = _locHelper.getLastLocation();
        if (l == null) l = _lastLocation;
        if (l != null) broadcast(l);
    }

    private void startRunning() {
        if (!_running) {
            _running = true;
            //   use TimerManager setrecurring
        }
    }

    private void handleBoot() {
        if (_running) {
            startRunning();
        }
    }

    private class Send extends AsyncTask<Presence, Void, String> {

        @Override
        protected String doInBackground(Presence... presences) {
            try {

                OeLog.d("Send.doInBackground");

                String r = null;
                Presence p = presences[0];
                HttpClient client = new DefaultHttpClient();
                HttpPut put = new HttpPut(PRESENCE_URL);
                put.addHeader("Content-Type", "application/json");
                put.addHeader("Accept", "application/json");
                try {
                    put.setEntity(new StringEntity(p.toString()));
                    HttpResponse response = client.execute(put);
                    InputStream content = response.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = buffer.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    r = sb.toString();
                } catch (UnsupportedEncodingException e) {
                    r = e.toString();
                } catch (IOException e) {
                    r = e.toString();
                }
                return r;
            } catch (Throwable err) {
                OeLog.w(err);
                return err.toString();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _currentTask = this;
        }

        @Override
        protected void onPostExecute(String r) {

            super.onPostExecute(r);
            _currentTask = null;
        }
    }

    private class Poll extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... _) {

            try {

                Cursor c = getContentResolver().query(SpaceProvider.CONTENT_URI,
                        SpaceProvider.Spaces.PROJECTION_ALL, null, null,
                        SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
                OeLog.d("doInBackground Poll cursor size: " + c.getCount());
                int pos = c.getColumnIndex(SpaceProvider.Spaces._ID);
                while (c.moveToNext()) {
                    String s = c.getString(pos);
                    pollSpace(s);
                }
                c.close();
                return null;
            } catch (Throwable err) {
                OeLog.w(err);
                return err.toString();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _currentPollTask = this;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            _currentPollTask = null;
        }
    }
}

