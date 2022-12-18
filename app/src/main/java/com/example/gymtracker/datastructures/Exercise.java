package com.example.gymtracker.datastructures;

import android.util.Log;

import com.example.gymtracker.DatabaseManager;

import java.io.Serializable;
import java.util.ArrayList;

public class Exercise implements Serializable {
    private final int databaseIndex;
    private final String name;
    private ArrayList<Set> sets = new ArrayList<>();

    public Exercise(int databaseIndex, ArrayList<Set> sets) {
        this.databaseIndex = databaseIndex;
        this.name = DatabaseManager.getExerciseName(databaseIndex);
        this.sets = sets;
    }

    public Exercise(int databaseIndex) {
        this.databaseIndex = databaseIndex;
        this.name = DatabaseManager.getExerciseName(databaseIndex);
        sets.add(new Set(1, 0, 0));
        sets.add(new Set(2, 0, 0));
        sets.add(new Set(3, 0, 0));
    }

    public int getDatabaseIndex() {
        return databaseIndex;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Set> getSets() {
        return sets;
    }
}
