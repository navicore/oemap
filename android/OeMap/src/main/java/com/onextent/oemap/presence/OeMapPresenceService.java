package com.onextent.oemap.presence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OeMapPresenceService extends Service {

    public static final int DEFAULT_TTL = Presence.MEDIUM;
    public static final String CMD_POLL = "poll";
    public static final String CMD_BOOT = "boot";
    public static final String CMD_ADD_SPACE = "add_space";
    public static final String CMD_RM_SPACE = "rm_space";
    public static final String KEY_REASON = "reason";
    public static final String KEY_SPACENAME = "spacename";
    //public static final String KEY_SPACENAMES = "spacenames";
    public static final String KEY_UID = "uid";
    //private static final String PRESENCE_URL =  "http://10.0.0.2:8080/presence";
    private static final String PRESENCE_URL = "http://oemap.onextent.com:8080/presence";
    private SpaceHelper _spaceHelper;
    private LocationHelper _locHelper;
    private PresenceHelper _presenceHelper = null;
    private KvHelper _kvHelper = null;
    private AsyncTask _currentTask = null;
    private AsyncTask _currentPollTask = null;
    private boolean _running = false;
    private Notification _notification = null;
    private SharedPreferences _prefs = null;

    @Override
    public void onCreate() {
        super.onCreate();

        _spaceHelper = new SpaceHelper(this);
        _presenceHelper = new PresenceHelper(this);
        _kvHelper = new KvHelper(this);

        updateNotification();
        _locHelper = new LocationHelper(new LocationHelper.LHContext() {
            @Override
            public Context getContext() {
                return OeMapPresenceService.this;
            }

            @Override
            public void updateLocation(Location l) {
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
    }

    private Presence createPresence(Location l, String spacename) {
        int ttl = _kvHelper.getInt(getString(R.string.pref_ttl), DEFAULT_TTL);
        return createPresence(l, spacename, ttl);
    }

    private Presence createPresence(Location l, String spacename, int ttl) {

        LatLng latLng = null;
        String label = null;
        String snippit = null;
        if (l != null) {

            //todo: allow per-map overrides
            latLng = new LatLng(l.getLatitude(), l.getLongitude());
            label = _kvHelper.get(getString(R.string.pref_username), "nobody");
            snippit = _kvHelper.get(getString(R.string.pref_snippit), "sigh...");
        }
        String uid = OeBaseActivity.id(this.getApplicationContext());
        Presence p = PresenceFactory.createPresence(uid, latLng, label, snippit, spacename, ttl);
        return p;
    }

    private void broadcast(Location l) {

        String uid = OeBaseActivity.id(this);
        for (String s : _spaceHelper.getAllSpaceNames()) {

            OeLog.d("broadcast for uid: " + uid + " on space: " + s);

            Presence p = createPresence(l, s);

            _presenceHelper.replacePresence(p);

            broadcastIntent(p);

            sendPresence(p);
        }
    }

    private void broadcastIntent(Presence p) {

        Intent intent = new Intent(getString(R.string.presence_service_update_intent));
        intent.putExtra(KEY_UID, p.getUID());
        intent.putExtra(KEY_SPACENAME, p.getSpaceName());
        OeLog.d("    sending action: " + intent.getAction() + " for uid: " + p.getUID() +
                " and space: " + p.getSpaceName());
        sendBroadcast(intent);
    }

    private void sendPresence(Presence p) {

        //if (_currentTask == null) //don't queue up if server is slow or down
        new Send().execute(p);
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
        OeLog.d("cmd reason: " + reason);

        if (CMD_BOOT.equals(reason)) {

            OeLog.d("booting");
            handleBoot();

        } else if (CMD_ADD_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);

            ContentValues values = new ContentValues();
            values.put(SpaceProvider.Spaces.NAME, spacename);
            Uri r = getContentResolver().insert(SpaceProvider.CONTENT_URI, values);
            updateNotification();

            startRunning();

        } else if (CMD_RM_SPACE.equals(reason)) {

            String spacename = extras.getString(KEY_SPACENAME);

            _spaceHelper.deleteSpacename(spacename);

            Presence p = createPresence(null, spacename, Presence.NONE);

            sendPresence(p);

            if (!_spaceHelper.hasSpaceNames()) {
                stopRunning();
            }
            updateNotification();
        }

        if (CMD_POLL.equals(reason)) {

            if (_spaceHelper.hasSpaceNames()) {
                wakeup();
            }
        } else {

            OeLog.w("unknown reason intent");
        }
    }

    private void updateNotification() {

        hideNotification();
        List<String> l = _spaceHelper.getAllSpaceNames();
        if (l != null && l.size() > 0) {

        StringBuffer b = new StringBuffer("broadcasting your location to:");
        int sz = l.size();
        for (int i = 0; i < sz; i++ ) {
            String s = l.get(i);
            b.append(" ");
            b.append(s);
            if (i < (sz -1))  b.append(",");
        }

        createNotification(b.toString());
        showNotification();
        }
    }

    private void pollSpace(String s) {

        /*
        - get a list of all uids already in store
        - add a new presence from the server, removing the uid from the old list as you go
        - any uids left in the list are deletes, create a delete presence and bcast it

        todo: this protocol is insane, clean it up so that it self cleans and is always
        in-sync with the server
         */

        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(PRESENCE_URL + "?space=" + URLEncoder.encode(s, "UTF8"));
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Accept", "application/json");
            Set<String> oldUids = null;
            Set<Presence> prepres = _presenceHelper.getAllPrecenses(s);
            if (prepres != null) {
                oldUids = new HashSet<String>();
                for (Presence p : prepres) {
                    oldUids.add(p.getUID());
                }
            }
            _presenceHelper.deletePresencesWithSpaceNameNotMine(s); //todo: too expensive and insanely clumsy
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
                    OeLog.d("processing 200 get:/n" + json);
                    processPollJson(s, json, oldUids);
                    break;
                case 404: //none found
                    break;
                default:
                    OeLog.w("got status line status code: " + response.getStatusLine().getStatusCode() + "/n" + json);
            }
        } catch (UnsupportedEncodingException e) {
            OeLog.e(e.toString(), e);
        } catch (IOException e) {
            OeLog.e(e.toString(), e);
        } catch (JSONException e) {
            OeLog.e("error polling map " + s + ": " + e.toString(), e);
        }
    }

    private void processPollJson(String space, String json, Set<String> oldUids) throws JSONException {

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
            for (String uid : oldUids) {
                Presence p = PresenceFactory.createPresence(uid, null, null, null, space, Presence.NONE);
                broadcastIntent(p);
            }
        }
    }

    private void poll() {

        if (_currentPollTask == null) //don't queue up if server is slow or down
            new Poll().execute();
    }

    private void stopRunning() {
        hideNotification();
        _running = false;
        //stopSelf(); //todo:
    }

    private void wakeup() {
        updateNotification();
        poll(); //do one now
        Location l = _locHelper.getLastLocation();
        if (l != null)
            broadcast(l);
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
            Cursor c = getContentResolver().query(SpaceProvider.CONTENT_URI,
                    SpaceProvider.Spaces.PROJECTION_ALL, null, null,
                    SpaceProvider.Spaces.SORT_ORDER_DEFAULT);
            int pos = c.getColumnIndex(SpaceProvider.Spaces.NAME);
            while (c.moveToNext()) {
                String s = c.getString(pos);
                OeLog.d("Poll.doInBackground " + s + " ...");
                pollSpace(s);
            }
            c.close();
            return null;
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

