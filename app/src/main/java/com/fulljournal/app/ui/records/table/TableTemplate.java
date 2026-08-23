package com.fulljournal.app.ui.records.table;

import com.fulljournal.app.data.entity.ColumnType;
import com.fulljournal.app.data.entity.RecordColumn;

import java.util.ArrayList;
import java.util.List;

/** A preset table name + column set that TableEditActivity can prefill from. */
public class TableTemplate {

    public final String name;
    private final List<ColumnDef> columnDefs;

    private TableTemplate(String name, List<ColumnDef> columnDefs) {
        this.name = name;
        this.columnDefs = columnDefs;
    }

    public List<RecordColumn> buildColumns() {
        List<RecordColumn> columns = new ArrayList<>();
        int sortOrder = 0;
        for (ColumnDef def : columnDefs) {
            RecordColumn column = new RecordColumn();
            column.name = def.name;
            column.type = def.type;
            column.choiceOptions = def.choiceOptions;
            column.sortOrder = sortOrder++;
            columns.add(column);
        }
        return columns;
    }

    private static class ColumnDef {
        final String name;
        final ColumnType type;
        final String choiceOptions;

        ColumnDef(String name, ColumnType type) {
            this(name, type, null);
        }

        ColumnDef(String name, ColumnType type, String choiceOptions) {
            this.name = name;
            this.type = type;
            this.choiceOptions = choiceOptions;
        }
    }

    public static List<TableTemplate> defaults() {
        List<TableTemplate> templates = new ArrayList<>();

        templates.add(new TableTemplate("体重表", listOf(
                new ColumnDef("体重(kg)", ColumnType.NUMBER),
                new ColumnDef("体脂肪率(%)", ColumnType.NUMBER))));

        templates.add(new TableTemplate("血圧表", listOf(
                new ColumnDef("最高血圧", ColumnType.NUMBER),
                new ColumnDef("最低血圧", ColumnType.NUMBER),
                new ColumnDef("脈拍", ColumnType.NUMBER))));

        templates.add(new TableTemplate("支出表", listOf(
                new ColumnDef("金額", ColumnType.NUMBER),
                new ColumnDef("カテゴリ", ColumnType.CHOICE, "食費,交通費,娯楽費,日用品,その他"),
                new ColumnDef("メモ", ColumnType.TEXT))));

        templates.add(new TableTemplate("運動記録表", listOf(
                new ColumnDef("種目", ColumnType.CHOICE, "ウォーキング,ランニング,筋トレ,その他"),
                new ColumnDef("時間(分)", ColumnType.NUMBER),
                new ColumnDef("消費カロリー", ColumnType.NUMBER))));

        return templates;
    }

    private static List<ColumnDef> listOf(ColumnDef... defs) {
        List<ColumnDef> list = new ArrayList<>();
        for (ColumnDef def : defs) {
            list.add(def);
        }
        return list;
    }
}
