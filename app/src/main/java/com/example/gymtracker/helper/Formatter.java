package com.example.gymtracker.helper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Formatter {
    private Formatter (){
    }

    public static String formatFloat(Float f) {
        String erg;
        if (f % 1 == 0) {
            erg = String.valueOf(Math.round(f));
        }
        else {
            erg = (new DecimalFormat("0.00")).format(f);
            if (erg.substring(erg.length() - 1).equals("0")) {
                erg = erg.substring(0, erg.length() - 1);
            }
        }
        return erg.replaceAll(",", ".");
    }

    public static String formatTime(int timeInSeconds) {
        int timeInMinutes = timeInSeconds / 60;
        int days = timeInMinutes / 1440;
        int hours = (timeInMinutes % 1440) / 60;
        int minutes = ((timeInMinutes % 1440) % 60);
        String erg = "";
        if (days > 0) {
            erg += days;
            erg += "d";
            erg += " ";
        }
        if (hours > 0) {
            erg += hours;
            erg += "h";
            erg += " ";
        }
        erg += minutes;
        erg += "min";

        return erg;
    }

    /**
     * Changes format from  yyyy-MM-dd HH:mm:ss.SSS to DD.MM.YYYY
     */
    public static String formatDate(String date) {
        date = date.substring(0, 10);
        date = date.replace("-", ".");
        date = date.substring(8) + date.substring(4, 8) + date.substring(0, 4);
        return date;
    }

    public static String subtractDaysFromDate(String date, int daysToAdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, -daysToAdd);
        return sdf.format(c.getTime());
    }

    public static boolean isDateAfter(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        boolean erg = false;
        try {
             erg = sdf.parse(startDate).after(sdf.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return erg;
    }

    public static long convertDateToUnixTimestampSeconds(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime() / 1000;
    }
}
