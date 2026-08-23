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

    /** Chronologically ordered month key ("yyyy-MM") -> sum of values recorded that month. */
    public final Map<String, Double> monthlyTotals = new LinkedHashMap<>();

    public boolean hasAnomaly;
    public boolean anomalyHigh;
    public String latestMonthLabel;
    public double latestMonthValue;

    public ColumnStats(RecordColumn column) {
        this.column = column;
    }
}
