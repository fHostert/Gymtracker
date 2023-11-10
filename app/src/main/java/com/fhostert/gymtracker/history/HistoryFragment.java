package com.fhostert.gymtracker.history;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fhostert.gymtracker.R;
import com.fhostert.gymtracker.datastructures.History;
import com.fhostert.gymtracker.datastructures.Workout;
import com.fhostert.gymtracker.helper.DatabaseManager;

public class HistoryFragment extends Fragment {

    //TODO refactor initialize and update together

    private boolean isInitialized = false;
    private final int howManyWorkoutsToLoad = 10;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance() {
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

        //load first 10 entries
        initialize(view);

        //initialize buttons
        Button loadMoreHistoryButton = view.findViewById(R.id.load_more_history_button);
        loadMoreHistoryButton.setOnClickListener(view1 -> loadMoreHistory());

        return view;
    }

    private void initialize(View view){
        if (isInitialized) {
            return;
        }

        //get data
        History history = DatabaseManager.getHistory(howManyWorkoutsToLoad, 0);

        //reset view
        LinearLayout historyLinearLayout = view.findViewById(R.id.history_linear_layout);
        historyLinearLayout.removeAllViews();

        //add workouts
        for (Workout workout : history.getWorkouts()) {
            HistoryWorkoutFragment historyWorkoutFragment =
                    HistoryWorkoutFragment.newInstance(workout);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());

            //add on click listener for the detail view to each workout
            newContainer.setOnClickListener(view1 -> {
                final Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
                intent.putExtra("WORKOUT", workout);
                startActivity(intent);
            });

            //add with unique tag
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyWorkoutFragment,
                    "HISTORY_WORKOUT" + workout.getID()).commit();
            historyLinearLayout.addView(newContainer);
        }
        isInitialized = true;

        //handle load more button visibility
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
            HistoryWorkoutFragment historyWorkoutFragment =
                    HistoryWorkoutFragment.newInstance(workout);
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

    /**
     * Click on load more Button loads 10 more entries
     */
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
            HistoryWorkoutFragment historyWorkoutFragment =
                    HistoryWorkoutFragment.newInstance(workout);
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