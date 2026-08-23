package com.fulljournal.app.ui.records.stats;

import com.fulljournal.app.data.entity.ColumnType;
import com.fulljournal.app.data.entity.RecordColumn;
import com.fulljournal.app.data.entity.RecordRow;
import com.fulljournal.app.data.entity.RecordValue;
import com.fulljournal.app.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure computation of per-column statistics and category cross-tabs. No Android framework dependencies. */
public class StatsCalculator {

    private static final double ANOMALY_Z_THRESHOLD = 1.5;

    public static class CrossTab {
        public String title;
        public List<String> months = new ArrayList<>();
        public List<String> categories = new ArrayList<>();
        public Map<String, Double> totalsByMonthAndCategory = new HashMap<>();

        public String key(String month, String category) {
            return month + "|" + category;
        }
    }

    public static List<ColumnStats> computeColumnStats(
            List<RecordColumn> columns, List<RecordRow> rows, List<RecordValue> values) {
        Map<Long, List<RecordValue>> valuesByRow = groupByRow(values);

        List<ColumnStats> result = new ArrayList<>();
        for (RecordColumn column : columns) {
            if (column.type != ColumnType.NUMBER) {
                continue;
            }
            ColumnStats stats = new ColumnStats(column);
            List<Double> allValues = new ArrayList<>();
            Map<String, Double> monthlySum = new LinkedHashMap<>();

            for (RecordRow row : rows) {
                Double value = parseNumber(findValue(valuesByRow, row.id, column.id));
                if (value == null) {
                    continue;
                }
                allValues.add(value);
                stats.latestValue = value;

                String monthKey = DateUtil.monthKey(row.recordDate);
                Double existing = monthlySum.get(monthKey);
                monthlySum.put(monthKey, (existing == null ? 0 : existing) + value);

                if (DateUtil.weekKey(row.recordDate).equals(DateUtil.weekKey(System.currentTimeMillis()))) {
                    stats.thisWeekSum += value;
                }
                if (monthKey.equals(DateUtil.monthKey(System.currentTimeMillis()))) {
                    stats.thisMonthSum += value;
                }
            }

            if (allValues.isEmpty()) {
                continue;
            }

            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (double v : allValues) {
                sum += v;
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
            stats.sum = sum;
            stats.avg = sum / allValues.size();
            stats.min = min;
            stats.max = max;
            stats.monthlyTotals.putAll(monthlySum);

            applyAnomalyDetection(stats);
            result.add(stats);
        }
        return result;
    }

    private static void applyAnomalyDetection(ColumnStats stats) {
        List<String> monthKeys = new ArrayList<>(stats.monthlyTotals.keySet());
        if (monthKeys.size() < 3) {
            return;
        }
        List<Double> monthValues = new ArrayList<>(stats.monthlyTotals.values());

        double mean = 0;
        for (double v : monthValues) {
            mean += v;
        }
        mean /= monthValues.size();

        double variance = 0;
        for (double v : monthValues) {
            variance += (v - mean) * (v - mean);
        }
        variance /= monthValues.size();
        double stdDev = Math.sqrt(variance);

        String latestKey = monthKeys.get(monthKeys.size() - 1);
        double latestValue = monthValues.get(monthValues.size() - 1);
        stats.latestMonthLabel = latestKey;
        stats.latestMonthValue = latestValue;

        if (stdDev <= 0) {
            return;
        }
        double z = (latestValue - mean) / stdDev;
        if (z >= ANOMALY_Z_THRESHOLD) {
            stats.hasAnomaly = true;
            stats.anomalyHigh = true;
        } else if (z <= -ANOMALY_Z_THRESHOLD) {
            stats.hasAnomaly = true;
            stats.anomalyHigh = false;
        }
    }

    /** Cross-tabs the first NUMBER column against every CHOICE column, month x category. */
    public static List<CrossTab> computeCrossTabs(
            List<RecordColumn> columns, List<RecordRow> rows, List<RecordValue> values) {
        RecordColumn numberColumn = null;
        for (RecordColumn column : columns) {
            if (column.type == ColumnType.NUMBER) {
                numberColumn = column;
                break;
            }
        }
        List<CrossTab> crossTabs = new ArrayList<>();
        if (numberColumn == null) {
            return crossTabs;
        }
        Map<Long, List<RecordValue>> valuesByRow = groupByRow(values);

        for (RecordColumn column : columns) {
            if (column.type != ColumnType.CHOICE) {
                continue;
            }
            CrossTab crossTab = new CrossTab();
            crossTab.title = numberColumn.name + " × " + column.name;

            List<String> categories = new ArrayList<>();
            if (column.choiceOptions != null) {
                for (String option : column.choiceOptions.split(",")) {
                    String trimmed = option.trim();
                    if (!trimmed.isEmpty()) {
                        categories.add(trimmed);
                    }
                }
            }
            crossTab.categories = categories;

            List<String> months = new ArrayList<>();
            for (RecordRow row : rows) {
                Double numberValue = parseNumber(findValue(valuesByRow, row.id, numberColumn.id));
                String category = findValue(valuesByRow, row.id, column.id);
                if (numberValue == null || category == null || category.isEmpty()) {
                    continue;
                }
                String month = DateUtil.monthKey(row.recordDate);
                if (!months.contains(month)) {
                    months.add(month);
                }
                String key = crossTab.key(month, category);
                Double existing = crossTab.totalsByMonthAndCategory.get(key);
                crossTab.totalsByMonthAndCategory.put(key, (existing == null ? 0 : existing) + numberValue);
            }
            crossTab.months = months;

            if (!crossTab.totalsByMonthAndCategory.isEmpty()) {
                crossTabs.add(crossTab);
            }
        }
        return crossTabs;
    }

    private static Map<Long, List<RecordValue>> groupByRow(List<RecordValue> values) {
        Map<Long, List<RecordValue>> map = new HashMap<>();
        for (RecordValue value : values) {
            List<RecordValue> bucket = map.get(value.rowId);
            if (bucket == null) {
                bucket = new ArrayList<>();
                map.put(value.rowId, bucket);
            }
            bucket.add(value);
        }
        return map;
    }

    private static String findValue(Map<Long, List<RecordValue>> valuesByRow, long rowId, long columnId) {
        List<RecordValue> bucket = valuesByRow.get(rowId);
        if (bucket == null) {
            return null;
        }
        for (RecordValue value : bucket) {
            if (value.columnId == columnId) {
                return value.value;
            }
        }
        return null;
    }

    private static Double parseNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
