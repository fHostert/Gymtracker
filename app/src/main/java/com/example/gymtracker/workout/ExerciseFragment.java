package com.example.gymtracker.workout;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.R;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.stats.StatsForExerciseActivity;

import java.util.Objects;

public class ExerciseFragment extends Fragment {

    private static final String ARG_EXERCISE = "param1";
    private static final String ARG_POSITION = "param2";

    private Exercise exercise;
    private int positionInWorkout;

    public ExerciseFragment() {
        // Required empty public constructor
    }

    public static ExerciseFragment newInstance(Exercise exercise, int positionInWorkout) {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        args.putInt(ARG_POSITION, positionInWorkout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = (Exercise) getArguments().getSerializable(ARG_EXERCISE);
            positionInWorkout = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        //Set exercise name
        ((TextView) view.findViewById(R.id.name_of_exercise_text_view)).
                setText(exercise.getName());

        //Add Sets
        for (Set set : exercise.getSets()) {
            addSet(set, view, false);
        }

        //Restore note
        if (!Objects.equals(DatabaseManager.getCurrentNote(exercise.getExerciseID()), "")) {
            addNote(view, false);
            EditText noteET = view.findViewById(R.id.exercise_note_edit_text);
            noteET.setText(exercise.getNote());
        }

        //Initialize buttons
        Button addSetButton = view.findViewById(R.id.add_set_button);
        addSetButton.setOnClickListener(view1 -> addEmptySet());

        ImageButton exerciseMenuButton = view.findViewById(R.id.exercise_menu_button);
        exerciseMenuButton.setOnClickListener(view1 -> exerciseMenuClick());

        ImageButton saveNoteButton = view.findViewById(R.id.delete_note_button);
        saveNoteButton.setOnClickListener(view1 -> deleteNoteClick(view));

        EditText noteET = view.findViewById(R.id.exercise_note_edit_text);
        noteET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //update note on text change
                EditText noteET = view.findViewById(R.id.exercise_note_edit_text);
                String note = String.valueOf(noteET.getText());
                DatabaseManager.addNoteToExercise(note, exercise.getExerciseID());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Reduce text size until it fits
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                TextView textView = view.findViewById(R.id.name_of_exercise_text_view);
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                float originalTextSize = textView.getTextSize();
                TextPaint textPaint = textView.getPaint();
                float textWidth = textPaint.measureText(textView.getText().toString());
                while (textWidth > textView.getWidth()) {
                    originalTextSize--;
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
                    textWidth = textPaint.measureText(textView.getText().toString());
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //replace Exercise
        if (resultCode == RESULT_OK && requestCode == 0) {
            replace(data.getExtras().getString("ITEM"));
        }
    }

    private void addSet(Set set, View view, boolean addToCurrentWorkoutTable) {
        LinearLayout setContainer = view.findViewById(R.id.set_container);
        SetFragment setFragment = SetFragment.newInstance(set, exercise.getExerciseID());
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), setFragment).commit();
        setContainer.addView(newContainer);

        if (addToCurrentWorkoutTable) {
            DatabaseManager.addSetToExerciseInCurrentWorkout(exercise.getExerciseID(), set);
        }

        TextView textView = view.findViewById(R.id.name_of_exercise_text_view); // Ersetze "R.id.textView" durch die ID deines TextView-Elements
    }

    private void addEmptySet() {
        LinearLayout setContainer = getView().findViewById(R.id.set_container);
        int setIndex = setContainer.getChildCount() + 1;
        Set set = new Set(setIndex);
        addSet(set, getView(), true);
    }

