package com.fulljournal.app.data.dao;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fulljournal.app.data.entity.RecordRow;

import java.util.List;

@Dao
public interface RecordRowDao {

    @Insert
    long insert(RecordRow row);

    @Update
    void update(RecordRow row);

    @Delete
    void delete(RecordRow row);

    @Query("SELECT * FROM record_row WHERE tableId = :tableId ORDER BY recordDate DESC, id DESC")
    LiveData<List<RecordRow>> observeForTable(long tableId);

    @Query("SELECT * FROM record_row WHERE tableId = :tableId ORDER BY recordDate ASC, id ASC")
    List<RecordRow> getForTableSync(long tableId);

    @Query("SELECT * FROM record_row ORDER BY createdAt DESC LIMIT 1")
    @Nullable
    RecordRow getMostRecentSync();
}
