package com.fhostert.gymtracker.helper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fhostert.gymtracker.MainActivity;
import com.fhostert.gymtracker.R;
import com.fhostert.gymtracker.datastructures.Settings;

public class TimerNotificationService extends Service {
    private NotificationCompat.Builder notificationBuilder;
    Handler handler;
    Runnable runnable;
    int lastFullSeconds;
    long endTime;

    boolean timer10SecondsSoundPlayed = false;
    boolean timer3SecondsSoundPlayed = false;
    MediaPlayer mediaPlayer;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCountdown();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("TIMER", "Service onDestroy");
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    public void startCountdown() {
        Log.d("TIMER", "Service startCountdown");
        createNotification();
        startForeground(420, notificationBuilder.build());
        timer10SecondsSoundPlayed = false;
        timer3SecondsSoundPlayed = false;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    endTime = DatabaseManager.getCurrentWorkoutTimerEnd();
                }
                catch (Exception e)
                {
                    handler.removeCallbacks(runnable);
                    stopSelf();
                }

                if (System.currentTimeMillis() < endTime) {
                    long millisRemaining = (endTime - System.currentTimeMillis());

                    int fullSeconds = (int) Math.round(millisRemaining / 1000.0);
                    if (fullSeconds == 1 && lastFullSeconds != fullSeconds) {
                        updateNotification(getResources().getString(R.string.timer) + " " + fullSeconds + " " + getResources().getString(R.string.second));
                    }
                    else if (lastFullSeconds != fullSeconds) {
                        updateNotification(getResources().getString(R.string.timer) + " " + fullSeconds + " " + getResources().getString(R.string.seconds));
                    }
                    lastFullSeconds = fullSeconds;

                    //audio logic
                    if (millisRemaining < 10500) {
                        timerUnder10Seconds();
                        timer10SecondsSoundPlayed = true;
                    }
                    if (millisRemaining < 3500) {
                        timerUnder3Seconds();
                        timer3SecondsSoundPlayed = true;
                    }

                    handler.postDelayed(this, 100);
                } else {
                    updateNotification(getResources().getString(R.string.timerExpired));
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    private void timerUnder10Seconds() {
        if (timer10SecondsSoundPlayed)
            return;
        Settings settings = DatabaseManager.getSettings();
        if (settings.timerVibrateAt10Seconds)
        {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        if (!settings.timerPlay10Seconds)
            return;
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_10_seconds);
        mediaPlayer.start();
    }

    private void timerUnder3Seconds() {
        if (timer3SecondsSoundPlayed)
            return;
        Settings settings = DatabaseManager.getSettings();
        if (settings.timerVibrateAt3Seconds)
        {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        if (!settings.timerPlay3Seconds)
            return;
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_3_seconds);
        mediaPlayer.start();
    }

    private void updateNotification(String text) {
        if (notificationBuilder != null) {
            notificationBuilder.setContentText(text);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(420, notificationBuilder.build());
        }
    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder = new NotificationCompat.Builder(this, "420")
                .setContentTitle(getResources().getString(R.string.notificationTitle))
                .setSmallIcon(R.drawable.ic_fitness_center_24)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

}