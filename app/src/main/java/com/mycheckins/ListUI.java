package com.mycheckins;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListUI extends Fragment {


    // Display the list ui
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_ui, container, false);


        // Get the items, 2 parallel array list is used, the other one is for display only
        final ArrayList<Item> items = new ArrayList<>();
        ArrayList<String> itemsDetails = new ArrayList<>();

        try {
            // Get all items from the database
            DBAdapter dbAdapter = new DBAdapter(getActivity());
            SQLiteDatabase database = dbAdapter.getReadableDatabase();
            Cursor cursor = database.rawQuery("select * from item", null);

            if(cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.id = cursor.getInt(cursor.getColumnIndex("id"));
                    item.title = cursor.getString(cursor.getColumnIndex("title"));
                    item.place = cursor.getString(cursor.getColumnIndex("place"));
                    item.details = cursor.getString(cursor.getColumnIndex("details"));
                    item.date = cursor.getString(cursor.getColumnIndex("date"));
                    item.location = cursor.getString(cursor.getColumnIndex("location"));
                    item.imageFilename = cursor.getString(cursor.getColumnIndex("image_filename"));

                    items.add(item);
                    itemsDetails.add(item.title + "\non " + item.date + "\nat " + item.place);
                } while(cursor.moveToNext());
            }

        } catch(Exception ex) {
            Log.e("List UI", ex.getMessage());
        }

        // Display all items
        ListView itemsListView = view.findViewById(R.id.items_list);
        ((ListView)view.findViewById(R.id.items_list)).setAdapter(new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, itemsDetails));

        // Make the items in the list allowed to be selected
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Pass the selected item to the item ui for viewing
                Globals.selectedItem = items.get(position);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrameLayout, new ItemUI(), "Item UI")
                        .commit();
            }
        });

        return view;
    }
}
