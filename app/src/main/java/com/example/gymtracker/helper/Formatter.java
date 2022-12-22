package com.example.gymtracker.helper;

import com.example.gymtracker.R;

import java.text.DecimalFormat;

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
}
