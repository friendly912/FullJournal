package com.fulljournal.app.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.RecordSearchHit;
import com.fulljournal.app.data.entity.RecordTable;
import com.fulljournal.app.data.entity.ScheduleEvent;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.fulljournal.app.ui.records.DataEntryActivity;
import com.fulljournal.app.ui.schedule.ScheduleEditActivity;
import com.fulljournal.app.util.DateUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends BaseToolbarActivity {

    private AppDatabase database;
    private SearchResultAdapter adapter;
    private TextView emptyView;
    private RecyclerView resultList;

    /** Cancels a stale search if the query changes again before it finishes. */
    private int searchToken = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupToolbar();
        setTitle(R.string.action_search);
        database = AppDatabase.getInstance(this);

        TextInputEditText inputQuery = findViewById(R.id.input_query);
        resultList = findViewById(R.id.result_list);
        emptyView = findViewById(R.id.empty_view);

        adapter = new SearchResultAdapter(this::onResultClick);
        resultList.setLayoutManager(new LinearLayoutManager(this));
        resultList.setAdapter(adapter);

        inputQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                runSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void runSearch(String query) {
        int token = ++searchToken;
        if (query.isEmpty()) {
            showResults(new ArrayList<>(), token);
            return;
        }
        AppDatabase.databaseExecutor.execute(() -> {
            List<SearchResultItem> results = new ArrayList<>();

            for (ScheduleEvent event : database.scheduleDao().search(query)) {
                String subtitle = event.allDay
                        ? getString(R.string.label_all_day)
                        : DateUtil.formatDateTime(event.startAt);
                results.add(SearchResultItem.forSchedule(event.id, event.title,
                        getString(R.string.search_result_schedule_prefix) + " ・ " + subtitle));
            }

            for (RecordTable table : database.recordTableDao().search(query)) {
                results.add(SearchResultItem.forTable(table.id, table.name,
                        getString(R.string.search_result_table_prefix)));
            }

            Map<Long, RecordSearchHit> hitsByRow = new LinkedHashMap<>();
            for (RecordSearchHit hit : database.recordValueDao().search(query)) {
                if (!hitsByRow.containsKey(hit.rowId)) {
                    hitsByRow.put(hit.rowId, hit);
                }
            }
            for (RecordSearchHit hit : hitsByRow.values()) {
                results.add(SearchResultItem.forRow(hit.tableId, hit.tableName, hit.tableName,
                        DateUtil.formatDate(hit.recordDate) + " ・ " + hit.matchedValue));
            }

            runOnUiThread(() -> showResults(results, token));
        });
    }

    private void showResults(List<SearchResultItem> results, int token) {
        if (token != searchToken) {
            return;
        }
        adapter.submitList(results);
        boolean empty = results.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        resultList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void onResultClick(SearchResultItem item) {
        switch (item.type) {
            case SCHEDULE:
                startActivity(new Intent(this, ScheduleEditActivity.class)
                        .putExtra(ScheduleEditActivity.EXTRA_EVENT_ID, item.targetId));
                break;
            case RECORD_TABLE:
            case RECORD_ROW:
                startActivity(new Intent(this, DataEntryActivity.class)
                        .putExtra(DataEntryActivity.EXTRA_TABLE_ID, item.targetId)
                        .putExtra(DataEntryActivity.EXTRA_TABLE_NAME, item.tableName));
                break;
        }
    }
}
