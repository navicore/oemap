package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.util.OeLog;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonPresence implements Presence {

    private static final String KEY_PID = "pid";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LON = "longitude";
    private static final String KEY_LBL = "label";
    private static final String KEY_SNP = "snippit";
    private static final String KEY_SPC = "space";
    private static final String KEY_TIM = "time";

    private final String _pid, _label, _snippet;
    private String _spacename;
    private final LatLng _location;
    private final long _create_time;

    JsonPresence(String json) throws JSONException {
        JSONObject jobj = new JSONObject(json);
        _pid = jobj.getString("pid");
        _location = new LatLng(jobj.getDouble("latitude"), jobj.getDouble("longitude"));
        _label = jobj.getString("label");
        _snippet = jobj.getString("snippet");
        _spacename = jobj.getString("spacename");
        _create_time = jobj.getLong("createTime");
    }

    JsonPresence(String pid, LatLng l, String lbl, String snippet, String spacename) {
        _pid = pid;
        _location = l;
        _label = lbl;
        _snippet = snippet;
        _spacename = spacename;
        _create_time = System.currentTimeMillis();
    }

    @Override
    public LatLng getLocation() {
        return _location;
    }

    @Override
    public String getUID() {
        return _pid;
    }

    @Override
    public String getLabel() {
        return _label;
    }

    @Override
    public String getSnippet() {
        return _snippet;
    }

    @Override
    public String getSpaceName() {
        return _spacename;
    }

    @Override
    public void setSpaceName(String name) {

        _spacename = name;
    }

    @Override
    public long getAgeInMillis() {
        return System.currentTimeMillis() - _create_time;
    }

    @Override
    public String toString() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("pid", _pid);
            jobj.put("longitude", _location.longitude);
            jobj.put("latitude", _location.latitude);
            jobj.put("label", _label);
            jobj.put("snippet", _snippet);
            jobj.put("spacename", _spacename);
            jobj.put("createTime", _create_time);

            return jobj.toString();
        } catch (JSONException e) {
            OeLog.e(e.toString(), e);
            return null;
        }
    }
}

