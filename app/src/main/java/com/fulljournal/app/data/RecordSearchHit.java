package com.fulljournal.app.data;

/** Projection of a record_value match joined with its row and table, used for search results. */
public class RecordSearchHit {
    public long tableId;
    public String tableName;
    public long rowId;
    public long recordDate;
    public String matchedValue;
}
