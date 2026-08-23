package com.fulljournal.app.ui.records;

import com.fulljournal.app.data.entity.RecordTable;
import com.fulljournal.app.ui.records.stats.RecordGapDetector;

public class TableListItem {
    public final RecordTable table;
    public final RecordGapDetector.GapInfo gapInfo;

    public TableListItem(RecordTable table, RecordGapDetector.GapInfo gapInfo) {
        this.table = table;
        this.gapInfo = gapInfo;
    }
}
