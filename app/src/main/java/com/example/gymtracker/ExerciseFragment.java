package com.example.gymtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;

public class ExerciseFragment extends Fragment {

    private static final String ARG_EXERCISE = "param1";

    private Exercise exercise;

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
            SetFragment setFragment = SetFragment.newInstance(set);
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
        SetFragment setFragment = SetFragment.newInstance(set);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), setFragment).commit();
        exerciseLinearLayout.addView(newContainer);

        DatabaseManager.insertSetIntoCurrentWorkout(exercise.getDatabaseIndex(), set);
    }


}