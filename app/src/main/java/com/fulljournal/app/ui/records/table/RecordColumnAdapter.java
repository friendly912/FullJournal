package com.fulljournal.app.ui.records.table;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.entity.ColumnType;
import com.fulljournal.app.data.entity.RecordColumn;

import java.util.ArrayList;
import java.util.List;

public class RecordColumnAdapter extends RecyclerView.Adapter<RecordColumnAdapter.ViewHolder> {

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    private final List<RecordColumn> columns = new ArrayList<>();
    private final OnRemoveListener listener;

    public RecordColumnAdapter(OnRemoveListener listener) {
        this.listener = listener;
    }

    public void add(RecordColumn column) {
        columns.add(column);
        notifyItemInserted(columns.size() - 1);
    }

    public void removeAt(int position) {
        columns.remove(position);
        notifyItemRemoved(position);
    }

    public List<RecordColumn> getColumns() {
        return columns;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record_column, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordColumn column = columns.get(position);
        holder.name.setText(column.name);
        holder.type.setText(typeLabel(holder.itemView.getContext(), column.type));
        holder.remove.setOnClickListener(v -> listener.onRemove(holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return columns.size();
    }

    private static String typeLabel(android.content.Context context, ColumnType type) {
        switch (type) {
            case DATE:
                return context.getString(R.string.column_type_date);
            case TEXT:
                return context.getString(R.string.column_type_text);
            case CHOICE:
                return context.getString(R.string.column_type_choice);
            case NUMBER:
            default:
                return context.getString(R.string.column_type_number);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView type;
        final ImageButton remove;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_column_name);
            type = itemView.findViewById(R.id.text_column_type);
            remove = itemView.findViewById(R.id.button_remove_column);
        }
    }
}
