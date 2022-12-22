package com.example.gymtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.workout.SetFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TemplateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TemplateFragment extends Fragment {

    private static final String ARG_WORKOUT = "param1";

    private Workout workout;

    public TemplateFragment() {
        // Required empty public constructor
    }

    public static TemplateFragment newInstance(Workout workout) {
        TemplateFragment fragment = new TemplateFragment();
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
        View view = inflater.inflate(R.layout.fragment_template, container, false);

        ((TextView) view.findViewById(R.id.name_of_template_text_view)).
                setText(workout.getName());

        TableLayout tableLayout = view.findViewById(R.id.template_exercises_table_layout);
        for (Exercise exercise : workout.getExercises()) {
            TextViewTableRowFragment newLine = TextViewTableRowFragment.
                    newInstance(exercise.getName());
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), newLine).commit();
            tableLayout.addView(newContainer);
        }
        return view;
    }
}