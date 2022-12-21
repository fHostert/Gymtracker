package com.example.gymtracker.workout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;

import java.util.ArrayList;

public class ExerciseFragment extends Fragment {

    private static final String ARG_EXERCISE = "param1";

    private Exercise exercise;
    private final ArrayList<SetFragment> setFragments = new ArrayList<>();

    public ExerciseFragment() {
        // Required empty public constructor
    }

    public static ExerciseFragment newInstance(Exercise exercise) {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable(ARG_EXERCISE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        ((TextView) view.findViewById(R.id.name_of_exercise_text_view)).
                setText(exercise.getName());

        //add Sets
        for (Set set : exercise.getSets()) {
            LinearLayout exerciseLinearLayout = view.findViewById(R.id.exercise_table_layout);
            SetFragment setFragment = SetFragment.newInstance(set, exercise.getDatabaseIndex());
            setFragments.add(setFragment);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), setFragment).commit();
            exerciseLinearLayout.addView(newContainer);
        }

        return view;
    }

    public void addSet() {
        LinearLayout exerciseLinearLayout = getView().findViewById(R.id.exercise_table_layout);
        Set set = new Set(exerciseLinearLayout.getChildCount() - 1, 0, 0);
        SetFragment setFragment = SetFragment.newInstance(set, exercise.getDatabaseIndex());
        exercise.addSet(set);
        setFragments.add(setFragment);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), setFragment).commit();
        exerciseLinearLayout.addView(newContainer);

        DatabaseManager.insertSetIntoCurrentWorkout(exercise.getDatabaseIndex(), set);
    }

    public void replace(String newExerciseName) {
        ((TextView) getView().findViewById(R.id.name_of_exercise_text_view)).
                setText(newExerciseName);
        exercise = new Exercise(DatabaseManager.getExerciseID(newExerciseName));

        LinearLayout exerciseLinearLayout = getView().findViewById(R.id.exercise_table_layout);
        exerciseLinearLayout.removeAllViews();
        setFragments.clear();

        //add empty Sets
        for (Set set : exercise.getSets()) {
            SetFragment setFragment = SetFragment.newInstance(set, exercise.getDatabaseIndex());
            setFragments.add(setFragment);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), setFragment).commit();
            exerciseLinearLayout.addView(newContainer);
        }
    }

    public void deleteLastSet() {
        exercise.deleteLastSet();
        setFragments.remove(setFragments.size() - 1);
        LinearLayout exerciseLinearLayout = getView().findViewById(R.id.exercise_table_layout);
        exerciseLinearLayout.removeViewAt(exerciseLinearLayout.getChildCount() - 1);
        DatabaseManager.removeLastSet(exercise.getDatabaseIndex(), exercise.getSets().size() + 1);

    }

    public int getDatabaseIndex() {
        return exercise.getDatabaseIndex();
    }

    public String getName() {
        return exercise.getName();
    }

    public SetFragment getSetFragment(int setIndex) {
        return setFragments.get(setIndex - 1);
    }

    public int getSetCount() {
        return exercise.getSets().size();
    }



}