package com.fulljournal.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "record_table")
public class RecordTable {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    public long createdAt;
}
