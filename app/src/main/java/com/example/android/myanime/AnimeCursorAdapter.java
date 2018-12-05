package com.example.android.myanime;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myanime.data.AnimeContract.AnimeEntry;

import java.io.File;

/**
 * Created by Abhishek on 02-01-2018.
 */

public class AnimeCursorAdapter extends CursorAdapter {

    public AnimeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView title_view = (TextView) view.findViewById(R.id.title_view);
        final TextView total_view = (TextView) view.findViewById(R.id.total_view);
        TextView progress_view = (TextView) view.findViewById(R.id.progress_view);
        TextView status_view = (TextView) view.findViewById(R.id.status_text_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
        Button updateButton = (Button) view.findViewById(R.id.update_button);


        //Get values from database
        final String title = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_TITLE));
        final String total = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_TOTAL_EP));
        final String progress = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_PROGRESS));
        int status = cursor.getInt(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_STATUS));
        final String imageUriString = cursor.getString(cursor.getColumnIndex(AnimeEntry.COLUMN_ANIME_IMAGE_URI));


        //Get uri and show image
        //Uri imageUri = Uri.parse(imageUriString);
        if (imageUriString != null){
            try {
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                // Log.d(TAG, String.valueOf(bitmap));
                imageView.setImageURI(Uri.parse(new File(imageUriString).toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //Set the text to the views
        title_view.setText(title);
        total_view.setText(total);
        progress_view.setText(progress);

        if (status == AnimeEntry.STATUS_CURRENTLY_WATCHING){
            status_view.setText(R.string.status_watching);
        }
        else if (status == AnimeEntry.STATUS_COMPLETED){
            status_view.setText(R.string.status_completed);
        }
        else{
            status_view.setText(R.string.status_plan_to_watch);
        }


        //When the +1 button is pressed
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progressUpdate = Integer.parseInt(progress);
                int totalCheck = Integer.parseInt(total);
                ContentValues values = new ContentValues();

                //Get the id of the button and set the Uri
                Uri currentUri = null;
                if (v != null) {
                    Object obj = v.getTag();
                    String st = obj.toString();
                    Long id = Long.parseLong(st);
                    currentUri = ContentUris.withAppendedId(AnimeEntry.CONTENT_URI, id);
                }

                //Check if the anime is completed
                if(progressUpdate == totalCheck){
                    Toast.makeText(context, "All episodes completed", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    progressUpdate = progressUpdate + 1;
                    values.put(AnimeEntry.COLUMN_ANIME_PROGRESS, progressUpdate);
                    context.getContentResolver().update(currentUri, values,
                            null, null);

                    //Check if all episodes are completed and change status to completed
                    if (progressUpdate == totalCheck){
                        values.put(AnimeEntry.COLUMN_ANIME_STATUS, AnimeEntry.STATUS_COMPLETED);
                        context.getContentResolver().update(currentUri, values,
                                null, null);
                    }
                }


            }
        });

        Object obj = cursor.getString(cursor.getColumnIndex(AnimeEntry._ID));
        updateButton.setTag(obj);

    }
}
