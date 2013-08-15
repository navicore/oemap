package com.onextent.oemap.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.oemap.R;


public class SpaceDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    public static final String SPACE_TABLE = "spaces";
    public static final String SPACE_NAME = "spacename";

    private static final String SQL_CREATE = "CREATE TABLE " +
        SPACE_TABLE + " (_id INTEGER PRIMARY KEY, " + SPACE_NAME + " TEXT)";

    public SpaceDbHelper(Context context) {
        super(context, context.getString(R.string.db_name_spaces), null, DATABASE_VERSION);
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

