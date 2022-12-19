package com.example.gymtracker;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.datastructures.Workout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    WorkoutFragment globalWorkoutFragment;
    private final int notificationId = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup database
        SQLiteDatabase db = openOrCreateDatabase("GymtrackerNew", MODE_PRIVATE,null);
        DatabaseManager.initialize(db);
        DatabaseManager.createExercisesTable(getResources().getStringArray(R.array.exercises));
        //DatabaseManager.dropTable("CurrentWorkout");
        //DatabaseManager.dropTable("CurrentWorkoutMetadata");

        //Setup Bottom Navigation View and Fragments containing each tab
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
        FragmentContainerView statistikContainer = findViewById(R.id.stats_container);


        historyContainer.setVisibility(View.INVISIBLE);
        statistikContainer.setVisibility(View.INVISIBLE);
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                homeContainer.setVisibility(View.VISIBLE);
                historyContainer.setVisibility(View.INVISIBLE);
                statistikContainer.setVisibility(View.INVISIBLE);
                this.setTitle(R.string.app_name);
            }
            else if (item.getItemId() == R.id.navigation_verlauf) {
                historyContainer.setVisibility(View.VISIBLE);
                homeContainer.setVisibility(View.INVISIBLE);
                statistikContainer.setVisibility(View.INVISIBLE);
                this.setTitle(R.string.history);
            }
            else if (item.getItemId() == R.id.navigation_statistik) {
                statistikContainer.setVisibility(View.VISIBLE);
                homeContainer.setVisibility(View.INVISIBLE);
                historyContainer.setVisibility(View.INVISIBLE);
                this.setTitle(R.string.stats);
            }
            return true;
        });

        //restore the last workout if it did not exit properly
        restoreWorkout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.change_workout_name) {
            changeWorkoutName();
        }
        return true;
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
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    public void reload() {
        Fragment newHome = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_container, newHome).commit();
    }

    private void restoreWorkout() {
        Workout workout = DatabaseManager.getCurrentWorkout();
        //build new workout
        if (workout != null) {
            WorkoutFragment workoutFragment = WorkoutFragment.newInstance(workout);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_container, workoutFragment).commit();
            getSupportFragmentManager().executePendingTransactions();
            globalWorkoutFragment = workoutFragment;
            this.setTitle(DatabaseManager.getCurrentWorkoutName());
        }
    }

    /*##############################################################################################
    ##########################################HOME BUTTONS##########################################
    ##############################################################################################*/
    public void startEmptyWorkoutClick(View view) {
        globalWorkoutFragment = new WorkoutFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_container, globalWorkoutFragment).commit();

        DatabaseManager.createCurrentWorkoutTable();
        DatabaseManager.createCurrentWorkoutMetadataTable();
        DatabaseManager.setCurrentWorkoutMetadata(
                getResources().getString(R.string.defaultWorkoutName));

        this.setTitle(DatabaseManager.getCurrentWorkoutName());
        startOngoingNotification();
    }


    /*##############################################################################################
    #########################################WORKOUT BUTTONS########################################
    ##############################################################################################*/
    public void addExerciseClick(View view) {
        globalWorkoutFragment.addExerciseClick();
    }

    public void quitWorkoutClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.quitWorkoutText));
        alert.setTitle(getResources().getString(R.string.quitWorkout));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            DatabaseManager.dropTable("CurrentWorkout");
            DatabaseManager.dropTable("CurrentWorkoutMetadata");
            reload();
            stopOngoingNotification();
        });
        //If cancel, return
        alert.setNegativeButton(getResources().getString(R.string.no), (dialog, whichButton) -> {
        });

        alert.show();
    }

    public void saveWorkoutClick(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.saveWorkoutText));
        alert.setTitle(getResources().getString(R.string.saveWorkout));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            DatabaseManager.createHistoryTable();
            if (DatabaseManager.saveCurrentWorkout()) {
                DatabaseManager.printTable("History");
                reload();
                stopOngoingNotification();
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

    public void addSetClick(View view) {
        ExerciseFragment exerciseFragment = getExerciseFragment(view);
        exerciseFragment.addSet();
    }

    /*##############################################################################################
    #########################################EXERCISE BUTTONS#######################################
    ##############################################################################################*/
    public void exerciseMenuClick(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        ExerciseFragment exerciseFragment = getExerciseFragment(view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.move_exercise_up) {
                    globalWorkoutFragment.moveExerciseUp(exerciseFragment);
                }
                else if (id == R.id.move_exercise_down) {
                    globalWorkoutFragment.moveExerciseDown(exerciseFragment);
                }
                else if (id == R.id.remove_exercise) {
                    globalWorkoutFragment.removeExercise(exerciseFragment);
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.exercise_menu, popup.getMenu());
        popup.show();
    }

    /*##############################################################################################
    ###########################################SET BUTTONS##########################################
    ##############################################################################################*/
    public void saveSetClick(View view) {
        getSetFragment(view).saveSet();
    }

    /*##############################################################################################
    #########################################NOTIFICATIONS##########################################
    ##############################################################################################*/
    public void startOngoingNotification(){
        CharSequence name = "General";
        String description = "Ongoing Workout Notification";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("69", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "69")
                        .setSmallIcon(R.drawable.ic_fitness_center_24)
                        .setContentTitle(getResources().getString(R.string.notificationTitle))
                        .setContentText(getResources().getString(R.string.notificationText))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setOngoing(true)
                        .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        notificationManagerCompat.notify(notificationId, builder.build());
    }

    public void stopOngoingNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);
    }

    /*##############################################################################################
    ############################################HELPER##############################################
    ##############################################################################################*/
    private ExerciseFragment getExerciseFragment(View view) {
        View parentView = ((View) view.getParent());
        Log.d("CLASSNAMEEXERCSIE", parentView.getClass().getSimpleName());
        String exerciseName = String.valueOf((
                (TextView)parentView.findViewById(R.id.name_of_exercise_text_view)).getText());
        ArrayList<ExerciseFragment> exercises = globalWorkoutFragment.getExerciseFragments();
        for (ExerciseFragment exerciseFragment : exercises) {
            if (Objects.equals(exerciseFragment.getName(), exerciseName)) {
                return exerciseFragment;
            }
        }
        return null;
    }

    private SetFragment getSetFragment(View view) {
        View parentView = ((View) view.getParent().getParent().getParent());
        TextView setIndexTV = ((View) view.getParent()).findViewById(R.id.set_index_text_view);
        int setIndex = Integer.parseInt(String.valueOf(setIndexTV.getText()));
        ExerciseFragment exerciseFragment = getExerciseFragment(parentView);
        return exerciseFragment.getSetFragment(setIndex);
    }


}