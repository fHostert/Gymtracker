package com.example.gymtracker.datastructures;

import java.io.Serializable;
import java.util.ArrayList;

public class History implements Serializable {
    private final ArrayList<Workout> workouts;

    public History(ArrayList<Workout> workouts) {
        this.workouts = workouts;
    }

    public ArrayList<Workout> getWorkouts() {
        return workouts;
    }
}
