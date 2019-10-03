package com.mycheckins;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter extends SQLiteOpenHelper {

    // Create a database adapter
    public DBAdapter(Context context) {
        super(context, "items.db", null, 1);
    }

    // Create the database tables needed
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("create table item (" +
                "id integer primary key autoincrement," +
                "title varchar(100) not null," +
                "place varchar(100) not null," +
                "details varchar(100) not null," +
                "date varchar(20) not null," +
                "location varchar(100) not null," +
                "image_filename VARCHAR(100) not null)");
    }

    // Perform a database upgrade if new version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion == newVersion)
            return;

        database.execSQL("drop table if exists item");
        onCreate(database);
    }
}
