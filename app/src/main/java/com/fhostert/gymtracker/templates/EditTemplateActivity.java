package com.fhostert.gymtracker.templates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fhostert.gymtracker.ChooseActivity;
import com.fhostert.gymtracker.R;
import com.fhostert.gymtracker.helper.DatabaseManager;

import java.util.ArrayList;
import java.util.Objects;

public class EditTemplateActivity extends AppCompatActivity {

    private String name = "";
    private ArrayList<TemplateRowFragment> fragments = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_template);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("NAME");
            String[] exercisesInTemplate = extras.getStringArray("EXERCISES");
            int[] numberOfSets = extras.getIntArray("NUMBEROFSETS");

            //editing Template
            if (exercisesInTemplate != null) {
                int counter = 0;
                for (String name : exercisesInTemplate) {
                    addExerciseToTemplate(name, numberOfSets[counter]);
                    counter++;
                }
            }
        }
        ((TextView) findViewById(R.id.add_template_name_text_view)).setText(name);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //add Exercise to Template
        if (resultCode == RESULT_OK && requestCode == 0) {
            addExerciseToTemplate(data.getExtras().getString("ITEM"), 3);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_template_menu, menu);

        //Color icons
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.setTint(getColor(R.color.white));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //create new exercise
        if (id == R.id.create_new_exercise_menu_add_template) {
            createNewExercise();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewExercise() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.createNewExerciseText));
        alert.setTitle(getResources().getString(R.string.createNewExercise));
        final View customLayout = getLayoutInflater().inflate(R.layout.alert, null);
        alert.setView(customLayout);

        alert.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
            EditText et = customLayout.findViewById(R.id.alert_input_edit_text);
            String newExerciseName = et.getText().toString();
            for (String exercise : DatabaseManager.getExercises()) {
                if (Objects.equals(exercise, newExerciseName) || newExerciseName.equals("")) {
                    Toast.makeText(this,
                            getResources().getString(R.string.exerciseAlreadyExists),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            DatabaseManager.createNewExercise(newExerciseName);
            Toast.makeText(this,
                    getResources().getString(R.string.newExerciseCreated),
                    Toast.LENGTH_SHORT).show();
        });

        //If cancel, do nothing
        alert.setNegativeButton(getResources().getString(R.string.cancel), (dialog, whichButton) -> {
            //Do nothing and cancel
        });

        alert.show();
    }

    public void addExerciseToTemplateClick(View view) {
        ArrayList<String> exercises = getExercises();
        String[] exercisesInTemplate = new String[exercises.size()];
        for (int i = 0; i < exercisesInTemplate.length; i++) {
            exercisesInTemplate[i] = exercises.get(i);
        }
        final Intent intent = new Intent(this, ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("REMOVE_LIST", exercisesInTemplate);
        intent.putExtra("TITLE", getResources().getString(R.string.addExerciseToTemplate));
        startActivityForResult(intent, 0);
    }

    private void addExerciseToTemplate(String exerciseName, int numberOfSets) {
        LinearLayout exerciseContainer = findViewById(R.id.new_template_exercises_layout);
        TemplateRowFragment newLine = TemplateRowFragment.
                newInstance(exerciseName, numberOfSets);
        FragmentContainerView newContainer = new FragmentContainerView(this);
        newContainer.setId(View.generateViewId());
        getSupportFragmentManager().beginTransaction()
                .add(newContainer.getId(), newLine).commit();
        exerciseContainer.addView(newContainer);
        fragments.add(newLine);
    }

    private ArrayList<String> getExercises() {
        ArrayList<String> exercises = new ArrayList<>();
        LinearLayout exerciseContainer = findViewById(R.id.new_template_exercises_layout);
        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View childView = exerciseContainer.getChildAt(i);
            TextView exerciseNameTV = (TextView)childView.findViewById(R.id.text_view_template_row);
            if (exerciseNameTV == null)
                continue;
            String exerciseName = exerciseNameTV.getText().toString();
            exercises.add(exerciseName);
        }
        return exercises;
    }

    private int[] getNumberOfSets() {
        int[] numberOfSets = new int[getExercises().size()];
        LinearLayout exerciseContainer = findViewById(R.id.new_template_exercises_layout);
        int counter = 0;
        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View childView = exerciseContainer.getChildAt(i);
            EditText numberOfSetsET = (EditText)childView.findViewById(R.id.number_of_sets_edit_text);
            if (numberOfSetsET == null) {
                counter++;
                continue;
            }

            int sets = Integer.parseInt(numberOfSetsET.getText().toString());
            numberOfSets[i - counter] = sets;
        }
        return numberOfSets;
    }

    public void saveTemplateClick(View view) {
        if (getExercises().size() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.noExerciseInTemplate),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseManager.saveTemplate(name, getExercises(), getNumberOfSets());
        setResult(RESULT_OK, new Intent());
        finish();
    }

    public void moveExerciseUp(String name){
        LinearLayout exerciseContainer = findViewById(R.id.new_template_exercises_layout);
        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View childView = exerciseContainer.getChildAt(i);
            TextView exerciseNameTV = (TextView)childView.findViewById(R.id.text_view_template_row);
            if (exerciseNameTV == null)
                continue;
            String exerciseName = exerciseNameTV.getText().toString();
            if (exerciseName.equals(name)) {
                if (i == 0) {
                    Toast.makeText(this,
                            getResources().getString(R.string.toastExerciseAlreadyUp),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                exerciseContainer.removeViewAt(i);
                exerciseContainer.addView(childView, i - 1);
                return;
            }
        }
    }

    public void moveExerciseDown(String name){
        LinearLayout exerciseContainer = findViewById(R.id.new_template_exercises_layout);
        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View childView = exerciseContainer.getChildAt(i);
            TextView exerciseNameTV = (TextView)childView.findViewById(R.id.text_view_template_row);
            if (exerciseNameTV == null)
                continue;
            String exerciseName = exerciseNameTV.getText().toString();
            if (exerciseName.equals(name)) {
                if (i == exerciseContainer.getChildCount() - 1) {
                    Toast.makeText(this,
                            getResources().getString(R.string.toastExerciseAlreadyDown),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                exerciseContainer.removeViewAt(i);
                exerciseContainer.addView(childView, i + 1);
                return;
            }
        }
    }
}