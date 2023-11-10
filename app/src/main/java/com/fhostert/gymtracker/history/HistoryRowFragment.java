package com.fhostert.gymtracker.history;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fhostert.gymtracker.R;

public class HistoryRowFragment extends Fragment {

    private static final String ARG_EXERCISE = "param1";
    private static final String ARG_BEST_SET = "param2";

    private String exercise;
    private String bestSet;

    public HistoryRowFragment() {
        // Required empty public constructor
    }

    public static HistoryRowFragment newInstance(String exercise, String bestSet) {
        HistoryRowFragment fragment = new HistoryRowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE, exercise);
        args.putString(ARG_BEST_SET, bestSet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = getArguments().getString(ARG_EXERCISE);
            bestSet = getArguments().getString(ARG_BEST_SET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_row, container, false);

        ((TextView) view.findViewById(R.id.history_exercise_text_view)).setText(exercise);
        ((TextView) view.findViewById(R.id.history_best_set_text_view)).setText(bestSet);

        return view;
    }
}