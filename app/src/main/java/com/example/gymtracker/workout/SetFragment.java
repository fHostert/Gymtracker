package com.example.gymtracker.workout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.MainActivity;
import com.example.gymtracker.datastructures.Settings;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Set;

import java.util.Locale;

public class SetFragment extends Fragment {

    private static final Locale l = Locale.GERMAN;

    private static final String ARG_SET = "param1";
    private static final String ARX_EXERCISE_ID = "param2";

    private Set set;
    private int exerciseID;

    private EditText weightET;
    private EditText repsET;
    private boolean areHintsSet;

    public SetFragment() {
        // Required empty public constructor
    }

    public static SetFragment newInstance(Set set, int exerciseID) {
        SetFragment fragment = new SetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SET, set);
        args.putInt(ARX_EXERCISE_ID, exerciseID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            set = (Set) getArguments().getSerializable(ARG_SET);
            exerciseID = getArguments().getInt(ARX_EXERCISE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_set, container, false);
        weightET = ((EditText) view.findViewById(R.id.weight_edit_text));
        repsET = ((EditText)view.findViewById(R.id.reps_edit_text));
        areHintsSet = false;

        //Fill fields
        //Set index field
        ((TextView) view.findViewById(R.id.set_index_text_view)).
                setText(String.valueOf(set.getIndex()));

        //Last Set field
        Set lastSet = DatabaseManager.getLastSet(exerciseID, set.getIndex());
        String lastSetString =
                (lastSet == null) ? "-" : String.format(l,
                        "%skg × %d", Formatter.formatFloat(lastSet.getWeight()),
                        lastSet.getReps());
        if (lastSet != null && lastSet.getWeight() == 0) {
            lastSetString = String.format(l,
                    "%d %s", lastSet.getReps(), getResources().getString(R.string.reps__));
        }
        ((TextView) view.findViewById(R.id.last_set_text_view)).
                setText(lastSetString);

        //Reps and weight field
        //Restoring workout
        if (set.getReps() != 0) {
            weightET.setText(Formatter.formatFloat(set.getWeight()));
            repsET.setText(String.valueOf(set.getReps()));
            colorSet(view);
        }
        //Set last set as hint
        else if (!lastSetString.equals("-")){
            weightET.setHint(Formatter.formatFloat(lastSet.getWeight()));
            repsET.setHint(String.valueOf(lastSet.getReps()));
            areHintsSet = true;
        }

        //initialize buttons
        Button saveSetButton = view.findViewById(R.id.save_set_button);
        saveSetButton.setOnClickListener(view1 -> saveSet());

        return view;
    }

    private void colorSet(View view) {
        weightET.setHintTextColor(weightET.getCurrentTextColor());
        repsET.setHintTextColor(repsET.getCurrentTextColor());
        view.findViewById(R.id.set_table_row).setBackgroundColor(
                getResources().getColor(R.color.setCompleted));

        //Change background color to second option
        /*weightET.setTextColor(getResources().getColor(R.color.black));
        repsET.setTextColor(getResources().getColor(R.color.black));
        weightET.setHintTextColor(getResources().getColor(R.color.black));
        repsET.setHintTextColor(getResources().getColor(R.color.black));
        ((TextView) view.findViewById(R.id.last_set_text_view))
                .setTextColor(getResources().getColor(R.color.black));
        ((TextView) view.findViewById(R.id.set_index_text_view))
                .setTextColor(getResources().getColor(R.color.black));*/
    }

    private void saveSet() {
        if (!DatabaseManager.arePreviousSetsDone(exerciseID, set.getIndex())){
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastFinishPreviousSetFirst),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Get values
        String repsString = String.valueOf(repsET.getText());
        String weightString = String.valueOf(weightET.getText());

        //Get hints as values if set
        repsString = (((repsString.equals("") && areHintsSet)
                ? (String) repsET.getHint() : repsString));
        weightString = (((weightString.equals("") && areHintsSet)
                ? (String) weightET.getHint() : weightString));

        //Cast data
        int reps = (repsString.equals("") ? 0: Integer.parseInt(repsString));
        float weight = (weightString.equals("") ? 0 : Float.parseFloat(weightString));
        if (reps < 1) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastNoRepsDone),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Update Database and color set
        set.setReps(reps);
        set.setWeight(weight);
        DatabaseManager.updateSet(exerciseID, set);
        colorSet(getView());

        //start Timer
        Settings settings = DatabaseManager.getSettings();
        if (settings.timerAutoPlay && DatabaseManager.getCurrentWorkoutTimerIsActive())
            ((MainActivity) getActivity()).startTimer();
    }
}