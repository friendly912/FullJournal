package com.fulljournal.app.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedule_event")
public class ScheduleEvent {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title = "";

    @Nullable
    public String description;

    /** Epoch millis of the event start. */
    public long startAt;

    /** Epoch millis of the event end, or 0 if not set. */
    public long endAt;

    public boolean allDay;

    /** Minutes before startAt to fire a reminder, or -1 for no reminder. */
    public int reminderMinutesBefore = -1;
}
