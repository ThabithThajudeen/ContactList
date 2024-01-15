package com.example.tutorial4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contactDb.db";
    private static final int DATABASE_VERSION = 2;

    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ContactContract.ContactEntry.TABLE_NAME + " (" +
                        ContactContract.ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +  // Added AUTOINCREMENT here
                        ContactContract.ContactEntry.COLUMN_NAME + " TEXT," +
                        ContactContract.ContactEntry.COLUMN_PHONE + " TEXT," +
                        ContactContract.ContactEntry.COLUMN_EMAIL + " TEXT," +
                        ContactContract.ContactEntry.COLUMN_IMAGE_PATH + " TEXT)";

        db.execSQL(SQL_CREATE_ENTRIES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + ContactContract.ContactEntry.TABLE_NAME + " ADD COLUMN " + ContactContract.ContactEntry.COLUMN_IMAGE_PATH + " TEXT");
        }
}
}
