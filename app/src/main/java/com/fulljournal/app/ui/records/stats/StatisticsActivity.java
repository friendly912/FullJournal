package com.fulljournal.app.ui.records.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.RecordColumn;
import com.fulljournal.app.data.entity.RecordRow;
import com.fulljournal.app.data.entity.RecordValue;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends BaseToolbarActivity {

    public static final String EXTRA_TABLE_ID = "extra_table_id";
    public static final String EXTRA_TABLE_NAME = "extra_table_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setupToolbar();

        long tableId = getIntent().getLongExtra(EXTRA_TABLE_ID, -1);
        setTitle(getIntent().getStringExtra(EXTRA_TABLE_NAME));

        android.widget.LinearLayout container = findViewById(R.id.stats_container);
        TextView noDataView = findViewById(R.id.text_no_data);
        AppDatabase database = AppDatabase.getInstance(this);

        AppDatabase.databaseExecutor.execute(() -> {
            List<RecordColumn> columns = database.recordColumnDao().getForTableSync(tableId);
            List<RecordRow> rows = database.recordRowDao().getForTableSync(tableId);
            List<Long> rowIds = new ArrayList<>();
            for (RecordRow row : rows) {
                rowIds.add(row.id);
            }
            List<RecordValue> values = rowIds.isEmpty()
                    ? new ArrayList<>() : database.recordValueDao().getForRows(rowIds);

            List<ColumnStats> columnStats = StatsCalculator.computeColumnStats(columns, rows, values);
            List<StatsCalculator.CrossTab> crossTabs = StatsCalculator.computeCrossTabs(columns, rows, values);

            runOnUiThread(() -> render(container, noDataView, columnStats, crossTabs));
        });
    }

    private void render(android.widget.LinearLayout container, TextView noDataView,
                         List<ColumnStats> columnStats, List<StatsCalculator.CrossTab> crossTabs) {
        if (columnStats.isEmpty()) {
            noDataView.setVisibility(View.VISIBLE);
            return;
        }
        noDataView.setVisibility(View.GONE);

        View summarySection = buildSummarySection(container, columnStats);
        if (summarySection != null) {
            container.addView(summarySection);
        }

        for (ColumnStats stats : columnStats) {
            container.addView(buildColumnSection(container, stats));
        }
        for (StatsCalculator.CrossTab crossTab : crossTabs) {
            container.addView(buildCrossTabSection(container, crossTab));
        }
    }

    /** Rule-based natural-language recap: month-over-month change plus goal progress, per numeric column. */
    @Nullable
    private View buildSummarySection(android.widget.LinearLayout parent, List<ColumnStats> columnStats) {
        List<String> lines = new ArrayList<>();
        for (ColumnStats stats : columnStats) {
            if (stats.hasMonthOverMonthChange) {
                double percent = stats.monthOverMonthChangePercent;
                if (Math.abs(percent) < 1) {
                    lines.add(getString(R.string.stats_summary_flat, stats.column.name));
                } else if (percent > 0) {
                    lines.add(getString(R.string.stats_summary_increase, stats.column.name, Math.round(percent)));
                } else {
                    lines.add(getString(R.string.stats_summary_decrease, stats.column.name, Math.round(-percent)));
                }
            }
            if (stats.column.goalValue != null && stats.column.goalValue != 0) {
                int percent = (int) Math.round(stats.latestValue / stats.column.goalValue * 100);
                lines.add(getString(R.string.stats_summary_goal, stats.column.name, percent));
            }
        }
        if (lines.isEmpty()) {
            return null;
        }

        View section = LayoutInflater.from(this).inflate(R.layout.item_stats_summary, parent, false);
        TextView body = section.findViewById(R.id.text_summary_body);
        body.setText(String.join("\n", lines));
        return section;
    }

    private View buildColumnSection(android.widget.LinearLayout parent, ColumnStats stats) {
        View section = LayoutInflater.from(this).inflate(R.layout.item_stat_column, parent, false);

        TextView title = section.findViewById(R.id.text_column_title);
        TextView summary = section.findViewById(R.id.text_summary_stats);
        TextView anomaly = section.findViewById(R.id.text_anomaly);
        TextView prediction = section.findViewById(R.id.text_prediction);
        View goalLayout = section.findViewById(R.id.layout_goal_progress);
        TextView goalText = section.findViewById(R.id.text_goal_progress);
        android.widget.ProgressBar goalProgress = section.findViewById(R.id.progress_goal);
        LineChart chart = section.findViewById(R.id.line_chart);

        title.setText(stats.column.name);
        summary.setText(String.format(Locale.getDefault(),
                "%s   %s   %s   %s\n%s   %s",
                getString(R.string.stats_sum, format(stats.sum)),
                getString(R.string.stats_avg, format(stats.avg)),
                getString(R.string.stats_min, format(stats.min)),
                getString(R.string.stats_max, format(stats.max)),
                "今週合計: " + format(stats.thisWeekSum),
                "今月合計: " + format(stats.thisMonthSum)));

        if (stats.hasAnomaly) {
            anomaly.setVisibility(View.VISIBLE);
            int messageRes = stats.anomalyHigh ? R.string.stats_anomaly_high : R.string.stats_anomaly_low;
            anomaly.setText(getString(messageRes, stats.latestMonthLabel, format(stats.latestMonthValue)));
        } else {
            anomaly.setVisibility(View.GONE);
        }

        if (stats.hasPrediction) {
            prediction.setVisibility(View.VISIBLE);
            prediction.setText(getString(R.string.stats_prediction, format(stats.predictedNextMonthValue)));
        } else {
            prediction.setVisibility(View.GONE);
        }

        if (stats.column.goalValue != null && stats.column.goalValue != 0) {
            goalLayout.setVisibility(View.VISIBLE);
            double goal = stats.column.goalValue;
            int percent = (int) Math.round(stats.latestValue / goal * 100);
            goalText.setText(getString(R.string.stats_goal_progress,
                    format(stats.latestValue), format(goal), percent));
            goalProgress.setProgress(Math.max(0, Math.min(100, percent)));
        } else {
            goalLayout.setVisibility(View.GONE);
        }

        bindChart(chart, stats);
        return section;
    }

    private void bindChart(LineChart chart, ColumnStats stats) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (java.util.Map.Entry<String, Double> entry : stats.monthlyTotals.entrySet()) {
            entries.add(new Entry(index, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            index++;
        }

        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        LineDataSet dataSet = new LineDataSet(entries, stats.column.name);
        dataSet.setColor(primaryColor);
        dataSet.setCircleColor(primaryColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45f);

        chart.invalidate();
    }

    private View buildCrossTabSection(android.widget.LinearLayout parent, StatsCalculator.CrossTab crossTab) {
        View section = LayoutInflater.from(this).inflate(R.layout.item_cross_tab, parent, false);
        TextView title = section.findViewById(R.id.text_cross_tab_title);
        TableLayout table = section.findViewById(R.id.table_cross_tab);
        title.setText(crossTab.title + "(月次)");

        TableRow header = new TableRow(this);
        header.addView(cell("", true));
        for (String category : crossTab.categories) {
            header.addView(cell(category, true));
        }
        table.addView(header);

        for (String month : crossTab.months) {
            TableRow row = new TableRow(this);
            row.addView(cell(month, true));
            for (String category : crossTab.categories) {
                Double value = crossTab.totalsByMonthAndCategory.get(crossTab.key(month, category));
                row.addView(cell(value == null ? "-" : format(value), false));
            }
            table.addView(row);
        }
        return section;
    }

    private TextView cell(String text, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String format(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.getDefault(), "%.0f", value);
        }
        return String.format(Locale.getDefault(), "%.2f", value);
    }
}
