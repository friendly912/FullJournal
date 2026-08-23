package com.fulljournal.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.fulljournal.app.data.entity.RecordValue;

import java.util.List;

@Dao
public interface RecordValueDao {

    @Insert
    void insertAll(List<RecordValue> values);

    @Query("SELECT * FROM record_value WHERE rowId IN (:rowIds)")
    List<RecordValue> getForRows(List<Long> rowIds);

    @Query("DELETE FROM record_value WHERE rowId = :rowId")
    void deleteForRow(long rowId);
}
