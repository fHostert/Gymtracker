package com.example.gymtracker.datastructures;

import com.example.gymtracker.helper.Formatter;


import java.io.Serializable;
import java.util.Locale;

public class Set implements Serializable {
    private final int index;
    private int reps;
    private float weight;
    private final boolean isPersonalRecord;
    private final int tendency;

    private static final Locale l = Locale.GERMAN;

    public Set(int index) {
        this.index = index;
        this.reps = 0;
        this.weight = 0;
        this.isPersonalRecord = false;
        this.tendency = 0;
    }

    public Set(int index, int reps, float weight) {
        this.index = index;
        this.reps = reps;
        this.weight = weight;
        this.isPersonalRecord = false;
        this.tendency = 0;
    }

    public Set(int index, int reps, float weight, boolean isPersonalRecord) {
        this.index = index;
        this.reps = reps;
        this.weight = weight;
        this.isPersonalRecord = isPersonalRecord;
        this.tendency = 0;
    }

    public Set(int index, int reps, float weight, int tendency, boolean isPersonalRecord) {
        this.index = index;
        this.reps = reps;
        this.weight = weight;
        this.isPersonalRecord = isPersonalRecord;
        this.tendency = tendency;
    }

    public int getIndex() {
        return index;
    }

    public int getReps() {
        return reps;
    }

    public float getWeight() {
        return weight;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getVolume() {
        return reps * weight;
    }

    public String getSetString() {
        if (weight == 0) {
            return String.format(l, "%d Wdhl.", reps);
        }
        return String.format(l, "%d × %skg", reps, Formatter.formatFloat(weight));
    }

    public boolean isPR() {
        return isPersonalRecord;
    }

    public int getTendency() {
        return tendency;
    }
}
