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
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.history.HistoryFragment;
import com.example.gymtracker.workout.ExerciseFragment;
import com.example.gymtracker.workout.SetFragment;
import com.example.gymtracker.workout.WorkoutFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    WorkoutFragment globalWorkoutFragment;
    private final int notificationId = 69;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup database
        SQLiteDatabase db = openOrCreateDatabase("GymtrackerNew", MODE_PRIVATE,null);
        DatabaseManager.initialize(db);
        DatabaseManager.createExercisesTable(getResources().getStringArray(R.array.exercises));
        DatabaseManager.createHistoryTable();
        DatabaseManager.createWorkoutsTable();
        //DatabaseManager.dropTable("CurrentWorkout");
        //DatabaseManager.dropTable("CurrentWorkoutMetadata");
        //DatabaseManager.dropTable("History");
        //DatabaseManager.dropTable("Workouts");

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
                this.setTitle(R.string.app_name);
            }
            else if (item.getItemId() == R.id.navigation_history) {
                historyContainer.setVisibility(View.VISIBLE);
                homeContainer.setVisibility(View.INVISIBLE);
                statsContainer.setVisibility(View.INVISIBLE);
                this.setTitle(R.string.history);

                ((HistoryFragment) getSupportFragmentManager().
                        findFragmentByTag("history_fragment")).initialize();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DatabaseManager.doesTableExist("CurrentWorkout")){
            getMenuInflater().inflate(R.menu.workout_menu, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.home_menu, menu);
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
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //delete Exercise
        if (resultCode == RESULT_OK && requestCode == 2) {
            deleteExercise(data.getExtras().getString("ITEM"));
        }
        //rename Exercise
        else if (resultCode == RESULT_OK && requestCode == 3) {
            renameExercise(data.getExtras().getString("ITEM"));
        }
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

        startWorkout();
    }

    public void startWorkout() {
        this.setTitle(DatabaseManager.getCurrentWorkoutName());
        startOngoingNotification();
        invalidateOptionsMenu();
    }

    public void deleteExercise() {
        final Intent intent = new Intent(this, ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("TITLE", getResources().getString(R.string.deleteExercise));
        startActivityForResult(intent, 2);
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
        startActivityForResult(intent, 3);
    }

    public void renameExercise(String exerciseName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.changeExerciseName));
        alert.setTitle(getResources().getString(R.string.changeExerciseNameText));
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
                });
                //If cancel, return
                newAlert.setNegativeButton(getResources().getString(R.string.no), (dialog, whichButton) -> {
                });

                newAlert.show();
            }
            //Just rename the exercise
            else {
                DatabaseManager.renameExercise(exerciseName, newExerciseName);
            }

        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();

        for (String exercise : DatabaseManager.getExercises()) {
            if (Objects.equals(exercise, exerciseName)) {

                return;
            }
        }
    }

    public void createNewTemplate() {

    }

    public void templateMenuClick(View view) {
    }

    /*##############################################################################################
    #########################################WORKOUT BUTTONS########################################
    ##############################################################################################*/
    public void addExerciseClick(View view) {
        globalWorkoutFragment.addExerciseClick();
    }

    public void quitWorkout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.quitWorkoutText));
        alert.setTitle(getResources().getString(R.string.quitWorkout));

        //If ok, continue
        alert.setPositiveButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
            DatabaseManager.dropTable("CurrentWorkout");
            DatabaseManager.dropTable("CurrentWorkoutMetadata");
            stopWorkout();
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
        stopOngoingNotification();
        invalidateOptionsMenu();
        this.setTitle(getResources().getString(R.string.app_name));
    }

    public void addSetClick(View view) {
        ExerciseFragment exerciseFragment = getExerciseFragment(view);
        exerciseFragment.addSet();
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
                if (Objects.equals(exercise, newExerciseName)) {
                    Toast.makeText(this,
                            getResources().getString(R.string.exerciseAlreadyExists),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            DatabaseManager.createNewExercise(newExerciseName);
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    /*##############################################################################################
    #########################################EXERCISE BUTTONS#######################################
    ##############################################################################################*/
    public void exerciseMenuClick(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        ExerciseFragment exerciseFragment = getExerciseFragment(view);
        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.move_exercise_up_menu) {
                globalWorkoutFragment.moveExerciseUp(exerciseFragment);
            }
            else if (id == R.id.move_exercise_down_menu) {
                globalWorkoutFragment.moveExerciseDown(exerciseFragment);
            }
            else if (id == R.id.remove_exercise_menu) {
                globalWorkoutFragment.removeExercise(exerciseFragment);
            }
            else if (id == R.id.replace_exercise_menu) {
                globalWorkoutFragment.replaceExercise(exerciseFragment);
            }
            else if (id == R.id.delete_last_set_menu) {
                globalWorkoutFragment.deleteLastSet(exerciseFragment);
            }
            return false;
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