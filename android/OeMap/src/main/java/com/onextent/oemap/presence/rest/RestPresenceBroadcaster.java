package com.onextent.oemap.presence.rest;

import android.os.AsyncTask;
import android.widget.Toast;

import com.onextent.android.activity.OeBaseActivity;
import com.onextent.oemap.presence.BasePresenceBroadcaster;
import com.onextent.oemap.presence.Presence;

import java.io.IOException;

public class RestPresenceBroadcaster extends BasePresenceBroadcaster {

    public RestPresenceBroadcaster(OeBaseActivity activity) {
        super(activity);
    }

    private static final String HOST = "10.0.2.2";

    @Override
    protected void broadcast(Presence l) {

        String msg = l.toString();
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //todo: send json via rest to map server
        //warning: do not pass ref async, l' mapname fld keeps changing  :(

            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    //try {
                    //} catch (IOException ex) {
                    //    msg = "Error :" + ex.getMessage();
                    //}
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) { //ui thread
                    if (msg != null && !"".equals(msg))
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }

            }.execute(null, null, null);
    }
}

