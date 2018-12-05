package com.example.android.myanime.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.myanime.data.AnimeContract.AnimeEntry;

/**
 * Created by Abhishek on 31-12-2017.
 */

public class AnimeDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "anime.db";

    public AnimeDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);}


    @Override
    public void onCreate(SQLiteDatabase db) {

        //String for creating anime table
        String SQL_CREATE_ANIME_TABLE = "CREATE TABLE " + AnimeEntry.TABLE_NAME + " ("
                + AnimeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AnimeEntry.COLUMN_ANIME_TITLE + " TEXT NOT NULL, "
                + AnimeEntry.COLUMN_ANIME_TOTAL_EP + " INTEGER NOT NULL, "
                + AnimeEntry.COLUMN_ANIME_PROGRESS + " INTEGER NOT NULL DEFAULT 0, "
                + AnimeEntry.COLUMN_ANIME_IMAGE_URI + " TEXT, "
                + AnimeEntry.COLUMN_ANIME_STATUS + " INTEGER NOT NULL DEFAULT 0);";

        //Execute SQL statement
        db.execSQL(SQL_CREATE_ANIME_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
