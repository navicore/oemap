/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.oemap.R;


public class SpaceDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 14;

    public static final String SPACE_TABLE = "spaces";

    public static final String LEASE            = "lease";
    public static final String NAME             = "name";
    public static final String SIZE_IN_METERS   = "nmeters";
    public static final String SIZE_IN_POINTS   = "points";
    public static final String TYPE             = "type";
    public static final String URI              = "uri";

    private static final String SQL_CREATE = "CREATE TABLE " +
        SPACE_TABLE +
            " (_id TEXT PRIMARY KEY, "  +
            NAME + " TEXT,"             +
            URI + " URI,"               +
            LEASE + " INTEGER,"         +
            TYPE + " INTEGER,"          +
            SIZE_IN_METERS + " INTEGER,"+
            SIZE_IN_POINTS + " INTEGER" +
            ")";

    private static final String SQL_NAME_IDX =
            "CREATE INDEX SPACE_INDEX ON " + SPACE_TABLE + " (" + NAME + ")";

    public SpaceDbHelper(Context context) {
        super(context, context.getString(R.string.db_name_spaces), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE);
        sqLiteDatabase.execSQL(SQL_NAME_IDX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SPACE_TABLE + ";");
        onCreate(sqLiteDatabase);
    }
}

