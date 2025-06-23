package com.example.bookreader.data.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {
    private static DatabaseClient instance;
    private final BookDb appDatabase;

    private DatabaseClient(Context context) {
        appDatabase = Room.databaseBuilder(context.getApplicationContext(),
                        BookDb.class, "my-database")
                .fallbackToDestructiveMigration()
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public BookDb getAppDatabase() {
        return appDatabase;
    }
}
