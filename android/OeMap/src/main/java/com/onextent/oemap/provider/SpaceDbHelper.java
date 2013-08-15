package com.onextent.oemap.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SpaceDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 1;
    private SQLiteDatabase _db;
    public static final String SPACE_TABLE = "spaces";
    public static final String SPACE_NAME = "spacename";
    public static final String DB_NAME = "oemap_spacename_db";

    private static final String SQL_CREATE = "CREATE TABLE " +
        SPACE_TABLE +                       // Table's name
        " (" +                           // The columns in the table
        " _id INTEGER PRIMARY KEY, " +
        " " + SPACE_NAME + " TEXT)";

    public SpaceDbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SPACE_TABLE + ";");
    }
}

