/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.presence;

import com.google.android.gms.maps.model.LatLng;
import com.onextent.android.util.OeLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class JsonPresence implements Presence {

    private static final String KEY_UID = "uid";
    private static final String KEY_PID = "pid";
    private static final String KEY_LOC = "location";
    private static final String KEY_CRD = "coordinates";
    private static final String KEY_LBL = "label";
    private static final String KEY_SNP = "snippit";
    private static final String KEY_SPC = "space";
    private static final String KEY_TIM = "time";
    private static final String KEY_TTL = "ttl";
    private static final String KEY_EXP = "exp";
    private static final int LATITUDE_IDX = 1;
    private static final int LONGITUDE_IDX = 0;
    static final private SimpleDateFormat _dateFormatGmt = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");

    static {
        _dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private final String _uid, _label, _snippet, _pid;
    private final LatLng _location;
    private final long _create_time;
    private final int _time_to_live;
    private final String _spacename;
    private final Date _lease;

    private int _remote_id_type = Presence.PUSH_TYPE_NONE;
    private String _remote_id;

    JsonPresence(JSONObject jobj) throws PresenceException {

        try {
            _uid = jobj.getString(KEY_UID);
            _spacename = jobj.getString(KEY_SPC);

            if (jobj.has(KEY_PID)) { //legacy pids  //todo: remove before public beta
                _pid = jobj.getString(KEY_PID);
            } else {
                _pid = makePid(_uid, _spacename);
            }

            //see http://www.geojson.org for standard.  for some reason lon comes before lat
            if (jobj.has(KEY_LOC)) {
                JSONObject loc = jobj.getJSONObject(KEY_LOC);
                JSONArray coords = loc.getJSONArray(KEY_CRD);
                _location = new LatLng(coords.getDouble(LATITUDE_IDX), coords.getDouble(LONGITUDE_IDX));
            } else {
                _location = null;
            }

            _label = jobj.getString(KEY_LBL);
            _snippet = jobj.getString(KEY_SNP);
            long tmp_time = 0;
            if (jobj.has(KEY_TIM)) {
                try {
                    tmp_time = jobj.getLong(KEY_TIM);
                } catch (JSONException ex) {
                    //OeLog.w("bad KEY_TIM: " + ex);  //ejs todo: fix test code on server
                }
            }

            _create_time = tmp_time;
            _time_to_live = jobj.getInt(KEY_TTL);
            if (jobj.has(KEY_EXP)) {

                String gmt = jobj.getString(KEY_EXP);
                Calendar gmtc = ISO8601.toCalendar(gmt);
                _lease = gmtc.getTime();
            } else {
                _lease = null;
            }
        } catch (JSONException e) {
            throw new PresenceException(e);
        } catch (ParseException e) {
            throw new PresenceException(e);
        }
    }

    public static String makePid(String uid, String space) {
        StringBuffer b = new StringBuffer();
        b.append(uid);
        b.append('_');
        b.append(space);
        return b.toString();
    }

    JsonPresence(String json) throws PresenceException, JSONException {
        this(new JSONObject(json));
    }

    JsonPresence(String uid, LatLng l, String lbl, String snippet, String spacename, int ttl, Date lease) {
        _uid = uid;
        _lease = lease;
        _location = l;
        _label = lbl;
        _snippet = snippet;
        _spacename = spacename;
        _create_time = System.currentTimeMillis();
        _time_to_live = ttl;
        StringBuffer b = new StringBuffer();
        b.append(getUID());
        b.append('_');
        b.append(getSpaceName());
        _pid = b.toString();
    }

    public JsonPresence(String uid, String spacename) {
        this(uid, null, null, null, spacename, Presence.NONE, null);
    }

    @Override
    public LatLng getLocation() {
        return _location;
    }

    @Override
    public String getPID() {
        return _pid;
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
    public int getRemoteIdType() {
        return _remote_id_type;
    }

    @Override
    public int setRemoteIdType(int rid) {
        return _remote_id_type = rid;
    }

    @Override
    public String getRemoteId() {
        return _remote_id;
    }

    @Override
    public void setRemoteid(String rid) {
        _remote_id = rid;
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
            jobj.put(KEY_PID, _pid);

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
            if (_lease != null) {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(_lease);
                jobj.put(KEY_EXP, ISO8601.fromCalendar(calendar));
            }

            return jobj.toString();
        } catch (JSONException e) {
            OeLog.e(e.toString(), e);
            return null;
        }
    }

    /*
    @Override
    public int hashCode() {
        return getSpaceName().hashCode() +  getUID().hashCode() + 5000;
    }
     */

    /**
     * Helper class for handling ISO 8601 strings of the following format:
     * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
     */
    public static final class ISO8601 {
        /**
         * Transform Calendar to ISO 8601 string.
         */
        public static String fromCalendar(final Calendar calendar) {
            Date date = calendar.getTime();
            String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .format(date);
            return formatted.substring(0, 22) + ":" + formatted.substring(22);
        }

        /**
         * Get current date and time formatted as ISO 8601 string.
         */
        public static String now() {
            return fromCalendar(GregorianCalendar.getInstance());
        }

        /**
         * Transform ISO 8601 string to Calendar.
         */
        public static Calendar toCalendar(final String iso8601string)
                throws ParseException, ParseException {
            Calendar calendar = GregorianCalendar.getInstance();
            String s = iso8601string.replace("Z", "+00:00");
            try {
                s = s.substring(0, 22) + s.substring(23);
            } catch (IndexOutOfBoundsException e) {
                throw new ParseException("Invalid length", 0);
            }
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
            calendar.setTime(date);
            return calendar;
        }
    }
}

