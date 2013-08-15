package com.onextent.android.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.onextent.oemap.R;
import com.onextent.oemap.provider.KvHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
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

        String uname = getUserName();

        _kvHelper.replace(getString(R.string.pref_username), uname);
        _kvHelper.replaceBoolean(getString(R.string.state_init), true);
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

