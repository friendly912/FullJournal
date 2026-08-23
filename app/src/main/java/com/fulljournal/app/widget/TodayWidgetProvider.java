package com.fulljournal.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.fulljournal.app.MainActivity;
import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.RecordRow;
import com.fulljournal.app.data.entity.RecordTable;
import com.fulljournal.app.data.entity.ScheduleEvent;
import com.fulljournal.app.util.DateUtil;

import java.util.List;

public class TodayWidgetProvider extends AppWidgetProvider {

    private static final int MAX_EVENTS_SHOWN = 3;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AppDatabase database = AppDatabase.getInstance(context);
        AppDatabase.databaseExecutor.execute(() -> {
            RemoteViews views = buildViews(context, database);
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }

    /** Call after any schedule/record change so widgets refresh without waiting for the periodic update. */
    public static void refreshAll(Context context) {
        Context appContext = context.getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
        int[] widgetIds = manager.getAppWidgetIds(new ComponentName(appContext, TodayWidgetProvider.class));
        if (widgetIds.length == 0) {
            return;
        }
        AppDatabase database = AppDatabase.getInstance(appContext);
        AppDatabase.databaseExecutor.execute(() -> {
            RemoteViews views = buildViews(appContext, database);
            for (int widgetId : widgetIds) {
                manager.updateAppWidget(widgetId, views);
            }
        });
    }

    private static RemoteViews buildViews(Context context, AppDatabase database) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);

        long dayStart = DateUtil.startOfDay(System.currentTimeMillis());
        long dayEnd = DateUtil.endOfDay(dayStart);
        List<ScheduleEvent> events = database.scheduleDao().getEventsForDaySync(dayStart, dayEnd);

        views.setTextViewText(R.id.widget_date, DateUtil.formatDate(dayStart));

        StringBuilder eventsText = new StringBuilder();
        int shown = 0;
        for (ScheduleEvent event : events) {
            if (shown >= MAX_EVENTS_SHOWN) {
                break;
            }
            if (shown > 0) {
                eventsText.append('\n');
            }
            String time = event.allDay ? context.getString(R.string.label_all_day) : DateUtil.formatTime(event.startAt);
            eventsText.append(time).append("  ").append(event.title);
            shown++;
        }
        if (shown == 0) {
            eventsText.append(context.getString(R.string.empty_schedule));
        }
        views.setTextViewText(R.id.widget_events, eventsText.toString());

        RecordRow latestRow = database.recordRowDao().getMostRecentSync();
        if (latestRow != null) {
            RecordTable table = database.recordTableDao().getById(latestRow.tableId);
            String tableName = table == null ? "" : table.name;
            views.setTextViewText(R.id.widget_latest_record,
                    "最新の記録: " + tableName + " (" + DateUtil.formatDate(latestRow.recordDate) + ")");
        } else {
            views.setTextViewText(R.id.widget_latest_record, "");
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_date, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_events, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_latest_record, pendingIntent);

        return views;
    }
}
