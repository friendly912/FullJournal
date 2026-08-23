package com.fulljournal.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.fulljournal.app.data.dao.RecordColumnDao;
import com.fulljournal.app.data.dao.RecordRowDao;
import com.fulljournal.app.data.dao.RecordTableDao;
import com.fulljournal.app.data.dao.RecordValueDao;
import com.fulljournal.app.data.dao.ScheduleDao;
import com.fulljournal.app.data.entity.RecordColumn;
import com.fulljournal.app.data.entity.RecordRow;
import com.fulljournal.app.data.entity.RecordTable;
import com.fulljournal.app.data.entity.RecordValue;
import com.fulljournal.app.data.entity.ScheduleEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                ScheduleEvent.class,
                RecordTable.class,
                RecordColumn.class,
                RecordRow.class,
                RecordValue.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(2);

    public abstract ScheduleDao scheduleDao();

    public abstract RecordTableDao recordTableDao();

    public abstract RecordColumnDao recordColumnDao();

    public abstract RecordRowDao recordRowDao();

    public abstract RecordValueDao recordValueDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fulljournal.db")
                            .build();
                }
            }
        }
        return instance;
    }
}
