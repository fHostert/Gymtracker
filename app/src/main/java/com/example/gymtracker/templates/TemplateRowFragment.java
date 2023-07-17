package com.example.gymtracker.templates;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.R;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.workout.WorkoutFragment;

public class TemplateRowFragment extends Fragment {

    private static final String ARG_TEXT = "param1";

    private String text;

    public TemplateRowFragment() {
        // Required empty public constructor
    }

    public static TemplateRowFragment newInstance(String text) {
        TemplateRowFragment fragment = new TemplateRowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_template_row, container, false);

        TextView textView =  view.findViewById(R.id.text_view_template_row);
        textView.setText(text);

        //Initialize buttons
        ImageButton deleteExerciseButton = view.findViewById(R.id.delete_exercise_from_template_button);
        deleteExerciseButton.setOnClickListener(view1 -> delete());

        ImageButton moveExerciseUpButton = view.findViewById(R.id.exercise_up_template_button);
        moveExerciseUpButton.setOnClickListener(view1 -> moveExerciseUp());

        ImageButton moveExerciseDownButton = view.findViewById(R.id.exercise_down_template_button);
        moveExerciseDownButton.setOnClickListener(view1 -> moveExerciseDown());

        return view;
    }

    private void moveExerciseUp() {
        ((EditTemplateActivity) getActivity()).moveExerciseUp(text);
    }

    private void moveExerciseDown() {
        ((EditTemplateActivity) getActivity()).moveExerciseDown(text);
    }

    public void setText(String text) {
        TextView textView =  getView().findViewById(R.id.text_view_template_row);
        textView.setText(text);
    }

    public void delete() {
        this.delete();
    }


    public String getText() {
        return text;    }
}