package com.example.gymtracker.history;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.example.gymtracker.R;
import com.example.gymtracker.TextViewTableRowFragment;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.History;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.templates.AddTemplateActivity;

public class HistoryFragment extends Fragment {

    private boolean isInitialized = false;
    private final int howManyWorkoutsToLoad = 10;

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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        initialize(view);
        Button loadMoreHistoryButton = view.findViewById(R.id.load_more_history_button);
        loadMoreHistoryButton.setOnClickListener(view1 -> loadMoreHistory());

        return view;
    }

    public void initialize(View view){
        if (isInitialized) {
            return;
        }
        History history = DatabaseManager.getHistory(howManyWorkoutsToLoad, 0);
        LinearLayout historyLinearLayout = view.findViewById(R.id.history_linear_layout);
        for (Workout workout : history.getWorkouts()) {
            HistoryWorkoutFragment historyWorkoutFragment = HistoryWorkoutFragment.newInstance(workout);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());

            newContainer.setOnClickListener(view1 -> {
                final Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
                intent.putExtra("WORKOUT", workout);
                startActivity(intent);
            });

            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyWorkoutFragment,
                    "HISTORY_WORKOUT" + workout.getID()).commit();
            historyLinearLayout.addView(newContainer);
        }
        isInitialized = true;

        if (historyLinearLayout.getChildCount() > 9) {
            view.findViewById(R.id.load_more_history_button).setVisibility(View.VISIBLE);
        }
    }

    private void initialize() {
        initialize(getView());
    }

    public void reload() {
        isInitialized = false;
        initialize();
    }

    public void update(){
        if (!isInitialized) {
            return;
        }
        //only add the most recent workout
        History history = DatabaseManager.getHistory(1, 0);
        LinearLayout historyLinearLayout = getView().findViewById(R.id.history_linear_layout);
        for (Workout workout : history.getWorkouts()) {
            HistoryWorkoutFragment historyWorkoutFragment = HistoryWorkoutFragment.newInstance(workout);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());

            newContainer.setOnClickListener(view1 -> {
                final Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
                intent.putExtra("WORKOUT", workout);
                startActivity(intent);
            });
            
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyWorkoutFragment,
                    "HISTORY_WORKOUT" + workout.getID()).commit();
            historyLinearLayout.addView(newContainer, 0);
        }
    }

    private void loadMoreHistory() {
        LinearLayout historyLinearLayout = getView().findViewById(R.id.history_linear_layout);
        int alreadyLoaded = historyLinearLayout.getChildCount();
        History history = DatabaseManager.getHistory(howManyWorkoutsToLoad,
                alreadyLoaded);

        if (history.getWorkouts().size() == 0) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.everythingLoaded),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        for (Workout workout : history.getWorkouts()) {
            HistoryWorkoutFragment historyWorkoutFragment = HistoryWorkoutFragment.newInstance(workout);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());

            newContainer.setOnClickListener(view1 -> {
                final Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
                intent.putExtra("WORKOUT", workout);
                startActivity(intent);
            });

            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyWorkoutFragment,
                            "HISTORY_WORKOUT" + workout.getID()).commit();
            historyLinearLayout.addView(newContainer);
        }
    }
}