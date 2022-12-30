package com.example.gymtracker.charts.datastructures;

public class WorkoutEntry {
    private int duration;
    private final String date;

    public WorkoutEntry(int duration, String date) {
        this.duration = duration;
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public String getDate() {
        return date;
    }

    public void addToDuration(int duration) {
        this.duration += duration;
    }
}
