package com.fulljournal.app.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "record_column",
        foreignKeys = @ForeignKey(
                entity = RecordTable.class,
                parentColumns = "id",
                childColumns = "tableId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("tableId")}
)
public class RecordColumn {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long tableId;

    @NonNull
    public String name = "";

    @NonNull
    public ColumnType type = ColumnType.NUMBER;

    public int sortOrder;

    /** Comma separated options, only used when type == CHOICE. */
    @Nullable
    public String choiceOptions;
}
