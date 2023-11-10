package com.fhostert.gymtracker.helper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class Formatter {
    private Formatter (){
    }

    /**
     * @param f The float
     * @return A String in the pattern of 99.99 or 99.9.
     */
    public static String formatFloat(Float f) {
        String erg;
        if (f % 1 == 0) {
            erg = String.valueOf(Math.round(f));
        }
        else {
            erg = (new DecimalFormat("0.00")).format(f);
            if (erg.endsWith("0")) {
                erg = erg.substring(0, erg.length() - 1);
            }
        }
        return erg.replaceAll(",", ".");
    }

    /**
     * @param timeInSeconds The time in seconds
     * @return A String in the pattern of 1d 1h 1min.
     */
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

    /**
     * @param dateString date in form yyyy-MM-dd HH:mm:ss.SSS
     * @return Unix time stamp since that date
     */
    public static long convertDateToUnixTimestampSeconds(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime() / 1000;
    }

    /**
     * @param first first date
     * @param second second date
     * @return the time in days between both dates
     */
    public static int getDateDiff(String first, String second) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDate = LocalDate.parse(first, formatter);
        LocalDate secondDate = LocalDate.parse(second, formatter);
        long days = ChronoUnit.DAYS.between(firstDate, secondDate);
        return (int) days;

    }

    /**
     * @param tendency should be -1, 0 or 1
     * @return the corresponding emoji
     */
    public static String tendency(int tendency) {
        String erg = "➡";
        if (tendency < 0) {
            erg = "⬇";
        }
        else if (tendency > 0) {
            erg = "⬆";
        }
        return erg;
    }
}
