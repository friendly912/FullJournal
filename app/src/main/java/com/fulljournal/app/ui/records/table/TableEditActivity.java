package com.fulljournal.app.ui.records.table;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.ColumnType;
import com.fulljournal.app.data.entity.RecordColumn;
import com.fulljournal.app.data.entity.RecordTable;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.fulljournal.app.ui.records.DataEntryActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class TableEditActivity extends BaseToolbarActivity {

    private static final ColumnType[] TYPES = {
            ColumnType.NUMBER, ColumnType.DATE, ColumnType.TEXT, ColumnType.CHOICE
    };

    private AppDatabase database;
    private EditText inputTableName;
    private RecordColumnAdapter columnAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_edit);
        setupToolbar();
        setTitle(R.string.table_edit_title);
        database = AppDatabase.getInstance(this);

        inputTableName = findViewById(R.id.input_table_name);
        RecyclerView columnList = findViewById(R.id.column_list);
        Button buttonAddColumn = findViewById(R.id.button_add_column);
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonTemplate = findViewById(R.id.button_template);

        columnAdapter = new RecordColumnAdapter(position -> columnAdapter.removeAt(position));
        columnList.setLayoutManager(new LinearLayoutManager(this));
        columnList.setAdapter(columnAdapter);

        buttonAddColumn.setOnClickListener(v -> showAddColumnDialog());
        buttonTemplate.setOnClickListener(v -> showTemplateDialog());
        buttonSave.setOnClickListener(v -> save());
    }

    private void showTemplateDialog() {
        List<TableTemplate> templates = TableTemplate.defaults();
        String[] names = new String[templates.size()];
        for (int i = 0; i < templates.size(); i++) {
            names[i] = templates.get(i).name;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_use_template)
                .setItems(names, (dialog, which) -> applyTemplate(templates.get(which)))
                .show();
    }

    private void applyTemplate(TableTemplate template) {
        inputTableName.setText(template.name);
        int existingCount = columnAdapter.getColumns().size();
        for (int i = existingCount - 1; i >= 0; i--) {
            columnAdapter.removeAt(i);
        }
        for (RecordColumn column : template.buildColumns()) {
            columnAdapter.add(column);
        }
    }

    private void showAddColumnDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_column, null);
        EditText inputName = dialogView.findViewById(R.id.input_column_name);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_column_type);
        TextInputLayout layoutChoiceOptions = dialogView.findViewById(R.id.layout_choice_options);
        EditText inputChoiceOptions = dialogView.findViewById(R.id.input_choice_options);
        TextInputLayout layoutGoalValue = dialogView.findViewById(R.id.layout_goal_value);
        EditText inputGoalValue = dialogView.findViewById(R.id.input_goal_value);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutChoiceOptions.setVisibility(TYPES[position] == ColumnType.CHOICE ? View.VISIBLE : View.GONE);
                layoutGoalValue.setVisibility(TYPES[position] == ColumnType.NUMBER ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_column_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        return;
                    }
                    RecordColumn column = new RecordColumn();
                    column.name = name;
                    column.type = TYPES[spinnerType.getSelectedItemPosition()];
                    column.sortOrder = columnAdapter.getColumns().size();
                    if (column.type == ColumnType.CHOICE) {
                        column.choiceOptions = inputChoiceOptions.getText().toString().trim();
                    }
                    if (column.type == ColumnType.NUMBER) {
                        String goalText = inputGoalValue.getText().toString().trim();
                        if (!goalText.isEmpty()) {
                            try {
                                column.goalValue = Double.parseDouble(goalText);
                            } catch (NumberFormatException ignored) {
                                // leave unset
                            }
                        }
                    }
                    columnAdapter.add(column);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void save() {
        String name = inputTableName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            inputTableName.setError(getString(R.string.hint_table_name));
            return;
        }
        List<RecordColumn> columns = new ArrayList<>(columnAdapter.getColumns());

        RecordTable table = new RecordTable();
        table.name = name;
        table.createdAt = System.currentTimeMillis();

        AppDatabase.databaseExecutor.execute(() -> {
            long tableId = database.recordTableDao().insert(table);
            for (RecordColumn column : columns) {
                column.tableId = tableId;
            }
            if (!columns.isEmpty()) {
                database.recordColumnDao().insertAll(columns);
            }
            runOnUiThread(() -> {
                startActivity(new Intent(this, DataEntryActivity.class)
                        .putExtra(DataEntryActivity.EXTRA_TABLE_ID, tableId)
                        .putExtra(DataEntryActivity.EXTRA_TABLE_NAME, name));
                finish();
            });
        });
    }
}
