package com.fulljournal.app.ui.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.fulljournal.app.R;
import com.fulljournal.app.ui.BaseToolbarActivity;
import com.fulljournal.app.util.AppPrefs;

public class SettingsActivity extends BaseToolbarActivity {

    private static final int[] REMINDER_MINUTES = {-1, 5, 15, 30, 60, 1440};

    /** Index in theme_options matches this array: システム設定に従う / ライト / ダーク. */
    private static final int[] THEME_MODES = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar();
        setTitle(R.string.settings_title);

        Spinner spinnerReminder = findViewById(R.id.spinner_default_reminder);
        Spinner spinnerTheme = findViewById(R.id.spinner_theme);
        Button buttonSave = findViewById(R.id.button_save);
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

        versionText.setText(getString(R.string.app_name) + " " + versionName());
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
