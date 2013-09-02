/*
 * Copyright (c) 2013. Ed Sweeney.  All Rights Reserved.
 */

package com.onextent.oemap.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.oemap.R;

public class PresenceDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 6;
    public static final String PRESENCE_TABLE = "presence";
    public static final String PRESENCE_UID = "uid";
    public static final String PRESENCE_SPACE = "space";
    public static final String PRESENCE_DATA = "data";

    private static final String SQL_CREATE =
            "CREATE TABLE " + PRESENCE_TABLE +
                    " (_id TEXT PRIMARY KEY, " +
                    PRESENCE_UID + " TEXT, " +
                    PRESENCE_SPACE + " TEXT, " +
                    PRESENCE_DATA +" TEXT)";

    private static final String SQL_SPACE_IDX =
            "CREATE INDEX SPACE_INDEX ON " + PRESENCE_TABLE + " (" + PRESENCE_SPACE + ")";

    public PresenceDbHelper(Context context) {
        super(context, context.getString(R.string.db_name_presence), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE);
        sqLiteDatabase.execSQL(SQL_SPACE_IDX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PRESENCE_TABLE + ";");
        onCreate(sqLiteDatabase);
    }
}

