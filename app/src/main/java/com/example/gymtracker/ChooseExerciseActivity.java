package com.example.gymtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseExerciseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_exercise);
        this.setTitle(R.string.chooseExercise);

        //Fill ListView with exercises
        ListView listView = findViewById(R.id.exercises_list_view);
        String[] exercises = DatabaseManager.getExercises();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, exercises);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {

            //Get filtered List
            String[] filteredList = new String[arrayAdapter.getCount()];
            for (int j = 0; j<arrayAdapter.getCount(); j++) {
                filteredList[j] = arrayAdapter.getItem(j);
            }

            setResult(RESULT_OK,
                    (new Intent()).putExtra("EXERCISE_NAME_KEY", filteredList[i]));
            finish();
        });



        //Search Logic
        EditText searchET = findViewById(R.id.search_edit_text);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

        });
    }


}