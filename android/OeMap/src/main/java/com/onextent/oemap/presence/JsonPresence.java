package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.oemap.OeLog;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonPresence implements Presence {

    private final String pid, label, snippet, spacename;
    private final LatLng location;

    JsonPresence(String json) throws JSONException {
        JSONObject jobj = new JSONObject(json);
        this.pid = jobj.getString("pid");
        this.location = new LatLng(jobj.getDouble("latitude"), jobj.getDouble("longitude"));
        this.label = jobj.getString("label");
        this.snippet = jobj.getString("snippet");
        this.spacename = jobj.getString("spacename");
    }

    JsonPresence(String pid, LatLng l, String lbl, String snippet, String spacename) {
        this.pid = pid;
        this.location = l;
        this.label = lbl;
        this.snippet = snippet;
        this.spacename = spacename;
    }

    @Override
    public LatLng getLocation() {
        return location;
    }

    @Override
    public String getPID() {
        return pid;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Override
    public String getSpaceName() {
        return spacename;
    }

    @Override
    public String toString() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("pid", pid);
            jobj.put("longitude", location.longitude);
            jobj.put("latitude", location.latitude);
            jobj.put("label", label);
            jobj.put("snippet", snippet);
            jobj.put("spacename", spacename);

            return jobj.toString();
        } catch (JSONException e) {
            OeLog.e(e.toString(), e);
            return null;
        }
    }
}

