package com.mycheckins;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // Start the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadListUI(null);
    }

    // Show the item UI fragment
    public void loadListUI(View view) {
        ListUI ui = new ListUI();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrameLayout, ui, "List UI")
                .commit();
    }

    // Show the fragment for creating a new item
    public void loadItemUI(View view) {
        Globals.selectedItem = null;
        Globals.capturedImageBitmap = null;
        Globals.capturedLocation = null;

        ItemUI ui = new ItemUI();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrameLayout, ui, "Item UI")
                .commit();
    }

    // Set the date on the Item UI by letting the user choose a date
    public void setItemDate(View view) {
        final Button dateButton = findViewById(R.id.set_date_button);
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                dateButton.setText(month + "/" + day + "/" + year);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Add a new item to the database
    public void addItem(View view) {
        Item item = new Item();
        item.title = ((EditText)findViewById(R.id.title_field)).getText().toString().trim();
        item.place = ((EditText)findViewById(R.id.place_field)).getText().toString().trim();
        item.details = ((EditText)findViewById(R.id.details_field)).getText().toString().trim();
        item.date = ((Button)findViewById(R.id.set_date_button)).getText().toString().trim();
        item.location = ((TextView)findViewById(R.id.location_field)).getText().toString().trim();
        item.imageFilename = "" + System.currentTimeMillis();

        // Make sure that values are provided
        if(item.title.isEmpty() || item.place.isEmpty() || item.details.isEmpty() || item.date.equalsIgnoreCase("Set Date") || Globals.capturedImageBitmap == null  || Globals.capturedLocation == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please complete the form and don't leave anything blank.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();

            return;
        }

        // Save it into the database
        try {
            DBAdapter dbAdapter = new DBAdapter(this);
            SQLiteDatabase database = dbAdapter.getWritableDatabase();
            database.beginTransaction();

            ContentValues values = new ContentValues();
            values.put("title", item.title);
            values.put("place", item.place);
            values.put("details", item.details);
            values.put("date", item.date);
            values.put("location", item.location);
            values.put("image_filename", item.imageFilename);

            database.insertOrThrow("item", null, values);

            FileOutputStream outFile = openFileOutput(item.imageFilename, Context.MODE_PRIVATE);
            Globals.capturedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outFile);
            outFile.close();

            database.setTransactionSuccessful();

            Globals.capturedImageBitmap = null;
            Globals.capturedLocation = null;
            loadListUI(null);
            Toast.makeText(this, "Item added.", Toast.LENGTH_SHORT).show();
            database.endTransaction();
        } catch(Exception e) {
            Log.e("Add Item", e.getMessage());
        }
    }

    // Delete an item
    public void deleteItem(View view) {
        if(Globals.selectedItem == null)
            return;

        try {
            DBAdapter dbAdapter = new DBAdapter(this);
            SQLiteDatabase database = dbAdapter.getWritableDatabase();
            database.beginTransaction();
            database.delete("item", "id=?", new String[]{ "" + Globals.selectedItem.id });
            database.setTransactionSuccessful();
            database.endTransaction();

            Globals.selectedItem = null;
            loadListUI(null);
            Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Log.e("Delete Item", e.getMessage());
        }
    }

    // Trigger and open the camera
    public void takePicture(View view) {
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 1);
    }

    // Show the maps activity
    public void loadItemMapLocation(View view) {
        if(Globals.capturedLocation == null) {
            Toast.makeText(this, "Location has not been initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(this, ItemMapLocationActivity.class));
    }

    // Show the help activity
    public void loadHelp(View view) {
        startActivity(new Intent(this, WebViewActivity.class));
    }

    // When an activity is done, this is called. This project only expects to return a picture
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
            return;

        Globals.capturedImageBitmap = (Bitmap) data.getExtras().get("data");
        ((ImageView)findViewById(R.id.image_field)).setImageBitmap(Globals.capturedImageBitmap);
    }

    // Wait for the permission grant
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        loadListUI(null);
    }
}
