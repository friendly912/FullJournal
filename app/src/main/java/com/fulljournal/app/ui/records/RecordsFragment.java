package com.fulljournal.app.ui.records;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.data.entity.RecordTable;
import com.fulljournal.app.ui.records.stats.RecordGapDetector;
import com.fulljournal.app.ui.records.table.TableEditActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecordsFragment extends Fragment {

    private AppDatabase database;
    private RecordTableAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = AppDatabase.getInstance(requireContext());

        RecyclerView tableList = view.findViewById(R.id.table_list);
        TextView emptyView = view.findViewById(R.id.empty_view);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_table);

        adapter = new RecordTableAdapter(table ->
                startActivity(new Intent(requireContext(), DataEntryActivity.class)
                        .putExtra(DataEntryActivity.EXTRA_TABLE_ID, table.id)
                        .putExtra(DataEntryActivity.EXTRA_TABLE_NAME, table.name)));
        tableList.setLayoutManager(new LinearLayoutManager(requireContext()));
        tableList.setAdapter(adapter);

        database.recordTableDao().observeAll().observe(getViewLifecycleOwner(), tables ->
                AppDatabase.databaseExecutor.execute(() -> {
                    List<TableListItem> items = buildItems(tables);
                    if (!isAdded()) {
                        return;
                    }
                    requireActivity().runOnUiThread(() -> {
                        adapter.submitList(items);
                        boolean empty = items.isEmpty();
                        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                        tableList.setVisibility(empty ? View.GONE : View.VISIBLE);
                    });
                }));

        fab.setOnClickListener(v -> startActivity(new Intent(requireContext(), TableEditActivity.class)));
    }

    private List<TableListItem> buildItems(List<RecordTable> tables) {
        List<TableListItem> items = new ArrayList<>();
        for (RecordTable table : tables) {
            RecordGapDetector.GapInfo gapInfo =
                    RecordGapDetector.evaluate(database.recordRowDao().getForTableSync(table.id));
            items.add(new TableListItem(table, gapInfo));
        }
        return items;
    }
}
