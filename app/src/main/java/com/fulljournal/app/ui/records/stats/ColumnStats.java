package com.fulljournal.app.ui.records.stats;

import com.fulljournal.app.data.entity.RecordColumn;

import java.util.LinkedHashMap;
import java.util.Map;

/** Aggregated statistics for a single NUMBER column of a record table. */
public class ColumnStats {
    public final RecordColumn column;
    public double sum;
    public double avg;
    public double min;
    public double max;
    public double thisWeekSum;
    public double thisMonthSum;

    /** The most recently recorded raw value (by recordDate), used against column.goalValue for progress. */
    public double latestValue;

    /** Chronologically ordered month key ("yyyy-MM") -> sum of values recorded that month. */
    public final Map<String, Double> monthlyTotals = new LinkedHashMap<>();

    public boolean hasAnomaly;
    public boolean anomalyHigh;
    public String latestMonthLabel;
    public double latestMonthValue;

    /** Simple linear-regression forecast of next month's total, when at least 3 months of history exist. */
    public boolean hasPrediction;
    public double predictedNextMonthValue;

    /** Percent change of this month's total vs. the previous month's, when both exist. */
    public boolean hasMonthOverMonthChange;
    public double monthOverMonthChangePercent;

    public ColumnStats(RecordColumn column) {
        this.column = column;
    }
}
