package com.fhostert.gymtracker.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.fhostert.gymtracker.R;
import com.fhostert.gymtracker.TextViewTableRowFragment;
import com.fhostert.gymtracker.datastructures.Exercise;
import com.fhostert.gymtracker.datastructures.Set;
import com.fhostert.gymtracker.datastructures.Workout;
import com.fhostert.gymtracker.helper.Formatter;

import java.util.Locale;
import java.util.Objects;

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

        // add title
        String title = Formatter.formatDate(workout.getDate()) + " - " + workout.getName();
        this.setTitle(title);

        TableLayout container = findViewById(R.id.detail_view_container);
        for (Exercise exercise : workout.getExercises()) {

            //add note
            if (!Objects.equals(exercise.getNote(), "")) {
                TextViewTableRowFragment newLine = TextViewTableRowFragment.
                        newInstance(getString(R.string.note) + " " + exercise.getNote(), true);
                FragmentContainerView newContainer = new FragmentContainerView(this);
                newContainer.setId(View.generateViewId());
                getSupportFragmentManager().beginTransaction()
                        .add(newContainer.getId(), newLine).commit();
                container.addView(newContainer);
            }

            //add sets
            for (Set set : exercise.getSets()) {
                String personalRecord = (set.isPR()) ? "\uD83C\uDFC6" : "";
                String newLineString = String.format(l,
                        "%s%s %d. %s %s: %s", personalRecord,
                        Formatter.tendency(set.getTendency()), set.getIndex(), getString(R.string.set),
                        exercise.getName(), set.getSetString());
                TextViewTableRowFragment newLine = TextViewTableRowFragment.
                        newInstance(newLineString, true);
                FragmentContainerView newContainer = new FragmentContainerView(this);
                newContainer.setId(View.generateViewId());
                getSupportFragmentManager().beginTransaction()
                        .add(newContainer.getId(), newLine).commit();
                container.addView(newContainer);
            }

            //add separator
            View separator = new View(this);
            int dimensionInDp =
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                3, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dimensionInDp);
            separator.setLayoutParams(lp);
            separator.setBackgroundColor(getColor(R.color.boarders));
            container.addView(separator);
        }
        container.removeViewAt(container.getChildCount() - 1);
    }
}