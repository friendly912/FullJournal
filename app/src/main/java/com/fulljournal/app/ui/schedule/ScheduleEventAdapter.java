package com.fulljournal.app.ui.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.entity.ScheduleEvent;
import com.fulljournal.app.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class ScheduleEventAdapter extends RecyclerView.Adapter<ScheduleEventAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(ScheduleEvent event);
    }

    private final List<ScheduleEvent> events = new ArrayList<>();
    private final OnEventClickListener listener;

    public ScheduleEventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ScheduleEvent> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleEvent event = events.get(position);
        holder.title.setText(event.title);
        if (event.allDay) {
            holder.time.setText(R.string.label_all_day);
        } else {
            holder.time.setText(DateUtil.formatDateTime(event.startAt));
        }
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView time;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            time = itemView.findViewById(R.id.text_time);
        }
    }
}
