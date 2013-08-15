package com.onextent.oemap.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.onextent.oemap.R;

public class KvDbHelper extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 2;
    public static final String KV_TABLE = "kv";
    public static final String KV_KEY = "key";
    public static final String KV_VALUE = "value";

    private static final String SQL_CREATE =
            "CREATE TABLE " + KV_TABLE +
            " (_id INTEGER PRIMARY KEY, " +
            KV_KEY + " TEXT UNIQUE, " + KV_VALUE + " TEXT)";

    public KvDbHelper(Context context) {
        super(context, context.getString(R.string.db_name_preferences), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + KV_TABLE + ";");
    }
}

