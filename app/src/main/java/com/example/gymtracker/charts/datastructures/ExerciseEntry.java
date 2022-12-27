package com.example.gymtracker.charts.datastructures;


import android.util.Log;

import com.example.gymtracker.datastructures.Set;

import java.util.ArrayList;

public class ExerciseEntry {
    private final ArrayList<Set> sets;
    private final String date;

    public ExerciseEntry(ArrayList<Set> sets, String date) {
        this.sets = sets;
        this.date = date;
    }

    public ArrayList<Set> getSets() {
        return sets;
    }

    public String getDate() {
        return date;
    }

    public float getVolume(int limit){
        if (limit == -1 || limit > sets.size()) {
            limit = sets.size();
        }
        float volume = 0;
        for (int i = 0; i < limit; i++){
            volume += sets.get(i).getReps() * sets.get(i).getWeight();
        }
        return volume;
    }

    public float getMaxWeight() {
        float maxWeight = 0;
        for (int i = 0; i < sets.size(); i++){
            if (sets.get(i).getWeight() > maxWeight) {
                maxWeight = sets.get(i).getWeight();
            }
        }
        return maxWeight;
    }
}
