package com.fulljournal.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.ScheduleEvent;

/** Re-schedules pending reminders after a reboot, since AlarmManager alarms don't survive one. */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        Context appContext = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(appContext);
        AppDatabase.databaseExecutor.execute(() -> {
            for (ScheduleEvent event : database.scheduleDao().getFutureWithReminder(System.currentTimeMillis())) {
                ReminderScheduler.schedule(appContext, event);
            }
        });
    }
}
