package com.example.gymtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gymtracker.templates.TemplateFragment;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.workout.WorkoutFragment;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ArrayList<Workout> templates = DatabaseManager.getTemplates();
        LinearLayout templateContainer = view.findViewById(R.id.template_container_linear_layout);

        for (Workout template : templates) {
            TemplateFragment newTemplate = TemplateFragment.newInstance(template);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());

            //set OnClickListener for each template
            newContainer.setOnClickListener(view1 -> {
                TextView nameTV = view1.findViewById(R.id.name_of_template_text_view);
                String workoutName = (String) nameTV.getText();

                DatabaseManager.loadTemplate(workoutName);

                ((MainActivity)getActivity()).restoreWorkout();
            });

            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), newTemplate).commit();
            templateContainer.addView(newContainer);
        }
        return view;
    }
}