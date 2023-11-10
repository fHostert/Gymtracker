package com.fhostert.gymtracker.charts.ChartFormatter;


import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class DateFormatterXAxis extends IndexAxisValueFormatter {

    //This class converts seconds to date so that the date string can be displayed on the X-Axis
    @Override
    public String getFormattedValue(float value) {
        // Convert float value to date string
        // Convert from seconds back to milliseconds to format time  to show to the user
        long emissionsMilliSince1970Time = ((long) value) * 1000;

        // Show time in local version
        Date timeMilliseconds = new Date(emissionsMilliSince1970Time);
        DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        return dateTimeFormat.format(timeMilliseconds);
    }
}
