/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.android.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

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

public class OeBaseActivity extends Activity {

    private static final String INSTALLATION = "INSTALLATION";
    private static final int MAX_HISTORY = 5;
    private static String sID = null;
    private KvHelper _kvHelper = null;

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
        _kvHelper = new KvHelper(this);
    }

    private void init() { //first time use init :)

        boolean alreadyInit = _kvHelper.getBoolean(getString(R.string.state_init), false);
        if (alreadyInit) return;

        String uname = getProfileDisplayName();

        _kvHelper.replace(getString(R.string.pref_username), uname);
        _kvHelper.replaceBoolean(getString(R.string.state_init), true);
    }

    private String getProfileDisplayName() {

        String name;

        Uri uri = ContactsContract.Profile.CONTENT_URI;
        String[] projection = new String[] {ContactsContract.Profile.DISPLAY_NAME};

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
}

