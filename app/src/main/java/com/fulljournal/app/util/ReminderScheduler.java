package com.fulljournal.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fulljournal.app.data.entity.ScheduleEvent;

public class ReminderScheduler {

    public static void schedule(Context context, ScheduleEvent event) {
        if (event.reminderMinutesBefore < 0) {
            return;
        }
        long triggerAt = event.startAt - event.reminderMinutesBefore * 60_000L;
        if (triggerAt <= System.currentTimeMillis()) {
            return;
        }

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildPendingIntent(context, event);
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }

    public static void cancel(Context context, ScheduleEvent event) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        alarmManager.cancel(buildPendingIntent(context, event));
    }

    private static PendingIntent buildPendingIntent(Context context, ScheduleEvent event) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id);
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, event.title);
        return PendingIntent.getBroadcast(
                context, (int) event.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
