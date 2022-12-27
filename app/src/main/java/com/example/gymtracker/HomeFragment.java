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

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                //Load template in database
                DatabaseManager.loadTemplate(workoutName);

                //Handle as if app got closed during workout
                //Templated will get loaded in.
                ((MainActivity)getActivity()).restoreWorkout();
            });

            //Give each template a unique tag and add it
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), newTemplate,
                            "TEMPLATE" + template.getName()).commit();
            templateContainer.addView(newContainer);
        }
        return view;
    }
}