package com.fulljournal.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.fulljournal.app.data.entity.RecordTable;

import java.util.List;

@Dao
public interface RecordTableDao {

    @Insert
    long insert(RecordTable table);

    @Delete
    void delete(RecordTable table);

    @Query("SELECT * FROM record_table ORDER BY createdAt DESC")
    LiveData<List<RecordTable>> observeAll();

    @Query("SELECT * FROM record_table WHERE id = :id")
    RecordTable getById(long id);

    @Query("SELECT * FROM record_table WHERE name LIKE '%' || :query || '%'")
    List<RecordTable> search(String query);
}
