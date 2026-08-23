package com.fulljournal.app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.fulljournal.app.data.AppDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Copies the whole Room SQLite database to/from a user-chosen file (Storage Access Framework Uri). */
public class BackupManager {

    public static void exportTo(Context context, Uri destinationUri) throws IOException {
        SupportSQLiteDatabase db = AppDatabase.getInstance(context).getOpenHelper().getWritableDatabase();
        Cursor cursor = db.query("PRAGMA wal_checkpoint(FULL)");
        cursor.close();

        File dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME);
        ContentResolver resolver = context.getContentResolver();
        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = resolver.openOutputStream(destinationUri)) {
            if (out == null) {
                throw new IOException("Unable to open destination for writing");
            }
            copy(in, out);
        }
    }

    public static void importFrom(Context context, Uri sourceUri) throws IOException {
        AppDatabase.closeAndReset();

        File dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME);
        ContentResolver resolver = context.getContentResolver();
        try (InputStream in = resolver.openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(dbFile)) {
            if (in == null) {
                throw new IOException("Unable to open backup file for reading");
            }
            copy(in, out);
        }

        deleteIfExists(new File(dbFile.getPath() + "-wal"));
        deleteIfExists(new File(dbFile.getPath() + "-shm"));
    }

    private static void deleteIfExists(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }
}
