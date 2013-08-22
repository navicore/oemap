package com.onextent.android.util;

import android.util.Log;

/**
 * Created by esweeney on 7/25/13.
 */
public class OeLog {

    private static final String TAG = "oemap";

    public static void d(String m) {

        Log.d(TAG, m);
    }

    public static void i(String m) {

        Log.i(TAG, m);
    }

    public static void w(String m) {

        Log.w(TAG, m);
    }

    public static void w(Throwable err) {

        Log.w(TAG, err.toString(), err);
    }

    public static void w(String m, Throwable err) {

        Log.w(TAG, m, err);
    }

    public static void e(Throwable e) {

        Log.e(TAG, e.toString(), e);
    }

    public static void e(String m) {

        Log.e(TAG, m);
    }

    public static void e(String m, Throwable err) {

        Log.e(TAG, m, err);
    }
}

