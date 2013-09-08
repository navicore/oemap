/*
 * Copyright (c) 2013. Ed Sweeney, All Rights Reserved
 */

package com.onextent.oemap.provider;

import com.onextent.android.util.OeLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by esweeney on 9/7/13.
 */
public class BaseProviderHelper {
    public static final String ENCODING = "UTF8";

    protected static String decode(String safe_id) {

        String id;
        try {
            id = URLDecoder.decode(safe_id, ENCODING);
        } catch (UnsupportedEncodingException e) {
            OeLog.w(e);
            id = safe_id;
        }
        return id;
    }

    protected static String encode(String id) {

        String safe_id;
        try {
            safe_id = URLEncoder.encode(id, ENCODING);
        } catch (UnsupportedEncodingException e) {
            OeLog.w(e);
            safe_id = id;
        }
        return safe_id;
    }
}
