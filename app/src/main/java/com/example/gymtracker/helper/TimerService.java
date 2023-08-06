package com.example.gymtracker.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gymtracker.MainActivity;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Settings;

public class TimerService extends Service {
    Intent broadcast = new Intent("COUNTDOWN");
    IBinder mBinder = new LocalBinder();

    CountDownTimer countDownTimer = null;
    private float duration;
    private float secondsRemaining;
    private float progress;
    private NotificationCompat.Builder notificationBuilder;

    boolean timer10SecondsSoundPlayed = false;
    boolean timer3SecondsSoundPlayed = false;

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Bundle extras = intent.getExtras();

        if(extras != null) {
            duration = extras.getFloat("DURATION");
        }
        secondsRemaining = duration;
        setCountDownTimer(secondsRemaining, duration);

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
        countDownTimer.cancel();
        secondsRemaining += 10;
        setCountDownTimer(secondsRemaining, duration);
    }

    private void setCountDownTimer(float timerDuration, float originalDuration) {
        countDownTimer = new CountDownTimer((long) (timerDuration * 1000L), 100) {
            public void onTick(long millisUntilFinished) {
                secondsRemaining -= 0.1;
                progress = secondsRemaining / originalDuration;

                broadcast.putExtra("PROGRESS", (float)progress);
                broadcast.putExtra("REMAINING", (float)secondsRemaining);
                sendBroadcast(broadcast);

                int fullSeconds = Math.round(secondsRemaining);
                if (fullSeconds == 1) {
                    updateNotification(getResources().getString(R.string.timer) + " " + fullSeconds + " " + getResources().getString(R.string.second));
                }
                else if (fullSeconds == 0) {
                    updateNotification(getResources().getString(R.string.timerExpired));
                }
                else {
                    updateNotification(getResources().getString(R.string.timer) + " " + fullSeconds + " " + getResources().getString(R.string.seconds));
                }

                if (fullSeconds == 10) {
                    timerUnder10Seconds();
                    timer10SecondsSoundPlayed = true;
                }
                if (fullSeconds == 3) {
                    timerUnder3Seconds();
                    timer3SecondsSoundPlayed = true;
                }
            }

            public void onFinish() {
                broadcast.putExtra("PROGRESS", 0.0f);
                broadcast.putExtra("REMAINING", 0.0f);
                sendBroadcast(broadcast);
                timer3SecondsSoundPlayed = false;
                timer10SecondsSoundPlayed = false;
                onDestroy();
            }
        }.start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
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
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound_10_seconds);
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
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound_3_seconds);
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
                .setContentIntent(pendingIntent);
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