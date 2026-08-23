package com.fulljournal.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "record_row",
        foreignKeys = @ForeignKey(
                entity = RecordTable.class,
                parentColumns = "id",
                childColumns = "tableId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("tableId")}
)
public class RecordRow {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long tableId;

    /** Epoch millis, truncated to the start of the recorded day. Aggregation axis. */
    public long recordDate;

    public long createdAt;
}
