package com.example.gymtracker.workout;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.TimerBar;

import java.util.Arrays;

public class WorkoutFragment extends Fragment {

    private static final String ARG_WORKOUT = "param1";

    private Workout workout;



    public WorkoutFragment() {
        // Required empty public constructor
    }

    public static WorkoutFragment newInstance(Workout workout) {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WORKOUT, workout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Workout restored or template loaded
        if (getArguments() != null) {
            workout = (Workout) getArguments().getSerializable(ARG_WORKOUT);
        }
        //Empty workout started
        else {
            workout = new Workout(getResources().getString(R.string.defaultWorkoutName));
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        //Add exercises but don't add exercises to database, they are already there
        for (Exercise exercise : workout.getExercises()) {
            addExercise(exercise, view, false);
        }

        //Initialize buttons
        Button addExerciseButton = view.findViewById(R.id.add_exercise_button);
        addExerciseButton.setOnClickListener(view1 -> addExerciseClick());



        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Add Exercise
        if (resultCode == RESULT_OK && requestCode == 0) {
            addEmptyExercise(data.getExtras().getString("ITEM"));
        }
    }

    private void addExerciseClick() {
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        String[] exercisesInWorkout = DatabaseManager.getExercisesInCurrentWorkout();
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("REMOVE_LIST", exercisesInWorkout);
        intent.putExtra("TITLE", getResources().getString(R.string.addExercise));
        startActivityForResult(intent, 0);
    }

    private void addExercise(Exercise exercise, View view, boolean addToCurrentWorkoutTable) {;
        LinearLayout exerciseContainer = view.findViewById(R.id.exercise_container);

        //The position in the layout of the new exercise
        int positionInLayout = exerciseContainer.getChildCount();

        ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise, positionInLayout);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());

        //Give the exerciseFragment an unique tag and add it
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), exerciseFragment, "EXERCISE" + exercise.getName())
                .commit();
        exerciseContainer.addView(newContainer);

        if (addToCurrentWorkoutTable) {
            for (Set set : exercise.getSets()) {
                DatabaseManager.insertSetIntoCurrentWorkout(
                        exercise.getExerciseID(), positionInLayout, set);
            }
            workout.addExercise(exercise.getName());
        }
    }

    private void addEmptyExercise(String exerciseName) {
        Exercise exercise = new Exercise(DatabaseManager.getExerciseID(exerciseName));
        addExercise(exercise, getView(), true);
    }

    public void replaceExercise(String newExercise, int positionInWorkout) {
        LinearLayout exerciseContainer = getView().findViewById(R.id.exercise_container);
        exerciseContainer.removeViewAt(positionInWorkout);

        Exercise exercise = new Exercise(DatabaseManager.getExerciseID(newExercise));

        ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise, positionInWorkout);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());

        //Give the exerciseFragment an unique tag and add it
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), exerciseFragment, "EXERCISE" + exercise.getName())
                .commit();
        exerciseContainer.addView(newContainer, positionInWorkout);
    }

    public void refreshExercisePositions(){
        String[] exercises = DatabaseManager.getExercisesInCurrentWorkout();
        int counter = 0;
        for (String e : exercises){
            ((ExerciseFragment) getParentFragmentManager().findFragmentByTag("EXERCISE" + e))
                    .setPositionInWorkout(counter);
            counter++;
        }
    }

    public LinearLayout getExerciseLayout() {
        return getView().findViewById(R.id.exercise_container);
    }
}