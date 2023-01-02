package com.example.gymtracker.templates;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.R;
import com.example.gymtracker.TextViewTableRowFragment;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TemplateFragment extends Fragment {

    private static final Locale l = Locale.GERMAN;

    private static final String ARG_TEMPLATE = "param1";

    private Workout template;

    public TemplateFragment() {
        // Required empty public constructor
    }

    public static TemplateFragment newInstance(Workout workout) {
        TemplateFragment fragment = new TemplateFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TEMPLATE, workout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            template = (Workout) getArguments().getSerializable(ARG_TEMPLATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_template, container, false);

        //title
        ((TextView) view.findViewById(R.id.name_of_template_text_view)).
                setText(template.getName());

        //last done
        String lastDone = DatabaseManager.getLastDateDoneTemplate(template.getName());
        if (lastDone != null) {
            String today = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault()).format(new Date());
            int dateDiff = Formatter.getDateDiff(lastDone.substring(0, 10), today);
            String days = (dateDiff == 1) ? getString(R.string.day) : getString(R.string.daysPlural);
            String text = getString(R.string.lastDoneDays) + " " + dateDiff + " " + days;
            ((TextView) view.findViewById(R.id.template_last_done_text_view)).
                    setText(text);
        }
        else {
            view.findViewById(R.id.template_last_done_text_view).
                    setVisibility(View.INVISIBLE);
        }


        TableLayout tableLayout = view.findViewById(R.id.template_exercises_table_layout);
        for (Exercise exercise : template.getExercises()) {
            String newLineString = String.format(l,
                    "%d × %s", exercise.getSets().size(), exercise.getName());
            TextViewTableRowFragment newLine = TextViewTableRowFragment.
                    newInstance(newLineString, false);
            FragmentContainerView newContainer = new FragmentContainerView(getContext());
            newContainer.setId(View.generateViewId());
            getParentFragmentManager().beginTransaction()
                    .add(newContainer.getId(), newLine,
                    "TEMPLATE_ROW" + exercise.getName()).commit();
            tableLayout.addView(newContainer);
        }

        //initialize buttons
        ImageButton templateMenuButton = view.findViewById(R.id.template_menu_button);
        templateMenuButton.setOnClickListener(view1 -> templateMenuClick());
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //remove Exercise from template
        if (resultCode == RESULT_OK && requestCode == 0) {
            deleteExerciseFromTemplate(data.getExtras().getString("ITEM"));
        }
        //add Exercise to template
        else if (resultCode == RESULT_OK && requestCode == 1) {
            addToTemplate(data.getExtras().getString("ITEM"));
        }
    }

    private void templateMenuClick() {
        ImageButton thisButton = getView().findViewById(R.id.template_menu_button);
        PopupMenu popup = new PopupMenu(getContext(), thisButton);
        popup.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.delete_template_menu) {
                deleteTemplate();
            }
            else if (id == R.id.add_to_template_menu) {
                addToTemplate();
            }
            else if (id == R.id.delete_from_template_menu) {
                deleteExerciseFromTemplate();
            }
            else if (id == R.id.rename_template_menu) {
                renameTemplate();
            }
            return false;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.template_menu, popup.getMenu());
        popup.show();
    }

    private void deleteTemplate() {
        DatabaseManager.deleteTemplate(template.getName());
        Fragment thisFragment = getParentFragmentManager().
                findFragmentByTag("TEMPLATE" + template.getName());
        getParentFragmentManager().beginTransaction().remove(thisFragment).commit();
        Toast.makeText(getContext(),
                getResources().getString(R.string.templateDeleted),
                Toast.LENGTH_SHORT).show();
    }

    private void addToTemplate() {
        Exercise[] exercises = DatabaseManager.getExercisesInTemplate(template.getName());
        String[] exercisesInTemplate = new String[exercises.length];
        for (int i = 0; i < exercisesInTemplate.length; i++) {
            exercisesInTemplate[i] = exercises[i].getName();
        }
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("REMOVE_LIST", exercisesInTemplate);
        intent.putExtra("TITLE", getResources().getString(R.string.addExerciseToTemplate));
        startActivityForResult(intent, 1);
    }

    private void addToTemplate(String exerciseName) {
        DatabaseManager.addExerciseToTemplate(template.getName(), exerciseName);

        TableLayout tableLayout = getView().findViewById(R.id.template_exercises_table_layout);
        String newLineString = String.format(l,
                "%d × %s", 3, exerciseName);
        TextViewTableRowFragment newLine = TextViewTableRowFragment.
                newInstance(newLineString, false);
        FragmentContainerView newContainer = new FragmentContainerView(getContext());
        newContainer.setId(View.generateViewId());
        getParentFragmentManager().beginTransaction()
                .add(newContainer.getId(), newLine,
                "TEMPLATE_ROW" + exerciseName).commit();
        tableLayout.addView(newContainer);

        template.addExercise(exerciseName);

        Toast.makeText(getContext(),
                getResources().getString(R.string.exerciseAdded),
                Toast.LENGTH_SHORT).show();
    }

    private void deleteExerciseFromTemplate() {
        Exercise[] exercises = DatabaseManager.getExercisesInTemplate(template.getName());
        String[] exercisesInTemplate = new String[exercises.length];
        if (exercises.length == 1) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.cantRemoveLastExercise),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < exercisesInTemplate.length; i++) {
            exercisesInTemplate[i] = exercises[i].getName();
        }
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        intent.putExtra("LIST", exercisesInTemplate);
        intent.putExtra("TITLE", getResources().getString(R.string.removeExercise));
        startActivityForResult(intent, 0);
    }

    private void deleteExerciseFromTemplate(String exerciseName) {
        DatabaseManager.deleteExerciseFromTemplate(template.getName(), exerciseName);
        Fragment exerciseRowFragment = getParentFragmentManager().
                findFragmentByTag("TEMPLATE_ROW" + exerciseName);
        getParentFragmentManager().beginTransaction().remove(exerciseRowFragment).commit();

        template.removeExercise(exerciseName);

        Toast.makeText(getContext(),
                getResources().getString(R.string.exerciseRemoved),
                Toast.LENGTH_SHORT).show();
    }

    private void renameTemplate() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getResources().getString(R.string.changeTemplateName));
        alert.setMessage(getResources().getString(R.string.changeTemplateNameText));
        final View customLayout = getLayoutInflater().inflate(R.layout.alert, null);
        alert.setView(customLayout);

        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            EditText et = customLayout.findViewById(R.id.alert_input_edit_text);
            String newTemplateName = et.getText().toString();
            if (DatabaseManager.doesTemplateExist(newTemplateName)) {
                Toast.makeText(getContext(),
                        getResources().getString(R.string.toastTemplateAlreadyExists),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseManager.renameTemplate(template.getName(), newTemplateName);
            Toast.makeText(getContext(),
                    getResources().getString(R.string.templateRenamed),
                    Toast.LENGTH_SHORT).show();
            ((TextView) getView().findViewById(R.id.name_of_template_text_view)).
                    setText(newTemplateName);
            template.setName(newTemplateName);
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }
}