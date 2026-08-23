package com.fulljournal.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "record_value",
        foreignKeys = {
                @ForeignKey(
                        entity = RecordRow.class,
                        parentColumns = "id",
                        childColumns = "rowId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = RecordColumn.class,
                        parentColumns = "id",
                        childColumns = "columnId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("rowId"), @Index("columnId")}
)
public class RecordValue {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long rowId;

    public long columnId;

    /** Raw text value; parsed according to the owning column's type. */
    @NonNull
    public String value = "";
}
