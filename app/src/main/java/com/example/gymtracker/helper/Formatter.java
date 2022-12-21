package com.example.gymtracker.helper;

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
}
