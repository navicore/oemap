package com.onextent.oemap.presence.rest;

import android.os.AsyncTask;
import android.widget.Toast;

import com.onextent.android.activity.OeBaseActivity;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.BasePresenceBroadcaster;
import com.onextent.oemap.presence.Presence;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RestPresenceBroadcaster extends BasePresenceBroadcaster {

    private static final long THROTTLE_DELAY = 10000;
    //HttpPost httpput = new HttpPost(HOST + "/" + p.getPID());
    private static final String PUTURL = "http://10.0.0.2:5555/presence";
    private long lastBCastTime = 0;

    public RestPresenceBroadcaster(OeBaseActivity activity) {
        super(activity);
    }

    @Override
    protected void broadcast(final Presence p) {

        long now = System.currentTimeMillis();

        if (now < lastBCastTime + THROTTLE_DELAY) return;

        OeLog.d("rest broadcast running...");

        lastBCastTime = now;

        //warning: do not pass ref async, l' mapname fld keeps changing  :(
        final String json = p.toString();

        //todo: make sure we can only run 1 of these at a time
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                DefaultHttpClient httpclient = new DefaultHttpClient();

                HttpPut httpput = new HttpPut(PUTURL + "/" + p.getPID());

                try {
                    StringEntity se = new StringEntity(json);

                    httpput.setEntity(se);
                    httpput.setHeader("Accept", "application/json");
                    httpput.setHeader("Content-type", "application/json");

                    ResponseHandler responseHandler = new BasicResponseHandler();
                    HttpResponse response = httpclient.execute(httpput);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    StringBuilder builder = new StringBuilder();
                    for (String line = null; (line = reader.readLine()) != null; ) {
                        builder.append(line).append("\n");
                    }
                    OeLog.d("rest broadcast result: " + builder.toString());
                        /*
                        JSONTokener tokener = new JSONTokener(builder.toString());
                        JSONArray finalResult = new JSONArray(tokener);
                         */

                } catch (IOException ex) {
                    OeLog.e("doInBackground error: " + ex, ex);
                    msg = "Error :" + ex.getMessage();
                    //} catch (JSONException ex) {
                    //    OeLog.e("doInBackground error: " + ex, ex);
                    //    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) { //ui thread
                OeLog.d("onPostExecute running...");
                if (msg != null && !"".equals(msg)) {
                    OeLog.d("onPostExecute error: " + msg);

                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            }

        }.execute(null, null, null);
    }
}

