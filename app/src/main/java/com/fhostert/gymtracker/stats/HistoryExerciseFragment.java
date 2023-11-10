package com.fhostert.gymtracker.stats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fhostert.gymtracker.R;
import com.fhostert.gymtracker.charts.datastructures.ExerciseEntry;
import com.fhostert.gymtracker.datastructures.Set;
import com.fhostert.gymtracker.helper.Formatter;

import java.util.Locale;
import java.util.Objects;


public class HistoryExerciseFragment extends Fragment {

    private static final String ARG_EXERCISE_ENTRY = "param1";

    private ExerciseEntry exerciseEntry;
    private final Locale l = Locale.GERMAN;

    public HistoryExerciseFragment() {
        // Required empty public constructor
    }

    public static HistoryExerciseFragment newInstance(ExerciseEntry exerciseEntry) {
        HistoryExerciseFragment fragment = new HistoryExerciseFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE_ENTRY, exerciseEntry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exerciseEntry = (ExerciseEntry) getArguments().getSerializable(ARG_EXERCISE_ENTRY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_exercise, container, false);

        //set title
        TextView head = view.findViewById(R.id.exercise_history_header);
        head.setText(String.format("%s - %s %s kg",
                Formatter.formatDate(exerciseEntry.getDate()), getResources().getString(R.string.totalWeight),
                Formatter.formatFloat(exerciseEntry.getVolume())));

        LinearLayout setContainer = view.findViewById(R.id.exercise_history_container);

        //add note
        if (!Objects.equals(exerciseEntry.getNote(), "")) {
            TextView newSet = new TextView(getContext());
            String note = getString(R.string.note) + " " + exerciseEntry.getNote();
            newSet.setText(note);
            setContainer.addView(newSet);
        }

        //add sets
        for (Set set : exerciseEntry.getSets()) {
            TextView newSet = new TextView(getContext());
            newSet.setText(String.format(l, "%d: %s kg × %d %s", set.getIndex(),
                    Formatter.formatFloat(set.getWeight()), set.getReps(),
                    getResources().getString(R.string.repetitionsFull)));
            setContainer.addView(newSet);
        }


        return view;
    }
}