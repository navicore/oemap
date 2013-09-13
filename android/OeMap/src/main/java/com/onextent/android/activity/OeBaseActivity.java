/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.android.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.R;
import com.onextent.oemap.provider.KvHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class OeBaseActivity extends Activity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String INSTALLATION = "INSTALLATION";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String sID = null;
    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "947389659649";
    private KvHelper _prefs = null;
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private String regid;

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        // http://en.wikipedia.org/wiki/Universally_unique_identifier#Random_UUID_probability_of_duplicates
        String id = createUUID();
        out.write(id.getBytes());
        out.close();
    }

    public static String createUUID() {

        // http://en.wikipedia.org/wiki/Universally_unique_identifier#Random_UUID_probability_of_duplicates
        return UUID.randomUUID().toString();
    }

    public static String encode(String value) {

        if (value == null) return null;

        try {
            return URLEncoder.encode(value, "UTF8");
        } catch (UnsupportedEncodingException e) {
            OeLog.w(e);
            return null;
        }
    }

    public static String decode(String id) {

        if (id == null) return null;

        try {
            return URLDecoder.decode(id, "UTF8");
        } catch (UnsupportedEncodingException e) {
            OeLog.w(e);
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    public String id() {
        return id(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _prefs = new KvHelper(this);

        // Check device for Play Services APK.
        if (!checkPlayServices()) {
            Toast.makeText(this, "error, no play services", Toast.LENGTH_LONG);
        } else {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(getApplicationContext());

            if (regid == null || regid.isEmpty()) {
                registerInBackground();
            }
        }
    }

    //
    // begin GCM
    //

    private void init() { //first time use init :)

        boolean alreadyInit = _prefs.getBoolean(getString(R.string.state_init), false);
        if (alreadyInit) return;

        String uname = getProfileDisplayName();

        _prefs.replace(getString(R.string.pref_username), uname);
        _prefs.replaceBoolean(getString(R.string.state_init), true);
    }

    private String getProfileDisplayName() {

        String name;

        Uri uri = ContactsContract.Profile.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Profile.DISPLAY_NAME};

        Cursor people = getContentResolver().query(uri, projection, null, null, null);

        int indexName = people.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);

        people.moveToFirst();
        do {
            name = people.getString(indexName);
            if (name != null && !name.equals("#BAL")) {
                OeLog.d("found profile name: " + name);
                break;
            }
        } while (people.moveToNext());

        OeLog.d("using profile name: " + name);
        return name;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                OeLog.i("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        String rid = _prefs.get(getString(R.string.prefs_gcm_regid));
        if (rid == null || rid.isEmpty()) {
            OeLog.w("Registration not found.");
        }
        return rid;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    //sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                OeLog.i(msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        _prefs.replace(getString(R.string.prefs_gcm_regid), regId);
    }
}

