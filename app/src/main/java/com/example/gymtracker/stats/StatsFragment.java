package com.example.gymtracker.stats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StatsFragment extends Fragment {

    public StatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatsFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        LineChart chart = (LineChart) view.findViewById(R.id.trainings_duration_chart);
        chart.getXAxis().setValueFormatter(new DateFormatterXAxis());

        ArrayList<WorkoutEntry> history = DatabaseManager.getWorkoutEntries();
        if (history == null) {
            return view;
        }

        List<Entry> entries = new ArrayList<>();
        for (WorkoutEntry entry : history) {
            long timestamp = Formatter.convertDateToUnixTimestampSeconds(entry.getDate());
            float durationInMinutes = (float) entry.getDuration() / 60;
            entries.add(new Entry(timestamp, durationInMinutes));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getXAxis().setLabelRotationAngle(90);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.invalidate(); // refresh







        return view;
    }
}