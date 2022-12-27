package com.example.gymtracker.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.example.gymtracker.R;
import com.example.gymtracker.TextViewTableRowFragment;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.History;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Workout;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;

import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    private Workout workout;

    private static final Locale l = Locale.GERMAN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            workout = (Workout) extras.getSerializable("WORKOUT");
        }
        String title = workout.getDate() + " " + workout.getName();
        this.setTitle(title);

        TableLayout container = findViewById(R.id.detail_view_container);
        for (Exercise exercise : workout.getExercises()) {
            for (Set set : exercise.getSets()) {
                String newLineString = String.format(l,
                        "%s %d. %s %s: %s", Formatter.tendency(set.getTendency()),
                        set.getIndex(), getString(R.string.set),
                        exercise.getName(), set.getSetString());
                TextViewTableRowFragment newLine = TextViewTableRowFragment.
                        newInstance(newLineString, true);
                FragmentContainerView newContainer = new FragmentContainerView(this);
                newContainer.setId(View.generateViewId());
                getSupportFragmentManager().beginTransaction()
                        .add(newContainer.getId(), newLine).commit();
                container.addView(newContainer);
            }
        }
    }
}