package com.fulljournal.app.ui.search;

public class SearchResultItem {

    public enum Type { SCHEDULE, RECORD_TABLE, RECORD_ROW }

    public final Type type;
    public final String title;
    public final String subtitle;
    public final long targetId;
    public final String tableName;

    private SearchResultItem(Type type, String title, String subtitle, long targetId, String tableName) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.targetId = targetId;
        this.tableName = tableName;
    }

    public static SearchResultItem forSchedule(long eventId, String title, String subtitle) {
        return new SearchResultItem(Type.SCHEDULE, title, subtitle, eventId, null);
    }

    public static SearchResultItem forTable(long tableId, String tableName, String subtitle) {
        return new SearchResultItem(Type.RECORD_TABLE, tableName, subtitle, tableId, tableName);
    }

    public static SearchResultItem forRow(long tableId, String tableName, String title, String subtitle) {
        return new SearchResultItem(Type.RECORD_ROW, title, subtitle, tableId, tableName);
    }
}
