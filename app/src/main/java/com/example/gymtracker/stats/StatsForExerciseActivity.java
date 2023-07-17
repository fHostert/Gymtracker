package com.example.gymtracker.stats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gymtracker.R;
import com.example.gymtracker.charts.ChartFormatter.DateFormatterXAxis;
import com.example.gymtracker.charts.datastructures.ExerciseEntry;
import com.example.gymtracker.charts.datastructures.ExerciseHistory;
import com.example.gymtracker.charts.datastructures.PersonalRecord;
import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StatsForExerciseActivity extends AppCompatActivity {

    private String exerciseName;

    private static final Locale l = Locale.GERMAN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_for_exercise);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            exerciseName = extras.getString("EXERCISE");
        }
        this.setTitle(exerciseName);

        ExerciseHistory history = DatabaseManager.getExerciseHistory(exerciseName);

        //style chart
        LineChart chart = findViewById(R.id.exercise_volume_chart);
        chart.getDescription().setEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.getXAxis().setLabelRotationAngle(65);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextSize(getResources().getDimension(R.dimen.chart_text_size));
        chart.setBackgroundColor(getResources().getColor(R.color.chartBackground));
        chart.getXAxis().setTextColor(getResources().getColor(R.color.chartTextColor));
        chart.getAxisLeft().setTextColor(getResources().getColor(R.color.boarders));
        chart.getAxisRight().setTextColor(getResources().getColor(R.color.red));

       refresh();

        //Set textViews
        TextView bestWeightTV = findViewById(R.id.stats_best_weight_text_view);
        TextView bestVolumeTV =  findViewById(R.id.stats_best_volume_text_view);
        PersonalRecord weightPR = DatabaseManager.getPersonalRecordWeight(exerciseName);
        PersonalRecord volumePR = DatabaseManager.getPersonalRecordVolume(exerciseName);

        String bestWeight;
        String bestVolume;
        if (weightPR.getWeight() != 0) {
            bestWeight = String.format(l, "%s kg × %d",
                    Formatter.formatFloat(weightPR.getWeight()),
                    weightPR.getReps());
        }
        else {
            bestWeight = String.format(l, "%d %s",
                    weightPR.getReps(), getString(R.string.reps__));
        }

        if (volumePR.getWeight() != 0) {
            bestVolume = String.format(l, "%s kg × %d",
                    Formatter.formatFloat(volumePR.getWeight()),
                    volumePR.getReps());
        }
        else {
            bestVolume = String.format(l, "%d %s",
                    volumePR.getReps(),  getString(R.string.reps__));
        }

        bestWeightTV.setText(String.format(
                "%s am %s", bestWeight, Formatter.formatDate(weightPR.getDate())));
        bestVolumeTV.setText(String.format(
                "%s am %s", bestVolume, Formatter.formatDate(volumePR.getDate())));

        //fill exercise history
        LinearLayout historyContainer = findViewById(R.id.exercise_history_linear_layout);
        for (ExerciseEntry entry : history.getEntries()) {
            HistoryExerciseFragment historyExerciseFragment =
                    HistoryExerciseFragment.newInstance(entry);
            FragmentContainerView newContainer = new FragmentContainerView(this);
            newContainer.setId(View.generateViewId());
            getSupportFragmentManager().beginTransaction()
                    .add(newContainer.getId(), historyExerciseFragment).commit();
            historyContainer.addView(newContainer);
        }

        EditText daysToCalcET = findViewById(R.id.days_to_calc_edit_text);
        TextWatcher refreshChartTW = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                refresh();

            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
        daysToCalcET.addTextChangedListener(refreshChartTW);

    }

    public void refresh() {
        LineChart chart = findViewById(R.id.exercise_volume_chart);
        EditText daysToCalcET = findViewById(R.id.days_to_calc_edit_text);
        String daysToCalString = daysToCalcET.getText().toString();
        if (daysToCalString.equals(""))
            return;

        //Get data
        ExerciseHistory history = DatabaseManager.getExerciseHistory(exerciseName);
        if (history == null) {
            return;
        }

        //set value formatter
        chart.getXAxis().setValueFormatter(new DateFormatterXAxis());

        //create lists with data entries
        List<Entry> entriesVolume = new ArrayList<>();
        List<Entry> entriesWeight = new ArrayList<>();
        for (ExerciseEntry entry : history.getEntries()) {
            long timestamp = Formatter.convertDateToUnixTimestampSeconds(entry.getDate());
            entriesVolume.add(new Entry(timestamp, entry.getVolume(Integer.parseInt(daysToCalString))));
            entriesWeight.add(new Entry(timestamp, entry.getMaxWeight()));
        }

        //this fixes a common bug in the library
        Collections.sort(entriesVolume, new EntryXComparator());
        Collections.sort(entriesWeight, new EntryXComparator());

        //add data to set
        LineDataSet dataSetVolume = new LineDataSet(entriesVolume, getString(R.string.volumeOfFirst) + " " + daysToCalString + " " + getString(R.string.saetze));
        dataSetVolume.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetVolume.setColor(getResources().getColor(R.color.boarders));
        dataSetVolume.setDrawCircles(false);
        dataSetVolume.setDrawValues(false);
        dataSetVolume.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));

        LineDataSet dataSetWeight = new LineDataSet(entriesWeight, getString(R.string.maxWeight));
        dataSetWeight.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSetWeight.setColor(getResources().getColor(R.color.red));
        dataSetWeight.setDrawCircles(false);
        dataSetWeight.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));
        dataSetWeight.setValueTextColor(getResources().getColor(R.color.red));
        dataSetWeight.setValueTextSize(getResources().getDimension(R.dimen.value_chart_text));

        LineData lineData = new LineData(dataSetVolume, dataSetWeight);
        chart.setData(lineData);

        //refresh
        chart.invalidate();
    }
}