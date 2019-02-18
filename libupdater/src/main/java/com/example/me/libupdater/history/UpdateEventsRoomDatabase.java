package com.example.me.libupdater.history;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UpdateEvent.class}, version = 1)
public abstract class UpdateEventsRoomDatabase extends RoomDatabase {
    public abstract UpdateEventDao updateEventDao();

    private static volatile UpdateEventsRoomDatabase INSTANCE;

    static UpdateEventsRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UpdateEventsRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UpdateEventsRoomDatabase.class, "word_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
