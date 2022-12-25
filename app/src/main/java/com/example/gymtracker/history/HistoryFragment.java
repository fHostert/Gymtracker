package com.example.gymtracker.history;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.History;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    private boolean isInitialized = false;
    private final int howManyWorkoutsToLoad = 1000;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void initialize(){
        if (isInitialized) {
            return;
        }
        History history = DatabaseManager.getHistory(howManyWorkoutsToLoad);
        LinearLayout historyLinearLayout = getView().findViewById(R.id.history_linear_layout);
        for (Workout workout : history.getWorkouts()) {
            HistoryWorkoutFragment historyWorkoutFragment = HistoryWorkoutFragment.newInstance(workout);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyWorkoutFragment,
                    String.valueOf(workout.getID())).commit();
            historyLinearLayout.addView(newContainer);
        }
        isInitialized = true;
    }

    public void reload() {
        isInitialized = false;
        initialize();
    }

    public void update(){
        if (!isInitialized) {
            return;
        }
        History history = DatabaseManager.getHistory(1);
        LinearLayout historyLinearLayout = getView().findViewById(R.id.history_linear_layout);
        for (Workout workout : history.getWorkouts()) {
            HistoryWorkoutFragment historyWorkoutFragment = HistoryWorkoutFragment.newInstance(workout);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyWorkoutFragment,
                    "HISTORYFRAGMENT" + workout.getID()).commit();
            historyLinearLayout.addView(newContainer, 0);
        }
    }
}