package com.fhostert.gymtracker.charts.datastructures;

import com.fhostert.gymtracker.datastructures.Set;

import java.io.Serializable;
import java.util.ArrayList;

public class ExerciseEntry  implements Serializable {
    private final ArrayList<Set> sets;
    private final String date;
    private final String note;

    public ExerciseEntry(ArrayList<Set> sets, String date, String note) {
        this.sets = sets;
        this.date = date;
        this.note = note;
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

    public float getVolume(){
        return getVolume(-1);
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

    public String getNote() {
        return note;
    }
}
