package com.example.gymtracker;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Workout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkoutFragment extends Fragment {

    private static final String ARG_WORKOUT = "param1";

    private Workout workout = null;

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
        if (getArguments() != null) {
            workout = (Workout) getArguments().getSerializable(ARG_WORKOUT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        if (workout == null || workout.getExercises() == null) {
            return view;
        }

        LinearLayout workoutLinearLayout = view.findViewById(R.id.workout_linear_layout);
        for (Exercise exercise : workout.getExercises()) {
            ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), exerciseFragment).commit();
            workoutLinearLayout.addView(newContainer);

            /*for (Set set : exercise.getSets()) {
                DatabaseManager.insertSetIntoCurrentWorkout(
                        exercise.getDatabaseIndex(), workoutLinearLayout.getChildCount(), set);
            }*/
        }
        return view;
    }

    public void addExerciseClick() {
        final Intent intent = new Intent(getContext(), ChooseExerciseActivity.class);
        startActivityForResult(intent, 0);
    }

    private void addExercise(Exercise exercise) {
        LinearLayout workoutLinearLayout = getView().findViewById(R.id.workout_linear_layout);
        ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), exerciseFragment).commit();
        workoutLinearLayout.addView(newContainer);

       for (Set set : exercise.getSets()) {
           DatabaseManager.insertSetIntoCurrentWorkout(
                   exercise.getDatabaseIndex(), workoutLinearLayout.getChildCount(), set);
       }
    }

    private void addEmptyExercise(String exerciseName) {
        LinearLayout workoutLinearLayout = getView().findViewById(R.id.workout_linear_layout);
        Exercise exercise = new Exercise(DatabaseManager.getExerciseID(exerciseName));
        ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), exerciseFragment).commit();
        workoutLinearLayout.addView(newContainer);

        for (Set set : exercise.getSets()) {
            DatabaseManager.insertSetIntoCurrentWorkout(
                    exercise.getDatabaseIndex(), workoutLinearLayout.getChildCount() - 4, set);
        }
        DatabaseManager.printTable("CurrentWorkout");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //add Exercise
        if (resultCode == RESULT_OK && requestCode == 0) {
          addEmptyExercise(data.getExtras().getString("EXERCISE_NAME_KEY"));
        }

    }



}