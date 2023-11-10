package com.fhostert.gymtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.fhostert.gymtracker.datastructures.Settings;
import com.fhostert.gymtracker.helper.DatabaseManager;

public class SettingsActivity extends AppCompatActivity {

    private Settings settings;

    EditText timerDuration;
    CheckBox timerAutoStart;
    CheckBox timerPlay3Seconds;
    CheckBox timerPlay10Seconds;
    CheckBox timerVibrateAt3;
    CheckBox timerVibrateAt10;

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

        timerVibrateAt3 = findViewById(R.id.timer_vibrate_at_10_box);
        timerVibrateAt3.setChecked(settings.timerVibrateAt10Seconds);

        timerVibrateAt10 = findViewById(R.id.timer_vibrate_at_3_box);
        timerVibrateAt10.setChecked(settings.timerVibrateAt3Seconds);
    }

    private void saveSettings() {
        settings.timerDuration = Integer.parseInt(timerDuration.getText().toString());
        settings.timerAutoPlay = timerAutoStart.isChecked();
        settings.timerPlay3Seconds = timerPlay3Seconds.isChecked();
        settings.timerPlay10Seconds = timerPlay10Seconds.isChecked();
        settings.timerVibrateAt3Seconds = timerVibrateAt3.isChecked();
        settings.timerVibrateAt10Seconds = timerVibrateAt10.isChecked();

        DatabaseManager.setSettings(settings);
        setResult(RESULT_OK);
        Toast.makeText(this,
                getResources().getString(R.string.settingsSaved),
                Toast.LENGTH_SHORT).show();
        finish();
    }
}