package com.fulljournal.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {

    public static long startOfDay(long epochMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(epochMillis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long endOfDay(long epochMillis) {
        return startOfDay(epochMillis) + 24L * 60 * 60 * 1000;
    }

    /** Monday-based ISO week bucket key, e.g. "2026-W08". */
    public static String weekKey(long epochMillis) {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        c.setTimeInMillis(epochMillis);
        int week = c.get(Calendar.WEEK_OF_YEAR);
        int year = c.get(Calendar.YEAR);
        return String.format(Locale.getDefault(), "%d-W%02d", year, week);
    }

    /** Month bucket key, e.g. "2026-08". */
    public static String monthKey(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(epochMillis);
    }

    public static String formatDate(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return sdf.format(epochMillis);
    }

    public static String formatDateTime(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        return sdf.format(epochMillis);
    }

    public static String formatTime(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(epochMillis);
    }
}
