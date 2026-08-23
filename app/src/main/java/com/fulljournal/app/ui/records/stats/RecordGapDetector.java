package com.fulljournal.app.ui.records.stats;

import com.fulljournal.app.data.entity.RecordRow;
import com.fulljournal.app.util.DateUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Flags a table as "overdue" when it hasn't been recorded in noticeably longer than its usual cadence. */
public class RecordGapDetector {

    private static final long MIN_OVERDUE_DAYS = 3;

    public static class GapInfo {
        public boolean overdue;
        public long daysSinceLastRecord;
    }

    /** {@code rows} must be sorted ascending by recordDate, as RecordRowDao.getForTableSync returns them. */
    public static GapInfo evaluate(List<RecordRow> rows) {
        GapInfo info = new GapInfo();
        if (rows.size() < 2) {
            return info;
        }

        long first = rows.get(0).recordDate;
        long last = rows.get(rows.size() - 1).recordDate;
        long spanDays = TimeUnit.MILLISECONDS.toDays(last - first);
        double avgIntervalDays = Math.max(1.0, spanDays / (double) (rows.size() - 1));

        long today = DateUtil.startOfDay(System.currentTimeMillis());
        long daysSinceLast = TimeUnit.MILLISECONDS.toDays(today - last);

        info.daysSinceLastRecord = daysSinceLast;
        info.overdue = daysSinceLast >= MIN_OVERDUE_DAYS && daysSinceLast > avgIntervalDays * 2;
        return info;
    }
}
