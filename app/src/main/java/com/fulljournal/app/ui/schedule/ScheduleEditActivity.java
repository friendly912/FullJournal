package com.fulljournal.app.ui.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.ScheduleEvent;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.fulljournal.app.util.DateUtil;
import com.fulljournal.app.util.ReminderScheduler;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleEditActivity extends BaseToolbarActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_DEFAULT_DATE = "extra_default_date";

    private static final int[] REMINDER_MINUTES = {-1, 5, 15, 30, 60, 1440};

    private static final int REPEAT_NONE = 0;
    private static final int REPEAT_DAILY = 1;
    private static final int REPEAT_WEEKLY = 2;
    private static final int REPEAT_MONTHLY = 3;

    /** How many occurrences to materialize for each repeat frequency (roughly one year of coverage). */
    private static final int REPEAT_DAILY_COUNT = 90;
    private static final int REPEAT_WEEKLY_COUNT = 26;
    private static final int REPEAT_MONTHLY_COUNT = 12;

    private AppDatabase database;
    private ScheduleEvent event;
    private boolean isNewEvent;
    private final Calendar calendar = Calendar.getInstance();

    private EditText inputTitle;
    private EditText inputDescription;
    private SwitchMaterial switchAllDay;
    private Button buttonPickDate;
    private Button buttonPickTime;
    private Spinner spinnerReminder;
    private View labelRepeat;
    private Spinner spinnerRepeat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);
        setupToolbar();
        setTitle(R.string.schedule_edit_title);
        database = AppDatabase.getInstance(this);

        inputTitle = findViewById(R.id.input_title);
        inputDescription = findViewById(R.id.input_description);
        switchAllDay = findViewById(R.id.switch_all_day);
        buttonPickDate = findViewById(R.id.button_pick_date);
        buttonPickTime = findViewById(R.id.button_pick_time);
        spinnerReminder = findViewById(R.id.spinner_reminder);
        labelRepeat = findViewById(R.id.label_repeat);
        spinnerRepeat = findViewById(R.id.spinner_repeat);
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonDelete = findViewById(R.id.button_delete);

        ArrayAdapter<CharSequence> reminderAdapter = ArrayAdapter.createFromResource(
                this, R.array.reminder_options, android.R.layout.simple_spinner_item);
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(reminderAdapter);

        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                this, R.array.repeat_options, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(repeatAdapter);

        switchAllDay.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                buttonPickTime.setVisibility(isChecked ? View.GONE : View.VISIBLE));

        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonPickTime.setOnClickListener(v -> showTimePicker());

        long eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        isNewEvent = eventId < 0;
        if (!isNewEvent) {
            labelRepeat.setVisibility(View.GONE);
            spinnerRepeat.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(v -> delete());
            AppDatabase.databaseExecutor.execute(() -> {
                event = database.scheduleDao().getById(eventId);
                runOnUiThread(this::bindEvent);
            });
        } else {
            event = new ScheduleEvent();
            long defaultDate = getIntent().getLongExtra(EXTRA_DEFAULT_DATE, System.currentTimeMillis());
            calendar.setTimeInMillis(defaultDate);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            updateDateTimeButtons();

            int defaultReminder = com.fulljournal.app.util.AppPrefs.getDefaultReminderMinutes(this);
            for (int i = 0; i < REMINDER_MINUTES.length; i++) {
                if (REMINDER_MINUTES[i] == defaultReminder) {
                    spinnerReminder.setSelection(i);
                    break;
                }
            }
        }

        buttonSave.setOnClickListener(v -> save());
    }

    private void bindEvent() {
        inputTitle.setText(event.title);
        inputDescription.setText(event.description);
        switchAllDay.setChecked(event.allDay);
        calendar.setTimeInMillis(event.startAt);
        updateDateTimeButtons();
        for (int i = 0; i < REMINDER_MINUTES.length; i++) {
            if (REMINDER_MINUTES[i] == event.reminderMinutesBefore) {
                spinnerReminder.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            updateDateTimeButtons();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateDateTimeButtons();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateDateTimeButtons() {
        buttonPickDate.setText(DateUtil.formatDate(calendar.getTimeInMillis()));
        buttonPickTime.setText(String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
    }

    private void save() {
        String title = inputTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            inputTitle.setError(getString(R.string.hint_title));
            return;
        }

        event.title = title;
        event.description = inputDescription.getText().toString().trim();
        event.allDay = switchAllDay.isChecked();
        if (event.allDay) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
        }
        event.startAt = calendar.getTimeInMillis();
        event.reminderMinutesBefore = REMINDER_MINUTES[spinnerReminder.getSelectedItemPosition()];

        int repeatIndex = isNewEvent ? spinnerRepeat.getSelectedItemPosition() : REPEAT_NONE;
        if (repeatIndex != REPEAT_NONE) {
            saveRecurringSeries(repeatIndex);
        } else {
            saveSingleEvent();
        }
    }

    private void saveSingleEvent() {
        AppDatabase.databaseExecutor.execute(() -> {
            if (event.id == 0) {
                event.id = database.scheduleDao().insert(event);
            } else {
                database.scheduleDao().update(event);
            }
            ReminderScheduler.cancel(getApplicationContext(), event);
            if (!event.allDay) {
                ReminderScheduler.schedule(getApplicationContext(), event);
            }
            com.fulljournal.app.widget.TodayWidgetProvider.refreshAll(getApplicationContext());
            runOnUiThread(this::finish);
        });
    }

    private void saveRecurringSeries(int repeatIndex) {
        int count;
        int calendarField;
        switch (repeatIndex) {
            case REPEAT_DAILY:
                count = REPEAT_DAILY_COUNT;
                calendarField = Calendar.DAY_OF_MONTH;
                break;
            case REPEAT_WEEKLY:
                count = REPEAT_WEEKLY_COUNT;
                calendarField = Calendar.WEEK_OF_YEAR;
                break;
            case REPEAT_MONTHLY:
            default:
                count = REPEAT_MONTHLY_COUNT;
                calendarField = Calendar.MONTH;
                break;
        }

        List<ScheduleEvent> events = new ArrayList<>();
        Calendar occurrence = (Calendar) calendar.clone();
        for (int i = 0; i < count; i++) {
            ScheduleEvent copy = new ScheduleEvent();
            copy.title = event.title;
            copy.description = event.description;
            copy.allDay = event.allDay;
            copy.reminderMinutesBefore = event.reminderMinutesBefore;
            copy.startAt = occurrence.getTimeInMillis();
            events.add(copy);
            occurrence.add(calendarField, 1);
        }

        AppDatabase.databaseExecutor.execute(() -> {
            List<Long> ids = database.scheduleDao().insertAll(events);
            for (int i = 0; i < events.size(); i++) {
                ScheduleEvent saved = events.get(i);
                saved.id = ids.get(i);
                if (!saved.allDay) {
                    ReminderScheduler.schedule(getApplicationContext(), saved);
                }
            }
            com.fulljournal.app.widget.TodayWidgetProvider.refreshAll(getApplicationContext());
            runOnUiThread(this::finish);
        });
    }

    private void delete() {
        AppDatabase.databaseExecutor.execute(() -> {
            ReminderScheduler.cancel(getApplicationContext(), event);
            database.scheduleDao().delete(event);
            com.fulljournal.app.widget.TodayWidgetProvider.refreshAll(getApplicationContext());
            runOnUiThread(this::finish);
        });
    }
}
