package com.example.me.libupdater.history;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface UpdateEventDao {
    @Insert
    void insert(UpdateEvent updateEvent);

    @Delete
    void delete(UpdateEvent updateEvent);

    @Query("DELETE FROM UpdateEvents")
    void deleteAll();

    @Query("SELECT * from UpdateEvents ORDER BY versionName ASC")
    LiveData<List<UpdateEvent>> getAllUpdateEvents();
}
