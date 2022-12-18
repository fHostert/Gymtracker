package com.example.gymtracker.datastructures;

import java.io.Serializable;
import java.util.ArrayList;

public class Workout implements Serializable {
    private final String name;
    private final int duration;
    private final String date;

    private final ArrayList<Exercise> exercises;

    public Workout(String name, ArrayList<Exercise> exercises) {
        this.name = name;
        this.duration = -1;
        this.date = "";
        this.exercises = exercises;
    }

    public Workout(String name, int duration, String date, ArrayList<Exercise> exercises) {
        this.name = name;
        this.duration = duration;
        this.date = date;
        this.exercises = exercises;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }
}
