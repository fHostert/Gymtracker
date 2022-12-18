package com.example.gymtracker.datastructures;

import java.io.Serializable;

public class Set implements Serializable {
    private final int index;
    private final int reps;
    private final float weight;

    public Set(int index, int reps, float weight) {
        this.index = index;
        this.reps = reps;
        this.weight = weight;
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
}
