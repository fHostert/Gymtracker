package com.example.gymtracker;

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
        }
        return erg;
    }
}
