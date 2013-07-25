package com.onextent.oemap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OeBaseActivity extends Activity {
    private static final String INSTALLATION = "INSTALLATION";
    private static final int MAX_HISTORY = 5;
    private static String sID = null;

    SharedPreferences mPrefs;
    SharedPreferences.Editor mPrefEdit;

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
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }

    public String id() {
        return id(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefEdit = mPrefs.edit();
        init();
    }

    private void init() { //first time use init :)

        boolean alreadyInit = mPrefs.getBoolean(getString(R.string.state_init), false);
        if (alreadyInit) return;

        String uname = getUserName();

        mPrefEdit.putString(getString(R.string.pref_username), uname);
        mPrefEdit.putBoolean(getString(R.string.state_init), true);
        mPrefEdit.commit();
    }

    List<String> getHistory() {

        try {

            String h = mPrefs.getString(getString(R.string.state_history), "[]");
            JSONArray a = new JSONArray(h);
            List<String> l = new ArrayList<String>();

            for (int i = 0; i < a.length(); i++) {
                String s = a.getString(i);
                l.add(s);
            }
            OeLog.d("getting history: " + l.toString());
            return l;

        } catch (JSONException e) {
            OeLog.w(e.toString(), e);
            return null;
        }
    }

    //todo? think about named histories for multiple stuff
    void addHistory(String item) {

        String h = mPrefs.getString(getString(R.string.state_history), "[]");
        try {
            JSONArray a = new JSONArray(h);
            a = trimHistory(a, item);
            a.put(item); //ejs todo: fixed len most recent = top
            OeLog.d("adding history: " + a.toString());
            mPrefEdit.putString(getString(R.string.state_history), a.toString());
            mPrefEdit.commit();
        } catch (JSONException e) {
            OeLog.w(e.toString(), e);
        }
    }

    private JSONArray trimHistory(JSONArray a, String item) {
        //cut json array to max len and remove items matching item
        // probably because 'item' is going on top of the new hist
        JSONArray newa = new JSONArray();
        int offs = newa.length() - MAX_HISTORY;
        if (offs < 0) offs = 0;
        for (int i = 0; i < MAX_HISTORY && i < a.length(); i++) {
            try {
                String newitem = a.getString(i + offs);
                if (newitem.equals(item)) continue;
                newa.put(a.get(i + offs));
            } catch (JSONException e) {
                OeLog.w(e.toString());
            }
        }
        return newa;
    }

    private String getUserName() {

        Cursor c = getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        int count = c.getCount();
        //String[] columnNames = c.getColumnNames();
        boolean b = c.moveToFirst();
        int position = c.getPosition();
        String uname = "nobody";
        if (count == 1 && position == 0) {

            int display_name_idx = c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
            if (display_name_idx >= 0) {
                String n = c.getString(display_name_idx);
                if (n != null && !n.equals(""))
                    uname = n;
            }
        }
        c.close();
        return uname;
    }
}

