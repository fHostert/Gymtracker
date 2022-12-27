package com.example.gymtracker.stats;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gymtracker.ChooseActivity;
import com.example.gymtracker.R;
import com.example.gymtracker.charts.ChartFormatter.DateFormatterXAxis;
import com.example.gymtracker.charts.datastructures.WorkoutEntry;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsFragment extends Fragment {

    private EditText daysToAverageOverET;
    private EditText daysToShowET;

    public StatsFragment() {
        // Required empty public constructor
    }

    public static StatsFragment newInstance(String param1, String param2) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        daysToAverageOverET = view.findViewById(R.id.days_to_average_over_edit_text);
        daysToShowET = view.findViewById(R.id.days_to_show_edit_text);

        TextWatcher refreshChartTW = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                refreshDurationChart(view);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
        daysToAverageOverET.addTextChangedListener(refreshChartTW);
        daysToShowET.addTextChangedListener(refreshChartTW);

        refreshDurationChart(view);
        styleChart(view);

        Button statsForExerciseButton = view.findViewById(R.id.stats_for_exercise_button);
        statsForExerciseButton.setOnClickListener(view1 -> statsForExerciseClick());

        TextView workoutCountTV = view.findViewById(R.id.stats_workout_sum_text_view);
        TextView totalDurationTV = view.findViewById(R.id.stats_duration_sum_text_view);
        TextView totalWeightTV = view.findViewById(R.id.stats_total_weight_sum_text_view);

        workoutCountTV.setText(String.valueOf(DatabaseManager.getWorkoutCount()));
        totalDurationTV.setText(Formatter.formatTime(DatabaseManager.getTotalDuration() / 1000));
        totalWeightTV.setText(String.format("%s KG", Formatter.formatFloat(DatabaseManager.getTotalWeight())));

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //stats for Exercise
        if (resultCode == RESULT_OK && requestCode == 0) {
            statsForExercise(data.getExtras().getString("ITEM"));
        }
    }

    private void statsForExerciseClick() {
        final Intent intent = new Intent(getContext(), ChooseActivity.class);
        intent.putExtra("LIST", DatabaseManager.getExercisesDoneAtLeastOnce());
        intent.putExtra("TITLE", getResources().getString(R.string.statsForExercise));
        startActivityForResult(intent, 0);
    }

    private void statsForExercise(String exerciseName) {
        if (DatabaseManager.wasExerciseNeverDone(exerciseName)) {
            Toast.makeText(getContext(),
                    getResources().getString(R.string.toastNeverDoneExercise),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final Intent intent = new Intent(getContext(), StatsForExerciseActivity.class);
        intent.putExtra("EXERCISE", exerciseName);
        startActivity(intent);
    }

    private void refreshDurationChart(View view) {
        String daysToShowString = String.valueOf(daysToShowET.getText());
        String daysToAverageOverString = String.valueOf(daysToAverageOverET.getText());

        int daysToShow = (daysToShowString.equals("") ? 0: Integer.parseInt(daysToShowString));
        int daysToAverageOver = (daysToAverageOverString.equals("") ? 0 : Integer.parseInt(daysToAverageOverString));
        ArrayList<WorkoutEntry> history = DatabaseManager.getWorkoutEntries(daysToShow, daysToAverageOver);
        if (history == null) {
            return;
        }
        LineChart chart = view.findViewById(R.id.trainings_duration_chart);
        chart.getXAxis().setValueFormatter(new DateFormatterXAxis());
        List<Entry> entries = new ArrayList<>();
        for (WorkoutEntry entry : history) {
            long timestamp = Formatter.convertDateToUnixTimestampSeconds(entry.getDate());
            float durationInMinutes = (float) entry.getDuration() / 60;
            float average = durationInMinutes / daysToAverageOver;
            entries.add(new Entry(timestamp, average));
        }

        //this fixes a common bug in the library
        Collections.sort(entries, new EntryXComparator());

        //add data to set
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.boarders));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        //refresh
        chart.invalidate();

    }

    private void styleChart(View view) {
        LineChart chart = view.findViewById(R.id.trainings_duration_chart);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.getXAxis().setLabelRotationAngle(65);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextSize(getResources().getDimension(R.dimen.chart_text_size));
        chart.setBackgroundColor(getResources().getColor(R.color.chartBackground));
        chart.getXAxis().setTextColor(getResources().getColor(R.color.chartTextColor));
        chart.getAxisLeft().setTextColor(getResources().getColor(R.color.chartTextColor));
        chart.getAxisRight().setTextColor(getResources().getColor(R.color.chartTextColor));
    }
}