package com.example.me.libupdater.history;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "UpdateEvents")
public class UpdateEvent {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "eventId")
    private int id;

    @ColumnInfo(name = "versionName")
    private String versionName;

    public UpdateEvent(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return this.versionName;
    }
}
