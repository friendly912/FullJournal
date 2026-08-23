package com.fulljournal.app.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.fulljournal.app.MainActivity;
import com.fulljournal.app.R;
import com.fulljournal.app.data.AppDatabase;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.fulljournal.app.util.AppPrefs;
import com.fulljournal.app.util.BackupManager;

import java.io.IOException;
import java.util.Date;

public class SettingsActivity extends BaseToolbarActivity {

    private static final int[] REMINDER_MINUTES = {-1, 5, 15, 30, 60, 1440};

    /** Index in theme_options matches this array: システム設定に従う / ライト / ダーク. */
    private static final int[] THEME_MODES = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
    };

    private final ActivityResultLauncher<String> createBackupLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/octet-stream"),
                    uri -> {
                        if (uri != null) {
                            runBackup(uri);
                        }
                    });

    private final ActivityResultLauncher<String[]> openBackupLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            confirmRestore(uri);
                        }
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar();
        setTitle(R.string.settings_title);

        Spinner spinnerReminder = findViewById(R.id.spinner_default_reminder);
        Spinner spinnerTheme = findViewById(R.id.spinner_theme);
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonBackup = findViewById(R.id.button_backup);
        Button buttonRestore = findViewById(R.id.button_restore);
        TextView versionText = findViewById(R.id.text_version);

        int currentDefault = AppPrefs.getDefaultReminderMinutes(this);
        for (int i = 0; i < REMINDER_MINUTES.length; i++) {
            if (REMINDER_MINUTES[i] == currentDefault) {
                spinnerReminder.setSelection(i);
                break;
            }
        }

        int currentTheme = AppPrefs.getThemeMode(this);
        for (int i = 0; i < THEME_MODES.length; i++) {
            if (THEME_MODES[i] == currentTheme) {
                spinnerTheme.setSelection(i);
                break;
            }
        }

        buttonSave.setOnClickListener(v -> {
            AppPrefs.setDefaultReminderMinutes(this, REMINDER_MINUTES[spinnerReminder.getSelectedItemPosition()]);
            int selectedTheme = THEME_MODES[spinnerTheme.getSelectedItemPosition()];
            AppPrefs.setThemeMode(this, selectedTheme);
            AppCompatDelegate.setDefaultNightMode(selectedTheme);
            finish();
        });

        buttonBackup.setOnClickListener(v -> {
            String suggestedName = "fulljournal_backup_"
                    + DateFormat.format("yyyyMMdd_HHmm", new Date()) + ".db";
            createBackupLauncher.launch(suggestedName);
        });

        buttonRestore.setOnClickListener(v -> openBackupLauncher.launch(new String[]{"*/*"}));

        versionText.setText(getString(R.string.app_name) + " " + versionName());
    }

    private void runBackup(Uri destinationUri) {
        AppDatabase.databaseExecutor.execute(() -> {
            try {
                BackupManager.exportTo(getApplicationContext(), destinationUri);
                runOnUiThread(() -> Toast.makeText(this, R.string.msg_backup_success, Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.msg_backup_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmRestore(Uri sourceUri) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_restore_confirm)
                .setPositiveButton(R.string.action_restore, (dialog, which) -> runRestore(sourceUri))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void runRestore(Uri sourceUri) {
        AppDatabase.databaseExecutor.execute(() -> {
            try {
                BackupManager.importFrom(getApplicationContext(), sourceUri);
                runOnUiThread(this::restartApp);
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.msg_restore_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    private String versionName() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
