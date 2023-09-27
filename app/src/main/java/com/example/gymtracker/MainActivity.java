package com.example.gymtracker;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import static androidx.core.content.FileProvider.getUriForFile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.datastructures.Settings;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.TimerBar;

import com.example.gymtracker.helper.TimerNotificationService;
import com.example.gymtracker.history.HistoryFragment;
import com.example.gymtracker.templates.EditTemplateActivity;
import com.example.gymtracker.workout.WorkoutFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final int notificationId = 69;
    boolean doubleBackToExitPressedOnce = false;

    //timer
    private MenuItem startTimerMenuItem;
    private MenuItem activateTimerMenuItem;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup database
        SQLiteDatabase db = openOrCreateDatabase("Gymtracker", MODE_PRIVATE,null);
        DatabaseManager.initialize(db);
        DatabaseManager.createExercisesTable(getResources().getStringArray(R.array.exercises));
        DatabaseManager.createHistoryTable();
        DatabaseManager.createWorkoutsTable();
        DatabaseManager.createTemplatesTable();
        DatabaseManager.createSettingsTable();

        //Bottom Navigation View Setup
        BottomNavigationView navView = findViewById(R.id.nav_view);
        ColorStateList iconColorStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        Color.parseColor("#a0a3a8"),
                        Color.parseColor("#FFFFFF"),
                });

        navView.setItemIconTintList(iconColorStates);
        navView.setItemTextColor(iconColorStates);

        FragmentContainerView homeContainer = findViewById(R.id.home_container);
        FragmentContainerView historyContainer = findViewById(R.id.history_container);
        FragmentContainerView statsContainer = findViewById(R.id.stats_container);

        //Click Logic
        historyContainer.setVisibility(View.INVISIBLE);
        statsContainer.setVisibility(View.INVISIBLE);
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                homeContainer.setVisibility(View.VISIBLE);
                historyContainer.setVisibility(View.INVISIBLE);
                statsContainer.setVisibility(View.INVISIBLE);
                if (DatabaseManager.doesTableExist("CurrentWorkoutMetadata"))
                {
                    this.setTitle(DatabaseManager.getCurrentWorkoutName());
                }
                else {
                    this.setTitle(getResources().getString(R.string.app_name));
                }

            }
            else if (item.getItemId() == R.id.navigation_history) {
                historyContainer.setVisibility(View.VISIBLE);
                homeContainer.setVisibility(View.INVISIBLE);
                statsContainer.setVisibility(View.INVISIBLE);
                this.setTitle(R.string.history);
            }
            else if (item.getItemId() == R.id.navigation_stats) {
                statsContainer.setVisibility(View.VISIBLE);
                homeContainer.setVisibility(View.INVISIBLE);
                historyContainer.setVisibility(View.INVISIBLE);
                this.setTitle(R.string.stats);
            }
            return true;
        });

        //restore the last workout if it did not exit properly
        restoreWorkout();

        //Check if app got opened by clicking on a .Gymtracker database
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri fileUri = intent.getData();
            importDatabase(fileUri);
        }


        handleTimer();


        /*findViewById(R.id.main_constraint_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                findViewById(R.id.main_constraint_layout).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                handleTimer();
            }
        });*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DatabaseManager.doesTableExist("CurrentWorkout")){
            getMenuInflater().inflate(R.menu.workout_menu, menu);
            startTimerMenuItem = menu.findItem(R.id.timer_start_add10_menu);
            activateTimerMenuItem = menu.findItem(R.id.timer_activate_deactivate_menu);
            startTimerMenuItem.setVisible(false);
            Log.d("TIMER", "onCreateOptionsMenus " + DatabaseManager.getCurrentWorkoutTimerIsActive());
            if (DatabaseManager.getCurrentWorkoutTimerIsActive()) {
                activateTimer();
            }

        }
        else {
            getMenuInflater().inflate(R.menu.home_menu, menu);
            invalidateOptionsMenu();
        }

        //Color icons
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.setTint(getColor(R.color.white));
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this,
                getResources().getString(R.string.doublePressToExit),
                Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(()
                -> doubleBackToExitPressedOnce=false, 2000);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //workout buttons
        if (id == R.id.change_workout_name) {
            changeWorkoutName();
        }
        else if (id == R.id.create_new_exercise_menu_workout ||
                id == R.id.create_new_exercise_menu_home) {
            createNewExercise();
        }
        else if (id == R.id.save_workout_menu) {
            saveWorkout();
        }
        else if (id == R.id.quit_workout_menu) {
            quitWorkout();
        }
        else if (id == R.id.timer_activate_deactivate_menu) {
            Log.d("TIMER", "menuklick vorher " + DatabaseManager.getCurrentWorkoutTimerIsActive());
            if(DatabaseManager.getCurrentWorkoutTimerIsActive()) {
                deactivateTimer();
            }
            else {
                activateTimer();
            }
            item.getIcon().setTint(getColor(R.color.white));
            Log.d("TIMER", "menuklick anchher " + DatabaseManager.getCurrentWorkoutTimerIsActive());
        }
        else if (id == R.id.timer_start_add10_menu) {
            if(System.currentTimeMillis() < DatabaseManager.getCurrentWorkoutTimerEnd()) {
                addToTimer();
            }
            else {
                startTimer();
            }
            item.getIcon().setTint(getColor(R.color.white));
        }
        else if (id == R.id.settings_menu_workout ||
                id == R.id.settings_menu_home) {
            openSettings();
        }
        //home buttons
        else if (id == R.id.delete_exercise_menu) {
            deleteExercise();
        }
        else if (id == R.id.rename_exercise_menu) {
            renameExercise();
        }
        else if (id == R.id.create_new_template_menu) {
            createNewTemplate();
        }
        else if (id == R.id.export_data_menu) {
            exportDatabase();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //delete Exercise
        if (resultCode == RESULT_OK && requestCode == 0) {
            deleteExercise(data.getExtras().getString("ITEM"));
        }
        //rename Exercise
        else if (resultCode == RESULT_OK && requestCode == 1) {
            renameExercise(data.getExtras().getString("ITEM"));
        }
        //create new template
        else if (resultCode == RESULT_OK && requestCode == 2) {
            reload();
        }
        //import database
        else if (resultCode == RESULT_OK && requestCode == 3) {
            importDatabase(data.getData());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TIMER", "Main onResume");



    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void reload() {
        Fragment newHome = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_container, newHome).commit();
    }

    public void restoreWorkout() {
        Workout workout = DatabaseManager.getCurrentWorkout();
        //build new workout
        if (workout != null) {
            WorkoutFragment workoutFragment = WorkoutFragment.newInstance(workout);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_container, workoutFragment,
                "WORKOUT_FRAGMENT").commit();
            getSupportFragmentManager().executePendingTransactions();
            this.setTitle(DatabaseManager.getCurrentWorkoutName());
            startWorkout();
        }
    }

    /*##############################################################################################
    ##########################################HOME BUTTONS##########################################
    ##############################################################################################*/
    public void startEmptyWorkoutClick(View view) {
        WorkoutFragment newWorkout = new WorkoutFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_container, newWorkout,
            "WORKOUT_FRAGMENT").commit();
        DatabaseManager.createCurrentWorkoutTable();
        DatabaseManager.createCurrentWorkoutMetadataTable();
        DatabaseManager.setCurrentWorkoutMetadata(
                getResources().getString(R.string.defaultWorkoutName));

        startWorkout();
    }

    public void startWorkout() {
        this.setTitle(DatabaseManager.getCurrentWorkoutName());
        startNotification();
        invalidateOptionsMenu();
    }

    public void deleteExercise() {
        final Intent intent = new Intent(this, ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("TITLE", getResources().getString(R.string.deleteExercise));
        startActivityForResult(intent, 0);
    }

    public void deleteExercise(String exerciseName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.deleteExercise));
        alert.setMessage(getResources().getString(R.string.deleteExerciseText));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            DatabaseManager.deleteExercise(exerciseName);
            ((HistoryFragment) getSupportFragmentManager().
                    findFragmentByTag("history_fragment")).reload();
            Toast.makeText(this,
                    getResources().getString(R.string.exerciseDeleted),
                    Toast.LENGTH_SHORT).show();
        });
        //If cancel, return
        alert.setNegativeButton(getResources().getString(R.string.no), (dialog, whichButton) -> {
        });

        alert.show();
    }

    public void renameExercise() {
        final Intent intent = new Intent(this, ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("TITLE", getResources().getString(R.string.renameExercise));
        startActivityForResult(intent, 1);
    }

    public void renameExercise(String exerciseName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.changeExerciseName));
        alert.setMessage(getResources().getString(R.string.changeExerciseNameText));
        final View customLayout = getLayoutInflater().inflate(R.layout.alert, null);
        alert.setView(customLayout);

        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            EditText et = customLayout.findViewById(R.id.alert_input_edit_text);
            String newExerciseName = et.getText().toString();

            //Exercise already exists. Ask again and Merge both exercises
            if (newExerciseName.equals(exerciseName)) {
                AlertDialog.Builder newAlert = new AlertDialog.Builder(this);
                newAlert.setTitle(getResources().getString(R.string.mergeExercises));
                newAlert.setMessage(getResources().getString(R.string.mergeExercisesText));

                //If ok, continue
                newAlert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface1, i1) -> {
                    DatabaseManager.mergeExercises(newExerciseName, exerciseName);
                    Toast.makeText(this,
                            getResources().getString(R.string.exercisesMerged),
                            Toast.LENGTH_SHORT).show();
                    reload();
                });
                //If cancel, return
                newAlert.setNegativeButton(getResources().getString(R.string.no), (dialog, whichButton) -> {
                });

                newAlert.show();
            }
            //Just rename the exercise
            else {
                DatabaseManager.renameExercise(exerciseName, newExerciseName);
                Toast.makeText(this,
                        getResources().getString(R.string.exerciseRenamed),
                        Toast.LENGTH_SHORT).show();
                reload();
            }
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    public void createNewTemplate() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.addNewTemplate));
        alert.setMessage(getResources().getString(R.string.addNewTemplateText));
        final View customLayout = getLayoutInflater().inflate(R.layout.alert, null);
        alert.setView(customLayout);

        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            EditText et = customLayout.findViewById(R.id.alert_input_edit_text);
            String newTemplateName = et.getText().toString();

            if (DatabaseManager.doesTemplateExist(newTemplateName) || newTemplateName.equals("")) {
                Toast.makeText(this,
                        getResources().getString(R.string.toastTemplateAlreadyExists),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            final Intent intent = new Intent(this, EditTemplateActivity.class);
            intent.putExtra("NAME", newTemplateName);
            startActivityForResult(intent, 2);
            reload();

        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    private void exportDatabase() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.exportDataText));
        alert.setTitle(getResources().getString(R.string.exportData));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            boolean hasPermission =
                    (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED);

            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        112);
            }

            //copy current database
            String databasePath = this.getDatabasePath("Gymtracker").getAbsolutePath();
            String exportFileName = "Gymtracker.Gymtracker";
            File exportFile = new File(getExternalFilesDir(null), exportFileName);
            try {
                InputStream inputStream = new FileInputStream(databasePath);
                OutputStream outputStream = new FileOutputStream(exportFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri contentUri = FileProvider.getUriForFile(
                    this, "com.example.gymtracker.fileprovider", exportFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            this.startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.exportData)));
        });

        //If cancel, return
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
        });
        alert.show();
    }

    private void importDatabase(Uri uri) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.importDataText));
        alert.setTitle(getResources().getString(R.string.importData));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            AlertDialog.Builder alertCon = new AlertDialog.Builder(this);
            alertCon.setMessage(getResources().getString(R.string.importDataTextConfirmation));
            alertCon.setTitle(getResources().getString(R.string.importData));

            //If ok, continue
            alertCon.setPositiveButton(getResources().getString(R.string.ok), (dialogInterfaceCon, iCon) -> {

                //Overwrite old Database
                String oldDatabaseFilePath = this.getDatabasePath("Gymtracker").getAbsolutePath();
                try {
                    //Save old db
                    InputStream inputStream = new FileInputStream(oldDatabaseFilePath);
                    FileOutputStream outputStream = new FileOutputStream(oldDatabaseFilePath + "TEMP");
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();

                    //Copy new db
                    inputStream = getContentResolver().openInputStream(uri);
                    outputStream = new FileOutputStream(oldDatabaseFilePath);
                    buffer = new byte[4096];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();

                    try {
                        if(DatabaseManager.doesTableExist("Exercises")) {
                            Toast.makeText(this,
                                    getResources().getString(R.string.toastImportSuccess),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e) {
                        //Import failed, restore
                        inputStream = new FileInputStream(oldDatabaseFilePath + "TEMP");
                        outputStream = new FileOutputStream(oldDatabaseFilePath);
                        buffer = new byte[4096];
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();
                        Toast.makeText(this,
                                getResources().getString(R.string.toastImportFail),
                                Toast.LENGTH_SHORT).show();
                    }

                    //Reload UI
                    reload();
                    ((HistoryFragment) getSupportFragmentManager().
                            findFragmentByTag("history_fragment")).reload();
                    Toast.makeText(this,
                            getResources().getString(R.string.toastImportSuccess),
                            Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this,
                            getResources().getString(R.string.toastImportFail),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });
            //If cancel, return
            alertCon.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            });
            alertCon.show();
        });
        //If cancel, return
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
        });
        alert.show();
    }

    /*##############################################################################################
    #########################################WORKOUT BUTTONS########################################
    ##############################################################################################*/
    public void quitWorkout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.quitWorkoutText));
        alert.setTitle(getResources().getString(R.string.quitWorkout));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            DatabaseManager.dropTable("CurrentWorkout");
            DatabaseManager.dropTable("CurrentWorkoutMetadata");
            stopWorkout();
            Toast.makeText(this,
                    getResources().getString(R.string.workoutDeleted),
                    Toast.LENGTH_SHORT).show();
        });
        //If cancel, return
        alert.setNegativeButton(getResources().getString(R.string.no), (dialog, whichButton) -> {
        });

        alert.show();
    }

    public void saveWorkout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.saveWorkoutText));
        alert.setTitle(getResources().getString(R.string.saveWorkout));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            DatabaseManager.createHistoryTable();
            if (DatabaseManager.saveCurrentWorkout()) {
                ((HistoryFragment) getSupportFragmentManager().
                        findFragmentByTag("history_fragment")).update();
                stopWorkout();
                Toast.makeText(this,
                        getResources().getString(R.string.workoutSaved),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,
                        getResources().getString(R.string.toastCannotSaveEmptyWorkout),
                        Toast.LENGTH_SHORT).show();
            }

        });
        //If cancel, return
        alert.setNegativeButton(getResources().getString(R.string.no), (dialog, whichButton) -> {
        });

        alert.show();
    }

    private void stopWorkout() {
        reload();
        invalidateOptionsMenu();
        stopService(new Intent(this, TimerNotificationService.class));
        deleteNotification();
        this.setTitle(getResources().getString(R.string.app_name));
    }

    private void changeWorkoutName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.workoutChangeNameEnterName));
        alert.setTitle(getResources().getString(R.string.workoutChangeName));
        final View customLayout = getLayoutInflater().inflate(R.layout.alert, null);
        alert.setView(customLayout);

        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            EditText et = customLayout.findViewById(R.id.alert_input_edit_text);
            String newWorkoutName = et.getText().toString();
            this.setTitle(newWorkoutName);
            DatabaseManager.changeCurrentWorkoutName(newWorkoutName);
            Toast.makeText(this,
                    getResources().getString(R.string.nameChanged),
                    Toast.LENGTH_SHORT).show();
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    public void createNewExercise() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.createNewExerciseText));
        alert.setTitle(getResources().getString(R.string.createNewExercise));
        final View customLayout = getLayoutInflater().inflate(R.layout.alert, null);
        alert.setView(customLayout);

        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            EditText et = customLayout.findViewById(R.id.alert_input_edit_text);
            String newExerciseName = et.getText().toString();
            for (String exercise : DatabaseManager.getExercises()) {
                if (Objects.equals(exercise, newExerciseName) || newExerciseName.equals("")) {
                    Toast.makeText(this,
                            getResources().getString(R.string.exerciseAlreadyExists),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            DatabaseManager.createNewExercise(newExerciseName);
            Toast.makeText(this,
                    getResources().getString(R.string.newExerciseCreated),
                    Toast.LENGTH_SHORT).show();
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    /*##############################################################################################
    #############################################TIMER##############################################
    ##############################################################################################*/
    private void activateTimer() {
        Log.d("TIMER", "MainService activateTimer");
        DatabaseManager.setCurrentWorkoutTimerIsActive(true);

        if (getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT") != null) {
            TimerBar timer = getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT")
                    .getView().findViewById(R.id.timer);
            timer.setVisibility(View.VISIBLE);
        }


        activateTimerMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_baseline_timer_off_24));
        activateTimerMenuItem.setTitle(R.string.deactivateTimer);
        activateTimerMenuItem.getIcon().setTint(getColor(R.color.white));
        startTimerMenuItem.setVisible(true);
        if (System.currentTimeMillis() < DatabaseManager.getCurrentWorkoutTimerEnd())
        {
            startTimerMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_baseline_timer_10_24));
            startTimerMenuItem.setTitle(R.string.add10Timer);
            startTimerMenuItem.getIcon().setTint(getColor(R.color.white));
        }
    }

    private void deactivateTimer() {
        Log.d("TIMER", "MainService deactivateTimer");
        DatabaseManager.setCurrentWorkoutTimerIsActive(false);
        DatabaseManager.setCurrentWorkoutTimerStart(-1);
        DatabaseManager.setCurrentWorkoutTimerEnd(-1);
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }

        activateTimerMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_baseline_timer_24));
        activateTimerMenuItem.setTitle(R.string.activateTimer);
        activateTimerMenuItem.getIcon().setTint(getColor(R.color.white));
        startTimerMenuItem.setVisible(false);
        startTimerMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
        startTimerMenuItem.setTitle(R.string.startTimer);
        startTimerMenuItem.getIcon().setTint(getColor(R.color.white));

        TimerBar timer = getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT")
                .getView().findViewById(R.id.timer);
        timer.setVisibility(View.GONE);
        timer.setProgress(1.0f);

        if (DatabaseManager.doesTableExist("CurrentWorkout")) {
            Toast.makeText(this,
                    getResources().getString(R.string.timerDeactivated),
                    Toast.LENGTH_SHORT).show();
        }
        stopService(new Intent(this, TimerNotificationService.class));


        Handler h = new Handler(Looper.getMainLooper());
        Runnable myRunnable = () -> updateNotification(getString(R.string.notificationText));
        h.postDelayed(myRunnable, 10);
    }

    public void startTimer() {
        Log.d("TIMER", "MainService startTimer");
        if (DatabaseManager.getCurrentWorkoutTimerStart() != -1) {
            return;
        }

        startTimerMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_baseline_timer_10_24));
        startTimerMenuItem.setTitle(R.string.add10Timer);
        startTimerMenuItem.getIcon().setTint(getColor(R.color.white));

        TimerBar timer = getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT")
                .getView().findViewById(R.id.timer);
        timer.setVisibility(View.VISIBLE);

        Toast.makeText(this,
                getResources().getString(R.string.timerStarted),
                Toast.LENGTH_SHORT).show();
        long start = System.currentTimeMillis();
        long end = start + DatabaseManager.getSettings().timerDuration * 1000L;
        DatabaseManager.setCurrentWorkoutTimerStart(start);
        DatabaseManager.setCurrentWorkoutTimerEnd(end);
        handleTimer();
        startService(new Intent(this, TimerNotificationService.class));
    }

    private void addToTimer() {
        Log.d("TIMER", "MainService addToTimer");
        DatabaseManager.setCurrentWorkoutTimerEnd((DatabaseManager.getCurrentWorkoutTimerEnd() + 10000L));
        Toast.makeText(this,
                getResources().getString(R.string.added10SecondsToTimer),
                Toast.LENGTH_SHORT).show();
    }

    private void handleTimer() {
        if (!DatabaseManager.doesTableExist("CurrentWorkout") ||
                !DatabaseManager.getCurrentWorkoutTimerIsActive())
            return;

        invalidateOptionsMenu();

        Settings settings = DatabaseManager.getSettings();
        int durationInSeconds = settings.timerDuration;
        long endTime = DatabaseManager.getCurrentWorkoutTimerEnd();


        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < endTime) {
                    float progress = (float)(endTime - System.currentTimeMillis()) / (durationInSeconds * 1000L);

                    if (getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT") != null) {
                        TimerBar timer = getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT")
                                .getView().findViewById(R.id.timer);
                        timer.setProgress(progress);
                        timer.setVisibility(View.VISIBLE);
                    }

                    handler.postDelayed(this, 10);
                } else {
                   timerExpired();
                   handler.removeCallbacks(this);
                }
            }
        };
        handler.post(runnable);
    }



    private void timerExpired() {
        Log.d("TIMER", "Main timerExpired");

        if (getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT") != null) {
            TimerBar timer = getSupportFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT")
                    .getView().findViewById(R.id.timer);
            timer.setProgress(0.0f);
        }
        DatabaseManager.setCurrentWorkoutTimerStart(-1);
        DatabaseManager.setCurrentWorkoutTimerEnd(-1);
        if (startTimerMenuItem != null) {
            startTimerMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
            startTimerMenuItem.setTitle(R.string.startTimer);
            startTimerMenuItem.getIcon().setTint(getColor(R.color.white));
        }
    }

    private void openSettings() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 4);
    }

    /*##############################################################################################
    #########################################NOTIFICATIONS##########################################
    ##############################################################################################*/
    public void startNotification(){
        NotificationChannel channel =
                new NotificationChannel("69", "General", NotificationManager.IMPORTANCE_LOW);
        channel.enableVibration(false);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "69")
                        .setSmallIcon(R.drawable.ic_fitness_center_24)
                        .setContentTitle(getResources().getString(R.string.notificationTitle))
                        .setContentText(getResources().getString(R.string.notificationText))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOngoing(true)
                        .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(notificationId, builder.build());
    }

    public void updateNotification(String text) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "69")
                        .setSmallIcon(R.drawable.ic_fitness_center_24)
                        .setContentTitle(getResources().getString(R.string.notificationTitle))
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(true)
                        .setContentIntent(contentIntent)
                        .setVibrate(new long[]{0L});;

        notificationManagerCompat.notify(notificationId, builder.build());
    }

    public void deleteNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);
    }
}