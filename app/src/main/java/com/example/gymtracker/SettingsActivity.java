package com.example.gymtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gymtracker.datastructures.Settings;
import com.example.gymtracker.helper.DatabaseManager;

public class SettingsActivity extends AppCompatActivity {

    private Settings settings;

    EditText timerDuration;
    CheckBox timerAutoStart;
    CheckBox timerPlay3Seconds;
    CheckBox timerPlay10Seconds;
    CheckBox timerVibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.setTitle(R.string.settings);

        //Initialize buttons
        Button saveSettingsButton = findViewById(R.id.save_settings_button);
        saveSettingsButton.setOnClickListener(view1 -> saveSettings());

        settings = DatabaseManager.getSettings();

        timerDuration = findViewById(R.id.duration_timer_edit_text);
        timerDuration.setText(String.valueOf(settings.timerDuration));

        timerAutoStart = findViewById(R.id.auto_start_timer_box);
        timerAutoStart.setChecked(settings.timerAutoPlay);

        timerPlay3Seconds = findViewById(R.id.timer_3_box);
        timerPlay3Seconds.setChecked(settings.timerPlay3Seconds);

        timerPlay10Seconds = findViewById(R.id.timer_10_box);
        timerPlay10Seconds.setChecked(settings.timerPlay10Seconds);

        timerVibrate = findViewById(R.id.timer_vibrate_box);
        timerVibrate.setChecked(settings.timerVibrate);
    }

    private void saveSettings() {
        settings.timerDuration = Integer.parseInt(timerDuration.getText().toString());
        settings.timerAutoPlay = timerAutoStart.isChecked();
        settings.timerPlay3Seconds = timerPlay3Seconds.isChecked();
        settings.timerPlay10Seconds = timerPlay10Seconds.isChecked();
        settings.timerVibrate = timerVibrate.isChecked();

        DatabaseManager.setSettings(settings);
        setResult(RESULT_OK);
        finish();
    }
}