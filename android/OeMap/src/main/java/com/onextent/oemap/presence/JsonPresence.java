package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.util.OeLog;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonPresence implements Presence {

    private static final String KEY_UID = "uid";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LON = "longitude";
    private static final String KEY_LBL = "label";
    private static final String KEY_SNP = "snippit";
    private static final String KEY_SPC = "space";
    private static final String KEY_TIM = "time";
    private static final String KEY_TTL = "ttl";

    private final String _uid, _label, _snippet;
    private String _spacename;
    private final LatLng _location;
    private final long _create_time;
    private final int _time_to_live;

    JsonPresence(String json) throws JSONException {
        JSONObject jobj = new JSONObject(json);
        _uid            = jobj.getString(KEY_UID);
        _location       = new LatLng(jobj.getDouble(KEY_LAT), jobj.getDouble(KEY_LON));
        _label          = jobj.getString(KEY_LBL);
        _snippet        = jobj.getString(KEY_SNP);
        _spacename      = jobj.getString(KEY_SPC);
        _create_time    = jobj.getLong(KEY_TIM);
        _time_to_live   = jobj.getInt(KEY_TTL);
    }

    JsonPresence(String pid, LatLng l, String lbl, String snippet, String spacename, int ttl) {
        _uid = pid;
        _location = l;
        _label = lbl;
        _snippet = snippet;
        _spacename = spacename;
        _create_time = System.currentTimeMillis();
        _time_to_live = ttl;
    }

    @Override
    public LatLng getLocation() {
        return _location;
    }

    @Override
    public String getUID() {
        return _uid;
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
    public int getTimeToLive() {
        return _time_to_live;
    }

    /*
    @Override
    public void setSpaceName(String name) {

        _spacename = name;
    }
     */

    @Override
    public long getAgeInMillis() {
        return System.currentTimeMillis() - _create_time;
    }

    @Override
    public String toString() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(KEY_UID, _uid);
            jobj.put(KEY_LAT, _location.latitude);
            jobj.put(KEY_LON, _location.longitude);
            jobj.put(KEY_LBL, _label);
            jobj.put(KEY_SNP, _snippet);
            jobj.put(KEY_SPC, _spacename);
            jobj.put(KEY_TIM, _create_time);
            jobj.put(KEY_TTL, _time_to_live);

            return jobj.toString();
        } catch (JSONException e) {
            OeLog.e(e.toString(), e);
            return null;
        }
    }
}

