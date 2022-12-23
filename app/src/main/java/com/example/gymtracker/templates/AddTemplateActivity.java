package com.example.gymtracker.templates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.R;
import com.example.gymtracker.TextViewTableRowFragment;
import com.example.gymtracker.helper.DatabaseManager;

import java.util.ArrayList;
import java.util.Objects;

public class AddTemplateActivity extends AppCompatActivity {

    private String name = "";
    private ArrayList<String> exercises = new ArrayList<>();

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_template);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            name = extras.getString("NAME");
        }
        ((TextView) findViewById(R.id.add_template_name_text_view)).setText(name);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //add Exercise to Template
        if (resultCode == RESULT_OK && requestCode == 0) {
            addExerciseToTemplate(data.getExtras().getString("ITEM"));
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this,
                getResources().getString(R.string.doublePressToExitAddTemplate),
                Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(()
                -> doubleBackToExitPressedOnce=false, 2000);
    }

    public void addExerciseToTemplateClick(View view) {
        final Intent intent = new Intent(this, ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercises());
        intent.putExtra("TITLE", getResources().getString(R.string.addExerciseToTemplate));
        startActivityForResult(intent, 0);
    }

    private void addExerciseToTemplate(String exerciseName) {
        for (String exercise : exercises) {
            if (Objects.equals(exercise, exerciseName)) {
                Toast.makeText(this,
                        getResources().getString(R.string.toastExerciseAlreadyInNewTemplate),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        exercises.add(exerciseName);

        LinearLayout exerciseContainer = findViewById(R.id.new_template_exercises_layout);
        TextViewTableRowFragment newLine = TextViewTableRowFragment.
                newInstance(exerciseName, true);
        FragmentContainerView newContainer = new FragmentContainerView(this);
        newContainer.setId(View.generateViewId());
        getSupportFragmentManager().beginTransaction()
                .add(newContainer.getId(), newLine).commit();
        exerciseContainer.addView(newContainer);
    }

    public void saveTemplateClick(View view) {
        DatabaseManager.saveTemplate(name, exercises);
        finish();
    }
}