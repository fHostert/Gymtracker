package com.example.gymtracker.templates;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.R;
import com.example.gymtracker.TextViewTableRowFragment;
import com.example.gymtracker.helper.DatabaseManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AddTemplateActivity extends AppCompatActivity {

    private String name = "";
    private final ArrayList<String> exercises = new ArrayList<>();


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

    public void addExerciseToTemplateClick(View view) {
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

    private void addExerciseToTemplate(String exerciseName) {
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
        if (exercises.size() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.noExerciseInTemplate),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseManager.saveTemplate(name, exercises);
        setResult(RESULT_OK, new Intent());
        finish();
    }
}