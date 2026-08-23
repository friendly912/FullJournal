package com.fulljournal.app.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.ScheduleEvent;
import com.fulljournal.app.util.DateUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ScheduleFragment extends Fragment {

    private AppDatabase database;
    private ScheduleEventAdapter adapter;
    private LiveData<List<ScheduleEvent>> currentLiveData;
    private long selectedDayStart;
    private RecyclerView eventList;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        eventList = view.findViewById(R.id.event_list);
        emptyView = view.findViewById(R.id.empty_view);
        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_event);

        adapter = new ScheduleEventAdapter(event ->
                startActivity(new Intent(requireContext(), ScheduleEditActivity.class)
                        .putExtra(ScheduleEditActivity.EXTRA_EVENT_ID, event.id)));
        eventList.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventList.setAdapter(adapter);

        selectedDayStart = DateUtil.startOfDay(calendarView.getDate());
        observeDay(selectedDayStart);

        calendarView.setOnDateChangeListener((widget, year, month, dayOfMonth) -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDayStart = DateUtil.startOfDay(c.getTimeInMillis());
            observeDay(selectedDayStart);
        });

        fab.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), ScheduleEditActivity.class)
                        .putExtra(ScheduleEditActivity.EXTRA_DEFAULT_DATE, selectedDayStart)));
    }

    private void observeDay(long dayStart) {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }
        long dayEnd = DateUtil.endOfDay(dayStart);
        currentLiveData = database.scheduleDao().observeEventsForDay(dayStart, dayEnd);
        currentLiveData.observe(getViewLifecycleOwner(), events -> {
            adapter.submitList(events);
            boolean empty = events == null || events.isEmpty();
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            eventList.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }
}
