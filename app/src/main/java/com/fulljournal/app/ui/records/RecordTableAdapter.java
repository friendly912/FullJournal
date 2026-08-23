package com.fulljournal.app.ui.records;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.entity.RecordTable;

import java.util.ArrayList;
import java.util.List;

public class RecordTableAdapter extends RecyclerView.Adapter<RecordTableAdapter.ViewHolder> {

    public interface OnTableClickListener {
        void onTableClick(RecordTable table);
    }

    private final List<TableListItem> items = new ArrayList<>();
    private final OnTableClickListener listener;

    public RecordTableAdapter(OnTableClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TableListItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableListItem item = items.get(position);
        holder.name.setText(item.table.name);
        if (item.gapInfo.overdue) {
            holder.reminder.setVisibility(View.VISIBLE);
            holder.reminder.setText(holder.itemView.getContext()
                    .getString(R.string.record_gap_warning, item.gapInfo.daysSinceLastRecord));
        } else {
            holder.reminder.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onTableClick(item.table));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView reminder;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            reminder = itemView.findViewById(R.id.text_reminder);
        }
    }
}
