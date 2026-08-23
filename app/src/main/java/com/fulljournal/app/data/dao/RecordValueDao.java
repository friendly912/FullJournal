package com.fulljournal.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.fulljournal.app.data.RecordSearchHit;
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

    @Query("SELECT rt.id AS tableId, rt.name AS tableName, rr.id AS rowId, rr.recordDate AS recordDate, " +
            "rv.value AS matchedValue " +
            "FROM record_value rv " +
            "INNER JOIN record_row rr ON rv.rowId = rr.id " +
            "INNER JOIN record_table rt ON rr.tableId = rt.id " +
            "WHERE rv.value LIKE '%' || :query || '%' " +
            "ORDER BY rr.recordDate DESC")
    List<RecordSearchHit> search(String query);
}
