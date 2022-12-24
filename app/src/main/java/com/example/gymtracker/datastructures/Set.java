package com.example.gymtracker.datastructures;

import java.io.Serializable;

public class Set implements Serializable {
    private final int index;
    private int reps;
    private float weight;
    private final boolean isPersonalRecord;

    public Set(int index) {
        this.index = index;
        this.reps = 0;
        this.weight = 0;
        this.isPersonalRecord = false;
    }

    public Set(int index, int reps, float weight) {
        this.index = index;
        this.reps = reps;
        this.weight = weight;
        this.isPersonalRecord = false;
    }

    public Set(int index, int reps, float weight, boolean isPersonalRecord) {
        this.index = index;
        this.reps = reps;
        this.weight = weight;
        this.isPersonalRecord = isPersonalRecord;
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

    public boolean isPersonalRecord() {
        return isPersonalRecord;
    }
}
