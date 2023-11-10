package com.fhostert.gymtracker.stats;

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

import com.fhostert.gymtracker.ChooseActivity;
import com.fhostert.gymtracker.R;
import com.fhostert.gymtracker.charts.ChartFormatter.DateFormatterXAxis;
import com.fhostert.gymtracker.charts.datastructures.WorkoutEntry;
import com.fhostert.gymtracker.helper.DatabaseManager;
import com.fhostert.gymtracker.helper.Formatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

        //Refresh Chart on values changed
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

        //First Loading
        refreshDurationChart(view);
        styleChart(view);

        //initialize Buttons
        Button statsForExerciseButton = view.findViewById(R.id.stats_for_exercise_button);
        statsForExerciseButton.setOnClickListener(view1 -> statsForExerciseClick());

        //initialize TextViews
        TextView workoutCountTV = view.findViewById(R.id.stats_workout_sum_text_view);
        TextView totalDurationTV = view.findViewById(R.id.stats_duration_sum_text_view);
        TextView totalWeightTV = view.findViewById(R.id.stats_total_weight_sum_text_view);

        workoutCountTV.setText(String.valueOf(DatabaseManager.getWorkoutCount()));
        totalDurationTV.setText(Formatter.formatTime(DatabaseManager.getTotalDuration()));
        totalWeightTV.setText(String.format(
                "%s KG", Formatter.formatFloat(DatabaseManager.getTotalWeight())));

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
        final Intent intent = new Intent(getContext(), StatsForExerciseActivity.class);
        intent.putExtra("EXERCISE", exerciseName);
        startActivity(intent);
    }

    private void refreshDurationChart(View view) {
        //Get parameters
        String daysToShowString = String.valueOf(daysToShowET.getText());
        String daysToAverageOverString = String.valueOf(daysToAverageOverET.getText());
        int daysToShow =
            (daysToShowString.equals("") ? 0: Integer.parseInt(daysToShowString));
        int daysToAverageOver =
            (daysToAverageOverString.equals("") ? 0 : Integer.parseInt(daysToAverageOverString));

        //Get data
        ArrayList<WorkoutEntry> history =
                DatabaseManager.getWorkoutEntries(daysToShow, daysToAverageOver);
        if (history == null) {
            return;
        }
        LineChart chart = view.findViewById(R.id.trainings_duration_chart);

        //This formatter transforms the unix timestamp to a date
        chart.getXAxis().setValueFormatter(new DateFormatterXAxis());

        //Add data to entries list
        List<Entry> entries = new ArrayList<>();
        float sum = 0;
        for (WorkoutEntry workout : history) {
            long timestamp = Formatter.convertDateToUnixTimestampSeconds(workout.getDate());
            float durationInMinutes = (float) workout.getDuration() / 60;
            float average = durationInMinutes / daysToAverageOver;
            entries.add(new Entry(timestamp, average));
            sum += average;
        }
        //this fixes a common bug in the library
        Collections.sort(entries, new EntryXComparator());

        //add data to set
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(getResources().getColor(R.color.boarders));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));

        //bind set to chart
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        LimitLine averageLine = new LimitLine(sum / history.size());
        averageLine.setLineColor(getResources().getColor(R.color.red));
        averageLine.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(averageLine);

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