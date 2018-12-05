package com.example.android.myanime.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Abhishek on 31-12-2017.
 */

public class AnimeContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.myanime";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ANIME = "animelist";

    public static final class AnimeEntry implements BaseColumns{

        public final static String TABLE_NAME = "animelist";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ANIME);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ANIME;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ANIME;


        //Column names in the table
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ANIME_TITLE = "title";
        public final static String COLUMN_ANIME_TOTAL_EP = "totalep";
        public final static String COLUMN_ANIME_PROGRESS = "progress";
        public final static String COLUMN_ANIME_STATUS = "status";
        public final static String COLUMN_ANIME_IMAGE_URI = "image";

        //Possiblr values for Status
        public final static int STATUS_CURRENTLY_WATCHING = 0;
        public final static int STATUS_COMPLETED = 1;
        public final static int STATUS_WANT_TO_WATCH = 2;

        public static Uri currentIdUri = null;

        //Status Strings
        public static boolean isValidStatus(int status){
            if (status == STATUS_CURRENTLY_WATCHING || status == STATUS_COMPLETED || status == STATUS_WANT_TO_WATCH)
                return true;
            else
                return false;
        }
    }
}
