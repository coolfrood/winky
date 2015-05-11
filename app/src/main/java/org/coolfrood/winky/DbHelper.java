package org.coolfrood.winky;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "org.coolfrood.winky.db";


    private static final String SQL_CREATE_DEVICE_TABLE =
            "CREATE TABLE " + DeviceDb.TABLE + " (" +
            DeviceDb.ID + " INTEGER PRIMARY KEY, " +
            DeviceDb.NAME + " TEXT, " +
            DeviceDb.POWERED + " INTEGER, " +
            DeviceDb.TAGS + " TEXT NOT NULL)";

    private static final String SQL_CREATE_TAG_TABLE =
            "CREATE TABLE " + TagDb.TABLE + " (" +
            TagDb.ID + " INTEGER PRIMARY KEY, " +
            TagDb.NAME + " TEXT, " +
            TagDb.IGNORED + " INTEGER)";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DEVICE_TABLE);
        db.execSQL(SQL_CREATE_TAG_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeviceDb.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TagDb.TABLE);
        onCreate(db);
    }
}
