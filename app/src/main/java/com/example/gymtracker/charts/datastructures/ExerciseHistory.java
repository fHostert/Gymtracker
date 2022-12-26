package com.example.gymtracker.charts.datastructures;

import java.util.ArrayList;

public class ExerciseHistory {
    private int ID;
    private String name;
    private ArrayList<ExerciseEntry> entries;

    public ExerciseHistory(int id, String name, ArrayList<ExerciseEntry> entries) {
        ID = id;
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
