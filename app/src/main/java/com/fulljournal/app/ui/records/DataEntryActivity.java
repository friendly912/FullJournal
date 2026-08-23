package com.fulljournal.app.ui.records;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.ColumnType;
import com.fulljournal.app.data.entity.RecordColumn;
import com.fulljournal.app.data.entity.RecordRow;
import com.fulljournal.app.data.entity.RecordValue;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.fulljournal.app.ui.records.stats.StatisticsActivity;
import com.fulljournal.app.util.DateUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataEntryActivity extends BaseToolbarActivity {

    /** Avoids java.util.function.Supplier, which needs desugaring below API 24. */
    private interface FieldValueProvider {
        String getValue();
    }

    public static final String EXTRA_TABLE_ID = "extra_table_id";
    public static final String EXTRA_TABLE_NAME = "extra_table_name";

    private AppDatabase database;
    private long tableId;
    private List<RecordColumn> columns = new ArrayList<>();
    private List<RecordRow> latestRows = new ArrayList<>();
    private TableLayout tableLayout;
    private TextView emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);
        setupToolbar();
        database = AppDatabase.getInstance(this);

        tableId = getIntent().getLongExtra(EXTRA_TABLE_ID, -1);
        setTitle(getIntent().getStringExtra(EXTRA_TABLE_NAME));

        tableLayout = findViewById(R.id.table_rows);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fab = findViewById(R.id.fab_add_row);

        database.recordColumnDao().observeForTable(tableId).observe(this, cols -> {
            columns = cols;
            refreshTable();
        });

        database.recordRowDao().observeForTable(tableId).observe(this, rows -> {
            this.latestRows = rows;
            refreshTable();
        });

        fab.setOnClickListener(v -> showRowDialog(null, null));
    }

    private void refreshTable() {
        List<RecordRow> rows = latestRows;
        AppDatabase.databaseExecutor.execute(() -> {
            Map<Long, List<RecordValue>> valuesByRow = fetchValuesByRow(rows);
            runOnUiThread(() -> renderTable(rows, valuesByRow));
        });
    }

    private Map<Long, List<RecordValue>> fetchValuesByRow(List<RecordRow> rows) {
        List<Long> rowIds = new ArrayList<>();
        for (RecordRow row : rows) {
            rowIds.add(row.id);
        }
        Map<Long, List<RecordValue>> valuesByRow = new HashMap<>();
        if (!rowIds.isEmpty()) {
            for (RecordValue value : database.recordValueDao().getForRows(rowIds)) {
                List<RecordValue> bucket = valuesByRow.get(value.rowId);
                if (bucket == null) {
                    bucket = new ArrayList<>();
                    valuesByRow.put(value.rowId, bucket);
                }
                bucket.add(value);
            }
        }
        return valuesByRow;
    }

    private void renderTable(List<RecordRow> rows, Map<Long, List<RecordValue>> valuesByRow) {
        tableLayout.removeAllViews();

        boolean empty = rows.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        tableLayout.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        TableRow header = new TableRow(this);
        header.addView(cell(getString(R.string.label_record_date), true));
        for (RecordColumn column : columns) {
            header.addView(cell(column.name, true));
        }
        tableLayout.addView(header);

        for (RecordRow row : rows) {
            List<RecordValue> values = valuesByRow.get(row.id);
            Map<Long, String> valueByColumnId = new HashMap<>();
            if (values != null) {
                for (RecordValue value : values) {
                    valueByColumnId.put(value.columnId, value.value);
                }
            }

            TableRow tableRow = new TableRow(this);
            tableRow.addView(cell(DateUtil.formatDate(row.recordDate), false));
            for (RecordColumn column : columns) {
                String value = valueByColumnId.get(column.id);
                tableRow.addView(cell(value == null || value.isEmpty() ? "-" : value, false));
            }
            tableRow.setClickable(true);
            tableRow.setFocusable(true);
            tableRow.setOnClickListener(v -> showRowDialog(row, valueByColumnId));
            tableRow.setOnLongClickListener(v -> {
                confirmDeleteRow(row);
                return true;
            });
            tableLayout.addView(tableRow);
        }
    }

    private TextView cell(String text, boolean header) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setBackgroundResource(header ? R.drawable.bg_table_header_cell : R.drawable.bg_table_cell);
        int paddingH = dp(16);
        int paddingV = dp(10);
        view.setPadding(paddingH, paddingV, paddingH, paddingV);
        view.setMinWidth(dp(96));
        if (header) {
            view.setTypeface(view.getTypeface(), Typeface.BOLD);
        }
        return view;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void confirmDeleteRow(RecordRow row) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.action_delete)
                .setPositiveButton(R.string.action_delete, (dialog, which) ->
                        AppDatabase.databaseExecutor.execute(() -> {
                            database.recordRowDao().delete(row);
                            com.fulljournal.app.widget.TodayWidgetProvider.refreshAll(getApplicationContext());
                        }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows the add/edit dialog. Pass {@code existingRow} and its current values to edit in place,
     * or {@code null} for both to create a new row.
     */
    private void showRowDialog(@Nullable RecordRow existingRow, @Nullable Map<Long, String> existingValues) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_row, null);
        Button buttonRecordDate = dialogView.findViewById(R.id.button_record_date);
        ViewGroup fieldsContainer = dialogView.findViewById(R.id.fields_container);

        Calendar recordDateCalendar = Calendar.getInstance();
        if (existingRow != null) {
            recordDateCalendar.setTimeInMillis(existingRow.recordDate);
        }
        buttonRecordDate.setText(DateUtil.formatDate(recordDateCalendar.getTimeInMillis()));
        buttonRecordDate.setOnClickListener(v -> new DatePickerDialog(this, (picker, year, month, day) -> {
            recordDateCalendar.set(year, month, day);
            buttonRecordDate.setText(DateUtil.formatDate(recordDateCalendar.getTimeInMillis()));
        }, recordDateCalendar.get(Calendar.YEAR), recordDateCalendar.get(Calendar.MONTH),
                recordDateCalendar.get(Calendar.DAY_OF_MONTH)).show());

        Map<RecordColumn, FieldValueProvider> fieldSuppliers = new HashMap<>();
        for (RecordColumn column : columns) {
            String existingValue = existingValues == null ? null : existingValues.get(column.id);
            fieldSuppliers.put(column, addField(fieldsContainer, column, recordDateCalendar, existingValue));
        }

        new AlertDialog.Builder(this)
                .setTitle(existingRow == null ? R.string.dialog_add_row_title : R.string.action_edit)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    List<RecordValue> values = new ArrayList<>();
                    for (Map.Entry<RecordColumn, FieldValueProvider> entry : fieldSuppliers.entrySet()) {
                        RecordValue value = new RecordValue();
                        value.columnId = entry.getKey().id;
                        String text = entry.getValue().getValue();
                        value.value = text == null ? "" : text;
                        values.add(value);
                    }

                    RecordRow row = existingRow != null ? existingRow : new RecordRow();
                    row.tableId = tableId;
                    row.recordDate = DateUtil.startOfDay(recordDateCalendar.getTimeInMillis());
                    if (existingRow == null) {
                        row.createdAt = System.currentTimeMillis();
                    }

                    AppDatabase.databaseExecutor.execute(() -> {
                        long rowId;
                        if (existingRow != null) {
                            database.recordRowDao().update(row);
                            database.recordValueDao().deleteForRow(row.id);
                            rowId = row.id;
                        } else {
                            rowId = database.recordRowDao().insert(row);
                        }
                        for (RecordValue value : values) {
                            value.rowId = rowId;
                        }
                        if (!values.isEmpty()) {
                            database.recordValueDao().insertAll(values);
                        }
                        com.fulljournal.app.widget.TodayWidgetProvider.refreshAll(getApplicationContext());
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Adds an input view for the given column into the container and returns a supplier of its text value. */
    private FieldValueProvider addField(ViewGroup container, RecordColumn column,
                                         Calendar recordDateCalendar, @Nullable String existingValue) {
        switch (column.type) {
            case CHOICE: {
                TextView label = new TextView(this);
                label.setText(column.name);
                container.addView(label);

                List<String> options = new ArrayList<>();
                if (column.choiceOptions != null) {
                    for (String option : column.choiceOptions.split(",")) {
                        String trimmed = option.trim();
                        if (!trimmed.isEmpty()) {
                            options.add(trimmed);
                        }
                    }
                }
                Spinner spinner = new Spinner(this);
                spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options));
                if (existingValue != null) {
                    int index = options.indexOf(existingValue);
                    if (index >= 0) {
                        spinner.setSelection(index);
                    }
                }
                container.addView(spinner);
                addSpacer(container);
                return () -> options.isEmpty() ? "" : options.get(spinner.getSelectedItemPosition());
            }
            case DATE: {
                TextView label = new TextView(this);
                label.setText(column.name);
                container.addView(label);

                Calendar fieldCalendar = (Calendar) recordDateCalendar.clone();
                if (existingValue != null && !existingValue.isEmpty()) {
                    try {
                        fieldCalendar.setTimeInMillis(Long.parseLong(existingValue));
                    } catch (NumberFormatException ignored) {
                        // keep default
                    }
                }
                Button dateButton = new Button(this);
                dateButton.setText(DateUtil.formatDate(fieldCalendar.getTimeInMillis()));
                dateButton.setOnClickListener(v -> new DatePickerDialog(this, (picker, year, month, day) -> {
                    fieldCalendar.set(year, month, day);
                    dateButton.setText(DateUtil.formatDate(fieldCalendar.getTimeInMillis()));
                }, fieldCalendar.get(Calendar.YEAR), fieldCalendar.get(Calendar.MONTH),
                        fieldCalendar.get(Calendar.DAY_OF_MONTH)).show());
                container.addView(dateButton);
                addSpacer(container);
                return () -> String.valueOf(fieldCalendar.getTimeInMillis());
            }
            case NUMBER:
            case TEXT:
            default: {
                TextInputLayout inputLayout = new TextInputLayout(this);
                inputLayout.setHint(column.name);
                TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
                if (column.type == ColumnType.NUMBER) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                }
                if (existingValue != null) {
                    editText.setText(existingValue);
                }
                inputLayout.addView(editText);
                container.addView(inputLayout);
                addSpacer(container);
                return () -> editText.getText() == null ? "" : editText.getText().toString().trim();
            }
        }
    }

    private void addSpacer(ViewGroup container) {
        View spacer = new View(this);
        int height = (int) (8 * getResources().getDisplayMetrics().density);
        spacer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        container.addView(spacer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_statistics) {
            startActivity(new Intent(this, StatisticsActivity.class)
                    .putExtra(StatisticsActivity.EXTRA_TABLE_ID, tableId)
                    .putExtra(StatisticsActivity.EXTRA_TABLE_NAME, getIntent().getStringExtra(EXTRA_TABLE_NAME)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
