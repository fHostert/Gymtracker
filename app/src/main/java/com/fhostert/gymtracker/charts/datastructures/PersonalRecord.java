package com.fhostert.gymtracker.charts.datastructures;

public class PersonalRecord {
    private final String date;
    private final String exercise;
    private final float weight;
    private final int repetitions;

    public PersonalRecord(String date, String exercise, float weight, int repetitions) {
        this.date = date;
        this.exercise = exercise;
        this.weight = weight;
        this.repetitions = repetitions;
    }

    public String getDate() {
        return date;
    }

    public String getExercise() {
        return exercise;
    }

    public float getWeight() {
        return weight;
    }

    public int getReps() {
        return repetitions;
    }
}
