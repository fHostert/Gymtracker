package com.example.gymtracker.history;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;

import java.util.Locale;

public class HistoryWorkoutFragment extends Fragment {

    private static final Locale l = Locale.GERMAN;

    private static final String ARG_WORKOUT = "param1";

    private Workout workout;

    public HistoryWorkoutFragment() {
        // Required empty public constructor
    }

    public static HistoryWorkoutFragment newInstance(Workout workout) {
        HistoryWorkoutFragment fragment = new HistoryWorkoutFragment();
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
        View view = inflater.inflate(R.layout.fragment_history_workout, container, false);

        TableLayout historyTableLayout = view.findViewById(R.id.workout_history_table_layout);
        for (Exercise exercise : workout.getExercises()) {
            String exercisePara = String.format(
                    l, "%d × %s", exercise.getSets().size(), exercise.getName());
            String bestSetPara = exercise.getBestSetString();
            HistoryRowFragment historyRowFragment = HistoryRowFragment.newInstance(exercisePara, bestSetPara);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyRowFragment).commit();
            historyTableLayout.addView(newContainer);
        }
        ((TextView) view.findViewById(R.id.workout_name_text_view)).
                setText(workout.getName());
        ((TextView) view.findViewById(R.id.workout_date_text_view)).
                setText(Formatter.formatDate(workout.getDate()));
        ((TextView) view.findViewById(R.id.duration_text_view)).
                setText(String.format("\uD83D\uDD50 %s", Formatter.formatTime(workout.getDuration())));
        ((TextView) view.findViewById(R.id.sum_weight_text_view)).
                setText(String.format("💪 %s kg", Formatter.formatFloat(workout.getTotalWeight())));
        ((TextView) view.findViewById(R.id.number_of_prs_text_view)).
                setText(String.format(l, "\uD83C\uDFC6 %d PRs", workout.getNumberOfPRs()));

        return view;
    }
}