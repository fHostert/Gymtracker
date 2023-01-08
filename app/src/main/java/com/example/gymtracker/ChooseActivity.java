package com.example.gymtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ChooseActivity extends AppCompatActivity {

    String title = "";
    String[] list = null;
    String[] removeList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //The title of the activity
            title = extras.getString("TITLE");
            //The list to choose from
            list = extras.getStringArray("LIST");
            //Items that should be removed from the list before choosing
            removeList = extras.getStringArray("REMOVE_LIST");
        }
        //Remove items from removeList
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(list));
        if (removeList != null) {
            arrayList.removeAll(Arrays.asList(removeList));
        }
        //Set Activity title
        this.setTitle(title);

        //If somebody really wants to add all possible exercises to a workout this should stop it
        if (arrayList.size() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.nothingLeftToAdd),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        //Fill ListView with list
        ListView listView = findViewById(R.id.exercises_list_view);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {

            //Get filtered List
            String[] filteredList = new String[arrayAdapter.getCount()];
            for (int j = 0; j<arrayAdapter.getCount(); j++) {
                filteredList[j] = arrayAdapter.getItem(j);
            }

            //finish
            Intent intent = new Intent();
            intent.putExtra("ITEM", filteredList[i]);
            setResult(RESULT_OK, intent);
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