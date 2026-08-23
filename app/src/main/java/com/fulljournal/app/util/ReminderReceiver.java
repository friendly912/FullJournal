package com.fulljournal.app.util;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fulljournal.app.FullJournalApp;
import com.fulljournal.app.MainActivity;
import com.fulljournal.app.R;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_TITLE = "extra_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        long eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
        String title = intent.getStringExtra(EXTRA_TITLE);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        android.app.PendingIntent contentIntent = android.app.PendingIntent.getActivity(
                context, (int) eventId, openIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FullJournalApp.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_schedule)
                .setContentTitle(context.getString(R.string.nav_schedule))
                .setContentText(title)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context).notify((int) eventId, builder.build());
    }
}
