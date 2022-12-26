package com.example.gymtracker.stats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

        //Initialize buttons
        Button refreshButton = view.findViewById(R.id.refresh_chart_button);
        refreshButton.setOnClickListener(view1 -> refreshDurationChart());

        return view;
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

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setHighlightEnabled(false);
        dataSet.setDrawHighlightIndicators(false);
        LineData lineData = new LineData(dataSet);
        lineData.setHighlightEnabled(false);
        chart.highlightValue(0, -1);
        chart.setData(lineData);
        chart.getXAxis().setLabelRotationAngle(90);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.invalidate(); // refresh

    }

    public void refreshDurationChart() {
        refreshDurationChart(getView());
    }
}