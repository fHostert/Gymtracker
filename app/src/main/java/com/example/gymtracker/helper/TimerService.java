package com.example.gymtracker.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gymtracker.MainActivity;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Settings;

import java.text.DecimalFormat;

public class TimerService extends Service {
    Intent broadcast = new Intent("COUNTDOWN");
    IBinder mBinder = new LocalBinder();

    CountDownTimer countDownTimer = null;
    private int duration;
    long startTime;
    long endTime;
    Handler countDownHandler;
    Runnable countDownRunnable;
    MediaPlayer mediaPlayer;
    int lastFullSeconds;
    private NotificationCompat.Builder notificationBuilder;

    boolean timer10SecondsSoundPlayed = false;
    boolean timer3SecondsSoundPlayed = false;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Bundle extras = intent.getExtras();

        if(extras != null) {
            duration = (int) extras.getFloat("DURATION");
        }

        startCountdown();
        createNotification();
        startForeground(69, notificationBuilder.build());


        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("69", "Timer Notification", importance);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    public void add10Seconds() {
        timer3SecondsSoundPlayed = false;
        timer10SecondsSoundPlayed = false;
        endTime += 10000;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void startCountdown() {
        startTime = System.currentTimeMillis();
        endTime = (long) (startTime + (duration * 1000L));
        lastFullSeconds = duration;

        countDownHandler = new Handler();
        countDownRunnable = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < endTime) {
                    float progress = (float)(endTime - System.currentTimeMillis()) / (duration * 1000L);
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

                    broadcast.putExtra("PROGRESS", progress);
                    broadcast.putExtra("REMAINING", (float)millisRemaining / 1000);
                    sendBroadcast(broadcast);

                    countDownHandler.postDelayed(this, 10);
                } else {
                    timer3SecondsSoundPlayed = false;
                    timer10SecondsSoundPlayed = false;
                    updateNotification(getResources().getString(R.string.timerExpired));

                    broadcast.putExtra("PROGRESS", 0.0f);
                    broadcast.putExtra("REMAINING", 0.0f);
                    sendBroadcast(broadcast);

                    onDestroy();
                }
            }
        };
        countDownHandler.postDelayed(countDownRunnable, 0);
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (countDownHandler != null) {
            countDownHandler.removeCallbacks(countDownRunnable);
        }
        if (notificationBuilder != null) {
            notificationBuilder.setContentText(getResources().getString(R.string.notificationText));
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(69, notificationBuilder.build());
        }
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

    public void resetAudio() {
        timer3SecondsSoundPlayed = false;
        timer10SecondsSoundPlayed = false;
    }

    private void updateNotification(String text) {
        if (notificationBuilder != null) {
            notificationBuilder.setContentText(text);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(69, notificationBuilder.build());
        }
    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = new NotificationCompat.Builder(this, "69")
                .setContentTitle(getResources().getString(R.string.notificationTitle))
                .setSmallIcon(R.drawable.ic_fitness_center_24)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public TimerService getTimerServiceInstance() {
            return TimerService.this;
        }
    }
}