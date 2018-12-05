package com.example.android.myanime.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.myanime.data.AnimeContract.AnimeEntry;

/**
 * Created by Abhishek on 31-12-2017.
 */

public class AnimeProvider extends ContentProvider {


    /** URI matcher code for the content URI for the pets table */
    private static final int ANIME = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int ANIME_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(AnimeContract.CONTENT_AUTHORITY, AnimeContract.PATH_ANIME, ANIME);
        sUriMatcher.addURI(AnimeContract.CONTENT_AUTHORITY, AnimeContract.PATH_ANIME + "/#", ANIME_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = AnimeProvider.class.getSimpleName();

    private AnimeDbHelper mDbHelper;



    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new AnimeDbHelper(getContext());
        return false;
    }



    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ANIME:
                cursor = database.query(AnimeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ANIME_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = AnimeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(AnimeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType( Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ANIME:
                return AnimeEntry.CONTENT_LIST_TYPE;
            case ANIME_ID:
                return AnimeEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert( Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ANIME:
                return insertAnime(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertAnime(Uri uri, ContentValues values) {
        // Check that the title is not null
        String name = values.getAsString(AnimeEntry.COLUMN_ANIME_TITLE);
        if (name == null || name == "") {
            throw new IllegalArgumentException("Anime requires a title");
        }

        // Check the total ep
        Integer totalep = values.getAsInteger(AnimeEntry.COLUMN_ANIME_TOTAL_EP);
        if(totalep != null && totalep < 0){
            throw new IllegalArgumentException("totalep should be positive");
        }

        //Check Progress
        Integer progress = values.getAsInteger(AnimeEntry.COLUMN_ANIME_PROGRESS);
        if (progress != null && progress < 0 && progress > totalep){
            throw  new IllegalArgumentException("Invalid Progress");
        }

        //Checking status
        Integer status = values.getAsInteger(AnimeEntry.COLUMN_ANIME_STATUS);
        if(status == null || AnimeEntry.isValidStatus(status) == false){
            throw new IllegalArgumentException("Invalid Status");
        }

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = database.insert(AnimeEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //Number of deleted rows
        int deletedRows;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ANIME:
                // Delete all rows that match the selection and selection args
                deletedRows = database.delete(AnimeEntry.TABLE_NAME, selection, selectionArgs);
                if(deletedRows != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return deletedRows;
            case ANIME_ID:
                // Delete a single row given by the ID in the URI
                selection = AnimeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                deletedRows = database.delete(AnimeEntry.TABLE_NAME, selection, selectionArgs);
                if(deletedRows != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return deletedRows;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch(match) {
            case ANIME:
                return updateAnime(uri, values, selection, selectionArgs);
            case ANIME_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = AnimeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateAnime(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private int updateAnime(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // If the {@link AnimeEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(AnimeEntry.COLUMN_ANIME_TITLE)) {
            String name = values.getAsString(AnimeEntry.COLUMN_ANIME_TITLE);
            if (name == null) {
                throw new IllegalArgumentException("Anime requires a name");
            }
        }

        // If the {@link AnimeEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(AnimeEntry.COLUMN_ANIME_STATUS)) {
            Integer gender = values.getAsInteger(AnimeEntry.COLUMN_ANIME_STATUS);
            if (gender == null || !AnimeEntry.isValidStatus(gender)) {
                throw new IllegalArgumentException("Anime requires valid status");
            }
        }

        // If the {@link AnimeEntry#COLUMN_PET_totalep} key is present,
        // check that the totalep value is valid.
        if (values.containsKey(AnimeEntry.COLUMN_ANIME_TOTAL_EP)) {
            // Check that the totalep is greater than or equal to 0 kg
            Integer totalep = values.getAsInteger(AnimeEntry.COLUMN_ANIME_TOTAL_EP);
            if (totalep != null && totalep < 0) {
                throw new IllegalArgumentException("Anime requires valid total");
            }
        }
        
        if(values.containsKey(AnimeEntry.COLUMN_ANIME_PROGRESS)){
            Integer totalep = values.getAsInteger(AnimeEntry.COLUMN_ANIME_TOTAL_EP);
            Integer progress = values.getAsInteger(AnimeEntry.COLUMN_ANIME_PROGRESS);
            if (progress != null && progress < 0 && progress >totalep){
                throw  new IllegalArgumentException("Invalid Progress");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Number of rows updated
        int rowsUpdated = database.update(AnimeEntry.TABLE_NAME, values, selection, selectionArgs);

        //Notify if there is a change in data
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }
}
