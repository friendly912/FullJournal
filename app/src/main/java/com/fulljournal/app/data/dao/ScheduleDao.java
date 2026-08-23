package com.fulljournal.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fulljournal.app.data.entity.ScheduleEvent;

import java.util.List;

@Dao
public interface ScheduleDao {

    @Insert
    long insert(ScheduleEvent event);

    @Insert
    List<Long> insertAll(List<ScheduleEvent> events);

    @Update
    void update(ScheduleEvent event);

    @Delete
    void delete(ScheduleEvent event);

    @Query("SELECT * FROM schedule_event WHERE startAt >= :dayStart AND startAt < :dayEnd ORDER BY allDay DESC, startAt ASC")
    LiveData<List<ScheduleEvent>> observeEventsForDay(long dayStart, long dayEnd);

    @Query("SELECT * FROM schedule_event WHERE startAt >= :dayStart AND startAt < :dayEnd ORDER BY startAt ASC")
    List<ScheduleEvent> getEventsForDaySync(long dayStart, long dayEnd);

    @Query("SELECT * FROM schedule_event WHERE id = :id")
    ScheduleEvent getById(long id);

    @Query("SELECT * FROM schedule_event WHERE allDay = 0 AND reminderMinutesBefore >= 0 AND startAt > :now")
    List<ScheduleEvent> getFutureWithReminder(long now);
}
