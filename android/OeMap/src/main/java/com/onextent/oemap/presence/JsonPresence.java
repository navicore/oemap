package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.util.OeLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonPresence implements Presence {

    private static final String KEY_UID = "uid";
    private static final String KEY_LOC = "location";
    private static final String KEY_CRD = "coordinates";
    private static final String KEY_LBL = "label";
    private static final String KEY_SNP = "snippit";
    private static final String KEY_SPC = "space";
    private static final String KEY_TIM = "time";
    private static final String KEY_TTL = "ttl";
    private static final int LATITUDE_IDX = 1;
    private static final int LONGITUDE_IDX = 0;
    private final String _uid, _label, _snippet;
    private final LatLng _location;
    private final long  _create_time;
    private final int   _time_to_live;
    private final String _spacename;

    JsonPresence(JSONObject jobj) throws JSONException {

        _uid = jobj.getString(KEY_UID);

        //see http://www.geojson.org for standard.  for some reason lon comes before lat
        if (jobj.has(KEY_LOC)) {
            JSONObject loc = jobj.getJSONObject(KEY_LOC);
            JSONArray coords = loc.getJSONArray(KEY_CRD);
            _location = new LatLng(coords.getDouble(LATITUDE_IDX), coords.getDouble(LONGITUDE_IDX));
        } else {
            _location = null;
        }

        _label          = jobj.getString(KEY_LBL);
        _snippet        = jobj.getString(KEY_SNP);
        _spacename      = jobj.getString(KEY_SPC);
        _create_time    = jobj.getLong(KEY_TIM);
        _time_to_live   = jobj.getInt(KEY_TTL);
    }

    JsonPresence(String json) throws JSONException {
        this(new JSONObject(json));
    }

    JsonPresence(String pid, LatLng l, String lbl, String snippet, String spacename, int ttl) {
        _uid            = pid;
        _location       = l;
        _label          = lbl;
        _snippet        = snippet;
        _spacename      = spacename;
        _create_time    = System.currentTimeMillis();
        _time_to_live   = ttl;
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

    @Override
    public long getAgeInMillis() {
        return System.currentTimeMillis() - _create_time;
    }

    @Override
    public String toString() {
        JSONObject jobj = new JSONObject();
        try {
            jobj.put(KEY_UID, _uid);

            if (_location != null) {

                JSONObject loc = new JSONObject();
                jobj.put(KEY_LOC, loc);
                loc.put("type", "Point");
                JSONArray coords = new JSONArray();
                loc.put(KEY_CRD, coords);
                coords.put(LONGITUDE_IDX, _location.longitude);
                coords.put(LATITUDE_IDX, _location.latitude);
            }

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

