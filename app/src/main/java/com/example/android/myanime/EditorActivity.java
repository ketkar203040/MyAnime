package com.example.android.myanime;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myanime.data.AnimeContract.AnimeEntry;

import java.io.File;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    //Get all the fields in the activity

    //Add image view
    ImageView poster_image;

    EditText title_edit;

    //Total button
    RelativeLayout total_button;
    EditText total_text_view;

    //Progress button
    RelativeLayout progress_button;
    TextView progress_text_view;

    private Spinner mStatusSpinner = null;

    private int mStatus = 0;

    private int mProgress = 0;

    private int mTotal = 0;

    private static final int EXISTING_ANIME_LOADER = 0;

    private int PICK_IMAGE_REQUEST = 1002;

    private String absolute_path = null;

    boolean mAnimeChanged;

    private Uri currentAnimeUri;

    private NumberPicker np = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        Intent intent = getIntent();
        currentAnimeUri = intent.getData();

        if(currentAnimeUri == null){
            setTitle(R.string.editor_activity_title_new_anime);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        else{
            setTitle(R.string.editor_activity_title_edit_anime);
            getLoaderManager().initLoader(EXISTING_ANIME_LOADER, null, this);
            //Toast.makeText(this, currentAnimeUri.toString(), Toast.LENGTH_SHORT).show();
        }

        title_edit = (EditText) findViewById(R.id.edit_title_name);
        total_button = (RelativeLayout) findViewById(R.id.total_button);
        total_text_view = (EditText) findViewById(R.id.total_editor_view);
        progress_button = (RelativeLayout) findViewById(R.id.progress_button);
        progress_text_view = (TextView) findViewById(R.id.progress_editor_view);
        mStatusSpinner = (Spinner) findViewById(R.id.spinner_status);
        poster_image = (ImageView) findViewById(R.id.add_image_view);

        //To prevent keyboard from opening when activity is opened
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Make numberpicker appear when clicked
        progress_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPicker();
            }
        });

        //open gallery to add image
        poster_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });
        poster_image.setOnTouchListener(mTouchListener);


        //Change listener to change the max value in number picker when total is changed
        total_text_view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s){
                if(!TextUtils.isEmpty(s)){
                    mTotal = Integer.parseInt(total_text_view.getText().toString());
                }
            }
        });

        setupSpinner();
    }

    //OnTouchListener
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mAnimeChanged = true;
            return false;
        }
    };

    //Opens image chooser
    private void openImageChooser(){

        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            absolute_path = getRealPathFromURI(this, uri);
            //Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
            //Log.v("Absolute Uri", absoluteuri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                poster_image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

/*
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor == null) {
                return contentUri.getPath();
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        finally {
            cursor.close();
        }

    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_save :
                saveAnime();
                finish();
                return true;
            case R.id.action_delete:
                getContentResolver().delete(currentAnimeUri, null, null);
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentAnimeUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_status_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mStatusSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.status_watching))) {
                        mStatus = AnimeEntry.STATUS_CURRENTLY_WATCHING;
                    } else if (selection.equals(getString(R.string.status_completed))) {
                        mStatus = AnimeEntry.STATUS_COMPLETED;
                    } else {
                        mStatus = AnimeEntry.STATUS_WANT_TO_WATCH;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mStatus = 0; // Unknown
            }
        });
    }

    //Saving the anime
    private void saveAnime(){

        //Get values from the Editor
        String titleEdit = title_edit.getText().toString().trim();
        String totalEdit = total_text_view.getText().toString().trim();
        String progressEdit = progress_text_view.getText().toString().trim();
        int statusEdit = mStatus;

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(AnimeEntry.COLUMN_ANIME_TITLE, titleEdit);
        values.put(AnimeEntry.COLUMN_ANIME_STATUS, statusEdit);

        //Check if image was changed
        if (absolute_path != null){
                values.put(AnimeEntry.COLUMN_ANIME_IMAGE_URI, absolute_path);
        }



        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 1 by default.
        int total = 1;
        if (!TextUtils.isEmpty(totalEdit)) {
            total = Integer.parseInt(totalEdit);
        }
        values.put(AnimeEntry.COLUMN_ANIME_TOTAL_EP, total);

        int progress = 0;
        if (!TextUtils.isEmpty(progressEdit)) {
            progress = Integer.parseInt(progressEdit);
        }
        values.put(AnimeEntry.COLUMN_ANIME_PROGRESS, progress);


        Uri newUri = null;
        int rowsUpdated = 0;

        if(currentAnimeUri == null){
            if(TextUtils.isEmpty(titleEdit) && TextUtils.isEmpty(totalEdit) && TextUtils.isEmpty(progressEdit)
                    && statusEdit == AnimeEntry.STATUS_CURRENTLY_WATCHING){
                return;
            }

            newUri = getContentResolver().insert(AnimeEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_anime_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_anime_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            rowsUpdated = getContentResolver().update(currentAnimeUri, values, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsUpdated == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_update_anime_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_anime_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        return new CursorLoader(this, currentAnimeUri, projection,
                null, null, null);
    }


    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        if(data.moveToFirst()) {
            //Get the values of the current pet
            String title = data.getString(data.getColumnIndex(AnimeEntry.COLUMN_ANIME_TITLE));
            String total = data.getString(data.getColumnIndex(AnimeEntry.COLUMN_ANIME_TOTAL_EP));
            int progress = data.getInt(data.getColumnIndex(AnimeEntry.COLUMN_ANIME_PROGRESS));
            int status = data.getInt(data.getColumnIndex(AnimeEntry.COLUMN_ANIME_STATUS));
            String imageUriString = data.getString(data.getColumnIndex(AnimeEntry.COLUMN_ANIME_IMAGE_URI));


            //Get uri and show image
            //Uri imageUri = Uri.parse(imageUriString);
            if (imageUriString != null){
                try {
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                    // Log.d(TAG, String.valueOf(bitmap));
                    poster_image.setImageURI(Uri.parse(new File(imageUriString).toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mTotal = Integer.parseInt(total);
            mProgress = progress;


            //Put the current pet values in the data field
            title_edit.setText(title);
            total_text_view.setText(total);
            progress_text_view.setText(Integer.toString(progress));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (status) {
                case AnimeEntry.STATUS_COMPLETED:
                    mStatusSpinner.setSelection(1);
                    break;
                case AnimeEntry.STATUS_WANT_TO_WATCH:
                    mStatusSpinner.setSelection(2);
                    break;
                default:
                    mStatusSpinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        title_edit.setText("");
        total_text_view.setText("");
        progress_text_view.setText("");
        mStatusSpinner.setSelection(0);

    }


    /* Number Picker for Progress */
    private void showNumberPicker(){
        final Dialog d = new Dialog(EditorActivity.this);
        d.setTitle("Episode Picker");
        d.setContentView(R.layout.number_picker_dialog);
        TextView set_button = (TextView) d.findViewById(R.id.set_button);
        TextView cancel_button = (TextView) d.findViewById(R.id.cancel_button);
        np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(mTotal);
        np.setMinValue(0);
        np.setValue(mProgress);
        np.setWrapSelectorWheel(true);
        // np.setOnValueChangedListener(this);

        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = np.getValue();
                progress_text_view.setText(Integer.toString(mProgress));

                //Check progress and set status spinner accordingly
                if (mProgress == mTotal){
                    mStatusSpinner.setSelection(1);
                }
                else if (mProgress < mTotal && mProgress > 0 ) {
                    mStatusSpinner.setSelection(0);
                }
                else{
                    mStatusSpinner.setSelection(2);
                }
                d.dismiss();
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        d.show();
    }

}
