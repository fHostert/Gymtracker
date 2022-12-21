package com.example.gymtracker.datastructures;

import java.io.Serializable;
import java.util.ArrayList;

public class History implements Serializable {
    private ArrayList<Workout> workouts = new ArrayList<>();

    public History(ArrayList<Workout> workouts) {
        this.workouts = workouts;
    }

    private ArrayList<Workout> getWorkouts() {
        return workouts;
    }
}
