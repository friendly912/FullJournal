package com.fulljournal.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.fulljournal.app.data.entity.RecordColumn;

import java.util.List;

@Dao
public interface RecordColumnDao {

    @Insert
    void insertAll(List<RecordColumn> columns);

    @Query("SELECT * FROM record_column WHERE tableId = :tableId ORDER BY sortOrder ASC")
    LiveData<List<RecordColumn>> observeForTable(long tableId);

    @Query("SELECT * FROM record_column WHERE tableId = :tableId ORDER BY sortOrder ASC")
    List<RecordColumn> getForTableSync(long tableId);
}
