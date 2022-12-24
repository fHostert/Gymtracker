package com.example.gymtracker.datastructures;

import com.example.gymtracker.helper.DatabaseManager;
import com.example.gymtracker.helper.Formatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Workout implements Serializable {
    private final int ID;
    private String name;
    private final int duration;
    private final String date;
    private final float totalWeight;
    private final int numberOfPRs;

    private final ArrayList<Exercise> exercises;

    public Workout(String name) {
        this.name = name;
        this.duration = -1;
        this.date = "";
        this.totalWeight = -1;
        this.exercises = new ArrayList<>();
        this.numberOfPRs = 0;
        this.ID = -1;
    }

    public Workout(String name, ArrayList<Exercise> exercises) {
        this.name = name;
        this.duration = -1;
        this.date = "";
        this.totalWeight = -1;
        this.exercises = exercises;
        this.numberOfPRs = 0;
        this.ID = -1;
    }

    public Workout(String name, int duration, String date, ArrayList<Exercise> exercises,
                   float totalWeight, int numberOfPrs, int ID) {
        this.name = name;
        this.duration = duration;
        this.date = date;
        this.exercises = exercises;
        this.totalWeight = totalWeight;
        this.numberOfPRs = numberOfPrs;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getTotalWeight() {
        return totalWeight;
    }

    public int getNumberOfPRs() {
        return numberOfPRs;
    }

    public String[] getExerciseNames() {
        String[] exercisesInWorkout = new String[exercises.size()];
        for (int i = 0; i < exercises.size(); i++) {
            exercisesInWorkout[i] = exercises.get(i).getName();
        }
        return exercisesInWorkout;
    }

    public void removeExercise(String exerciseName) {
        for (int i = 0; i < exercises.size(); i++) {
            if (Objects.equals(exercises.get(i).getName(), exerciseName)) {
                exercises.remove(i);
                return;
            }
        }
    }

    public void addExercise(String exerciseName) {
        exercises.add(new Exercise(DatabaseManager.getExerciseID(exerciseName)));
    }
}
