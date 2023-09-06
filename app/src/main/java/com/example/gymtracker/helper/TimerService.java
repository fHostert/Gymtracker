package com.example.gymtracker.helper;

import android.app.Notification;
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

    long startTime;
    long endTime;
    float progress;
    Handler countDownHandler;
    Runnable countDownRunnable;
    MediaPlayer mediaPlayer;
    int lastFullSeconds;
    private NotificationCompat.Builder notificationBuilder;

    boolean timerIsActive;
    boolean timerIsRunning;
    boolean timerIsExpired;

    boolean timer10SecondsSoundPlayed = false;
    boolean timer3SecondsSoundPlayed = false;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        timerIsActive = true;
        Log.d("TIMER", "Service onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TIMER", "Service onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timerIsActive = true;
        Log.d("TIMER", "Service onStartCommand");
        return START_STICKY;
    }

    public void startCountdown() {
        Log.d("TIMER", "Service startCountdown");
        createNotification();
        startForeground(69, notificationBuilder.build());
        int duration = DatabaseManager.getSettings().timerDuration;
        timerIsActive = true;
        timerIsRunning = true;
        timerIsExpired = false;
        startTime = System.currentTimeMillis();
        endTime = startTime + (duration * 1000L);
        lastFullSeconds = duration;

        countDownHandler = new Handler();
        countDownRunnable = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < endTime) {
                    progress = (float)(endTime - System.currentTimeMillis()) / (duration * 1000L);
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
                    sendBroadcast(broadcast);

                    countDownHandler.postDelayed(this, 10);
                } else {
                    timer3SecondsSoundPlayed = false;
                    timer10SecondsSoundPlayed = false;
                    timerIsRunning = false;
                    timerIsExpired = true;
                    updateNotification(getResources().getString(R.string.timerExpired));
                    progress = 0.0f;

                    broadcast.putExtra("PROGRESS", progress);
                    sendBroadcast(broadcast);
                }
            }
        };
        countDownHandler.postDelayed(countDownRunnable, 0);
    }

    public void add10Seconds() {
        timer3SecondsSoundPlayed = false;
        timer10SecondsSoundPlayed = false;
        endTime += 10000;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void deactivate() {
        Log.d("TIMER", "Service deactivate");
        if (countDownHandler != null) {
            countDownHandler.removeCallbacks(countDownRunnable);
        }
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
        timerIsActive = false;
        timerIsRunning = false;
        timerIsExpired = false;
        progress = 1.0f;
        broadcast.putExtra("PROGRESS", progress);
        sendBroadcast(broadcast);

        stopForeground(true);
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

    public boolean getTimerIsActive() {
        return timerIsActive;
    }

    public boolean getTimerIsRunning() {
        return timerIsRunning;
    }

    public boolean getTimerIsExpired() {
        return timerIsExpired;
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
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