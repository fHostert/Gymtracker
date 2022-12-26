package com.example.gymtracker.charts.datastructures;


import com.example.gymtracker.datastructures.Set;

public class ExerciseEntry {
    private Set set;
    private String date;

    public ExerciseEntry(Set set, String date) {
        this.set = set;
        this.date = date;
    }

    public Set getSet() {
        return set;
    }

    public String getDate() {
        return date;
    }
}
