package com.onextent.oemap.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.oemap.R;

public class PresenceDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 2;
    public static final String PRESENCE_TABLE = "presence";
    public static final String PRESENCE_UID = "uid";
    public static final String PRESENCE_SPACE = "space";
    public static final String PRESENCE_DATA = "data";

    //ejs todo: constrain mus be key + map
    private static final String SQL_CREATE =
            "CREATE TABLE " + PRESENCE_TABLE +
            " (_id INTEGER PRIMARY KEY, " +
            PRESENCE_UID + " TEXT, " +
            PRESENCE_SPACE + " TEXT, " +
            PRESENCE_DATA +" TEXT, UNIQUE (" + PRESENCE_UID + ", " + PRESENCE_SPACE+ "))";

    public PresenceDbHelper(Context context) {
        super(context, context.getString(R.string.db_name_presence), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PRESENCE_TABLE + ";");
    }
}

