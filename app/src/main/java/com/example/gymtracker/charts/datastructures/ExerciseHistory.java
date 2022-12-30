package com.example.gymtracker.charts.datastructures;

import java.io.Serializable;
import java.util.ArrayList;

public class ExerciseHistory implements Serializable {
    private final int ID;
    private final String name;
    private ArrayList<ExerciseEntry> entries;

    public ExerciseHistory(int ID, String name, ArrayList<ExerciseEntry> entries) {
        this.ID = ID;
        this.name = name;
        this.entries = entries;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ExerciseEntry> getEntries() {
        return entries;
    }
}
