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

    private final List<RecordTable> tables = new ArrayList<>();
    private final OnTableClickListener listener;

    public RecordTableAdapter(OnTableClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<RecordTable> newTables) {
        tables.clear();
        tables.addAll(newTables);
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
        RecordTable table = tables.get(position);
        holder.name.setText(table.name);
        holder.name.setOnClickListener(v -> listener.onTableClick(table));
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
        }
    }
}
