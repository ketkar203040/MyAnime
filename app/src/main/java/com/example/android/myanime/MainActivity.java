package com.example.android.myanime;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.myanime.data.AnimeContract.AnimeEntry;
import com.example.android.myanime.data.AnimeDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public AnimeDbHelper mAnimeDbHelper = new AnimeDbHelper(this);

    private static final int ANIME_LOADER = 0;

    AnimeCursorAdapter animeCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get permission if not granted
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1514);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view);

        animeCursorAdapter = new AnimeCursorAdapter(this, null);
        listView.setAdapter(animeCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnimeEntry.currentIdUri = ContentUris.withAppendedId(AnimeEntry.CONTENT_URI, id);
                //private static Uri sendUri = Uri.
                Intent i = new Intent(MainActivity.this, EditorActivity.class);
                i.setData(AnimeEntry.currentIdUri);
                startActivity(i);
            }
        });

        //displayAnime();

        getLoaderManager().initLoader(ANIME_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //displayAnime();
    }

    private void insertData(){
        // Gets the data repository in write mode
        SQLiteDatabase db = mAnimeDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(AnimeEntry.COLUMN_ANIME_TITLE, "Erased");
        values.put(AnimeEntry.COLUMN_ANIME_TOTAL_EP, 12);
        values.put(AnimeEntry.COLUMN_ANIME_PROGRESS, 6);
        values.put(AnimeEntry.COLUMN_ANIME_STATUS, AnimeEntry.STATUS_CURRENTLY_WATCHING);

        // Insert the new row, returning the primary key value of the new row
        //db.insert(AnimeEntry.TABLE_NAME, null, values);
        getContentResolver().insert(AnimeEntry.CONTENT_URI, values);

    }
/*
    private void displayAnime(){
        SQLiteDatabase db = mAnimeDbHelper.getReadableDatabase();

        String sql = "SELECT * FROM " + AnimeEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        try {

            TextView aniView = (TextView) findViewById(R.id.anime_text);
            aniView.setText("");

            while (cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(AnimeEntry._ID));
                String title = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_TITLE));
                String total = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_TOTAL_EP));
                String status = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_STATUS));

                aniView.append(("\n" + id + "   " + title + "   " + total + "   " + status));
            }
        }
        finally {
            cursor.close();
        }

    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu items
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.dummy_data :
                insertData();
                //displayAnime();
                return true;
            case R.id.delete_all:
                getContentResolver().delete(AnimeEntry.CONTENT_URI, null, null);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                AnimeEntry._ID,
                AnimeEntry.COLUMN_ANIME_TITLE,
                AnimeEntry.COLUMN_ANIME_TOTAL_EP,
                AnimeEntry.COLUMN_ANIME_PROGRESS,
                AnimeEntry.COLUMN_ANIME_STATUS,
                AnimeEntry.COLUMN_ANIME_IMAGE_URI
        };

        return new CursorLoader(this, AnimeEntry.CONTENT_URI, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        animeCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        animeCursorAdapter.swapCursor(null);
    }
}