    private void exerciseMenuClick() {
        ImageButton thisButton = getView().findViewById(R.id.exercise_menu_button);
        PopupMenu popup = new PopupMenu(getContext(), thisButton);
        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.move_exercise_up_menu) {
                moveExerciseUp();
            }
            else if (id == R.id.move_exercise_down_menu) {
                moveExerciseDown();
            }
            else if (id == R.id.remove_exercise_menu) {
                deleteExercise();
            }
            else if (id == R.id.replace_exercise_menu) {
                replaceExerciseClick();
            }
            else if (id == R.id.delete_last_set_menu) {
                deleteLastSet();
            }
            else if (id == R.id.add_exercise_note_menu) {
                addNote(getView(), true);
            }
            else if (id == R.id.show_stats_menu){
                showStats();
            }
            return false;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.exercise_menu, popup.getMenu());
        popup.show();
    }

    private void moveExerciseUp() {
        //Get the exercise container
        LinearLayout exerciseContainer =
                ((WorkoutFragment) getParentFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT"))
                        .getExerciseLayout();

        //Exercise already up
        if (positionInWorkout == 0) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastExerciseAlreadyUp),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Update Database
        DatabaseManager.moveExerciseUp(exercise.getExerciseID(), positionInWorkout);

        //Swap exercises in parent container
        View thisExercise = exerciseContainer.getChildAt(positionInWorkout);
        exerciseContainer.removeViewAt(positionInWorkout);
        exerciseContainer.addView(thisExercise, positionInWorkout - 1);

        //Update Position in Workout
        ((WorkoutFragment) getParentFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT"))
                .refreshExercisePositions();
    }

    private void moveExerciseDown() {
        //Get the exercise container
        LinearLayout exerciseContainer =
                ((WorkoutFragment) getParentFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT"))
                .getExerciseLayout();

        //Exercise already down
        if (positionInWorkout == exerciseContainer.getChildCount() - 1) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastExerciseAlreadyDown),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Update Database
        DatabaseManager.moveExerciseDown(exercise.getExerciseID(), positionInWorkout);

        //Swap exercises in parent container
        View thisExercise = exerciseContainer.getChildAt(positionInWorkout);
        exerciseContainer.removeViewAt(positionInWorkout);
        exerciseContainer.addView(thisExercise, positionInWorkout + 1);

        //Update Position in Workout
        ((WorkoutFragment) getParentFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT"))
                .refreshExercisePositions();
    }

    private void deleteExercise() {
        //Update Database
        DatabaseManager.deleteExerciseFromCurrentWorkout(exercise.getExerciseID());

        //Get the exercise container
        LinearLayout exerciseContainer = ((View) getView().getParent().getParent())
                .findViewById(R.id.exercise_container);
        //Delete the exercise
        exerciseContainer.removeViewAt(positionInWorkout);

        Toast.makeText(getContext(),
                getResources().getString(R.string.exerciseRemoved),
                Toast.LENGTH_SHORT).show();

        //Update Position in Workout
        ((WorkoutFragment) getParentFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT"))
                .refreshExercisePositions();
    }

    private void replaceExerciseClick() {
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        String[] exercisesInWorkout = DatabaseManager.getExercisesInCurrentWorkout();
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("REMOVE_LIST", exercisesInWorkout);
        intent.putExtra("TITLE", getResources().getString(R.string.replaceExerciseThrough));
        startActivityForResult(intent, 0);
    }

    private void replace(String newExerciseName) {
        //Update Database
        DatabaseManager.replaceExercise(DatabaseManager.getExerciseID(newExerciseName),
                exercise.getExerciseID(), positionInWorkout);

        //Update Layout
        ((WorkoutFragment) getParentFragmentManager().findFragmentByTag("WORKOUT_FRAGMENT"))
                .replaceExercise(newExerciseName, positionInWorkout);

        //Update exercise
        exercise = new Exercise(DatabaseManager.getExerciseID(newExerciseName));
    }

    private void deleteLastSet() {
        //Get parent container
        LinearLayout setContainer = getView().findViewById(R.id.set_container);
        if (setContainer.getChildCount() == 1) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.cantRemoveLastSet),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //remove last set
        setContainer.removeViewAt(setContainer.getChildCount() - 1);

        //update Database
        DatabaseManager.removeSet(exercise.getExerciseID(),
                setContainer.getChildCount() + 1);
    }

    private void addNote(View view, boolean giveFocus) {
        EditText noteET = view.findViewById(R.id.exercise_note_edit_text);
        noteET.setVisibility(View.VISIBLE);
        if (giveFocus) {
            noteET.requestFocus();
        }

        ImageButton saveNoteButton = view.findViewById(R.id.delete_note_button);
        saveNoteButton.setVisibility(View.VISIBLE);

        String note = String.valueOf(noteET.getText());
        if (!note.equals("")) {
            DatabaseManager.addNoteToExercise(note, exercise.getExerciseID());
        }
    }

    private void deleteNoteClick(View view) {
        DatabaseManager.deleteNoteFromExercise(exercise.getExerciseID());
        Toast.makeText(getContext(),
                getResources().getString(R.string.noteDeleted),
                Toast.LENGTH_SHORT).show();

        EditText noteET = view.findViewById(R.id.exercise_note_edit_text);
        noteET.setVisibility(View.GONE);

        ImageButton saveNoteButton = view.findViewById(R.id.delete_note_button);
        saveNoteButton.setVisibility(View.GONE);
    }

    private void showStats() {
        if (DatabaseManager.wasExerciseNeverDone(exercise.getName())) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastNeverDoneExercise),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final Intent intent = new Intent(getContext(), StatsForExerciseActivity.class);
        intent.putExtra("EXERCISE", exercise.getName());
        startActivity(intent);
    }

    public void setPositionInWorkout(int i) {
        positionInWorkout = i;
    }
}