package com.example.nautilusapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.Simplified_User.CREATE_TABLE);
        db.execSQL(DatabaseContract.User.CREATE_TABLE);
        db.execSQL(DatabaseContract.Friend.CREATE_TABLE);
        db.execSQL(DatabaseContract.Picture.CREATE_TABLE);
        db.execSQL(DatabaseContract.Discussion.CREATE_TABLE);
        db.execSQL(DatabaseContract.Talk_in.CREATE_TABLE);
        db.execSQL(DatabaseContract.Message.CREATE_TABLE);
        db.execSQL(DatabaseContract.Species.CREATE_TABLE);
        db.execSQL(DatabaseContract.Observation.CREATE_TABLE);
        db.execSQL(DatabaseContract.Note.CREATE_TABLE);
        db.execSQL(DatabaseContract.is_in_observation.CREATE_TABLE);
        db.execSQL(DatabaseContract.is_in_species_page.CREATE_TABLE);

    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.Simplified_User.DELETE_TABLE);
        db.execSQL(DatabaseContract.User.DELETE_TABLE);
        db.execSQL(DatabaseContract.Friend.DELETE_TABLE);
        db.execSQL(DatabaseContract.Picture.DELETE_TABLE);
        db.execSQL(DatabaseContract.Discussion.DELETE_TABLE);
        db.execSQL(DatabaseContract.Talk_in.DELETE_TABLE);
        db.execSQL(DatabaseContract.Message.DELETE_TABLE);
        db.execSQL(DatabaseContract.Species.DELETE_TABLE);
        db.execSQL(DatabaseContract.Observation.DELETE_TABLE);
        db.execSQL(DatabaseContract.Note.DELETE_TABLE);
        db.execSQL(DatabaseContract.is_in_observation.DELETE_TABLE);
        db.execSQL(DatabaseContract.is_in_species_page.DELETE_TABLE);

        onCreate(db);
    }
}
