package com.fulljournal.app.data;

import androidx.room.TypeConverter;

import com.fulljournal.app.data.entity.ColumnType;

public class Converters {

    @TypeConverter
    public static ColumnType toColumnType(String value) {
        return value == null ? ColumnType.NUMBER : ColumnType.valueOf(value);
    }

    @TypeConverter
    public static String fromColumnType(ColumnType type) {
        return type == null ? ColumnType.NUMBER.name() : type.name();
    }
}
