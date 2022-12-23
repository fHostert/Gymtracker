package com.example.gymtracker.datastructures;

import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class Exercise implements Serializable {

    private static final Locale l = Locale.GERMAN;

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

    public Exercise(int databaseIndex, int numberOfEmptySets) {
        this.databaseIndex = databaseIndex;
        this.name = DatabaseManager.getExerciseName(databaseIndex);
        for (int i = 0; i < numberOfEmptySets; i++) {
            sets.add(new Set(i + 1, 0, 0));
        }
    }

    public void addSet(Set set) {
        sets.add(set);
    }

    public void deleteLastSet() {
        sets.remove(sets.size() - 1);
    }

    public int getExerciseID() {
        return databaseIndex;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Set> getSets() {
        return sets;
    }

    public String getBestSetString(){
        float benchmark = -1;
        Set bestSet = null;
        int mostReps = 0;
        for (Set set : sets) {
            if (set.getReps() * set.getWeight() > benchmark) {
                benchmark = set.getReps() * set.getWeight();
                bestSet = set;
            }
            if (set.getReps() > mostReps) {
                mostReps = set.getReps();
            }
        }
        if (bestSet.getWeight() == 0)
        {
            return String.format(l, "%d Wdhl.", mostReps);
        }
        return String.format(l, "%d × %skg",
                bestSet.getReps(), Formatter.formatFloat(bestSet.getWeight()));
    }
}
