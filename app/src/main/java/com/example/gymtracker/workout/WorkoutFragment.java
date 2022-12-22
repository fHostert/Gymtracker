package com.example.gymtracker.workout;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Workout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkoutFragment extends Fragment {

    private static final String ARG_WORKOUT = "param1";

    private Workout workout = null;
    private final ArrayList<ExerciseFragment> exerciseFragments = new ArrayList<>();
    private ExerciseFragment exerciseFragmentToBeReplaced = null;

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
            exerciseFragments.add(exerciseFragment);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), exerciseFragment).commit();
            workoutLinearLayout.addView(newContainer);
        }
        return view;
    }

    public void addExerciseClick() {
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("TITLE", getResources().getString(R.string.addExercise));
        startActivityForResult(intent, 0);
    }

    private void addEmptyExercise(String exerciseName) {
        for (ExerciseFragment e : exerciseFragments) {
            if (Objects.equals(e.getName(), exerciseName)) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.exerciseAlreadyInWorkout),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        LinearLayout workoutLinearLayout = getView().findViewById(R.id.workout_linear_layout);
        Exercise exercise = new Exercise(DatabaseManager.getExerciseID(exerciseName));
        ExerciseFragment exerciseFragment = ExerciseFragment.newInstance(exercise);
        exerciseFragments.add(exerciseFragment);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //add Exercise
        if (resultCode == RESULT_OK && requestCode == 0) {
          addEmptyExercise(data.getExtras().getString("ITEM"));
        }
        //replace Exercise
        else if (resultCode == RESULT_OK && requestCode == 1) {
            replaceExercise(data.getExtras().getString("ITEM"));
        }
    }

    public void moveExerciseUp(ExerciseFragment exerciseFragment) {
        LinearLayout workoutLinearLayout = getView().findViewById(R.id.workout_linear_layout);
        int indexInArray = 0;
        for (int i = 0; i < exerciseFragments.size(); i++) {
            if (exerciseFragments.get(i) == exerciseFragment) {
                indexInArray = i;
                break;
            }
        }
        if (indexInArray == 0) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastExerciseAlreadyUp),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseManager.moveExerciseUp(exerciseFragment.getDatabaseIndex(), indexInArray + 1);
        View thisExercise = workoutLinearLayout.getChildAt(indexInArray);
        workoutLinearLayout.removeViewAt(indexInArray);
        workoutLinearLayout.addView(thisExercise, indexInArray - 1);
        Collections.swap(exerciseFragments, indexInArray, indexInArray - 1);
    }

    public void moveExerciseDown(ExerciseFragment exerciseFragment) {
        LinearLayout workoutLinearLayout = getView().findViewById(R.id.workout_linear_layout);
        int indexInArray = 0;
        for (int i = 0; i < exerciseFragments.size(); i++) {
            if (exerciseFragments.get(i) == exerciseFragment) {
                indexInArray = i;
                break;
            }
        }
        if (indexInArray == exerciseFragments.size() - 1) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastExerciseAlreadyDown),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseManager.moveExerciseDown(exerciseFragment.getDatabaseIndex(), indexInArray + 1);
        View thisExercise = workoutLinearLayout.getChildAt(indexInArray);
        workoutLinearLayout.removeViewAt(indexInArray);
        workoutLinearLayout.addView(thisExercise, indexInArray + 1);
        Collections.swap(exerciseFragments, indexInArray, indexInArray + 1);
    }

    public void removeExercise(ExerciseFragment exerciseFragment) {
        LinearLayout workoutLinearLayout = getView().findViewById(R.id.workout_linear_layout);
        DatabaseManager.deleteExerciseFromCurrentWorkout(exerciseFragment.getDatabaseIndex());
        for (int i = 0; i < exerciseFragments.size(); i++) {
            if (exerciseFragments.get(i) == exerciseFragment) {
                exerciseFragments.remove(i);
                workoutLinearLayout.removeViewAt(i);
                break;
            }
        }
    }

    /**
     * Called from button click.
     * @param exerciseFragment The exercise that gets replaced.
     */
    public void replaceExercise(ExerciseFragment exerciseFragment) {
        exerciseFragmentToBeReplaced = exerciseFragment;
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("TITLE", getResources().getString(R.string.replaceExerciseThrough));
        startActivityForResult(intent, 1);
    }

    /**
     * Called from OnActivityResult
     * @param exerciseName The name of the new exercise.
     */
    private void replaceExercise(String exerciseName) {
        DatabaseManager.replaceExercise(DatabaseManager.getExerciseID(exerciseName),
                exerciseFragmentToBeReplaced.getDatabaseIndex());
        for (int i = 0; i < exerciseFragments.size(); i++) {
            if (exerciseFragments.get(i) == exerciseFragmentToBeReplaced) {
                exerciseFragments.get(i).replace(exerciseName);
                break;
            }
        }
    }

    public void deleteLastSet(ExerciseFragment exerciseFragment) {
        if (exerciseFragment.getSetCount() < 2) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.cantRemoveLastSet),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        exerciseFragment.deleteLastSet();
    }

    public ArrayList<ExerciseFragment> getExerciseFragments() {
        return exerciseFragments;
    }



}