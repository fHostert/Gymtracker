package com.example.gymtracker.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.gymtracker.R;
import com.example.gymtracker.charts.datastructures.ExerciseEntry;
import com.example.gymtracker.charts.datastructures.ExerciseHistory;
import com.example.gymtracker.charts.datastructures.PersonalRecord;
import com.example.gymtracker.charts.datastructures.WorkoutEntry;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.History;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Settings;
import com.example.gymtracker.datastructures.Workout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;


/*
* The Database Schema can be found in the Documentation Folder.
*/
public final class DatabaseManager {
    private static SQLiteDatabase db;
    private static final Locale l = Locale.GERMAN;

    private DatabaseManager () {
    }

    public static void initialize(SQLiteDatabase dbPara) {
        db = dbPara;

        //Update older versions
        try
        {
            String query = "ALTER TABLE Templates ADD COLUMN position INT";
            db.execSQL(query);
            //query = "DROP TABLE CurrentWorkout";
            //db.execSQL(query);
            //query = "DROP TABLE CurrentWorkoutMetadata";
            //db.execSQL(query);
        }
        catch (Exception ignored)
        {

        }

    }

    /*##############################################################################################
    #########################################CURRENTWORKOUT#########################################
    ##############################################################################################*/
    public static void createCurrentWorkoutTable() {
        String query = "CREATE TABLE IF NOT EXISTS CurrentWorkout" +
                        "(exerciseID INT, position INT, setIndex INT, reps INT, weight REAL, " +
                        "note VARCHAR DEFAULT '');";
        db.execSQL(query);
    }

    public static void insertSetIntoCurrentWorkout(int exerciseID, int position, Set set) {
        String note = "";
        if (set.getIndex() == 1) {
            note = getLastNote(exerciseID);
        }
        String query = String.format(l,
                "INSERT INTO CurrentWorkout VALUES " +
                        "(%d, %d, %d, %d, '%s', '%s')",
                        exerciseID, position, set.getIndex(), set.getReps(),
                        Formatter.formatFloat(set.getWeight()), note);
        db.execSQL(query);
    }

    public static void addSetToExerciseInCurrentWorkout(int exerciseID, Set set) {
        String query = String.format(l,
                "SELECT position FROM CurrentWorkout " +
                        "WHERE exerciseID = %d;", exerciseID);
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        int position = rs.getInt(0);
        rs.close();

        insertSetIntoCurrentWorkout(exerciseID, position, set);
    }

    public static void updateSet(int exerciseID, Set set) {
        String query = String.format(l,
                "UPDATE CurrentWorkout " +
                        "SET reps = %d, weight = '%s' " +
                        "WHERE exerciseID = %d AND setIndex = %d;",
                        set.getReps(), Formatter.formatFloat(set.getWeight()),
                        exerciseID, set.getIndex());
        db.execSQL(query);
    }

    public static void deleteExerciseFromCurrentWorkout(int exerciseID) {
        //find position of exercise
        String query = String.format(l,
                "SELECT position FROM CurrentWorkout WHERE exerciseID = %d;",
                        exerciseID);
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        int oldPosition = rs.getInt(0);
        rs.close();

        //delete the exercise
        query = String.format(l,
                "DELETE FROM CurrentWorkout WHERE exerciseID = %d;",
                        exerciseID);
        db.execSQL(query);

        //update all exercises that are positioned after the deleted one
        query = String.format(l,
                "UPDATE CurrentWorkout SET position = position - 1 " +
                        "WHERE position > %d;",
                        oldPosition);
        db.execSQL(query);
    }

    public static String[] getExercisesInCurrentWorkout() {
        String query = "SELECT DISTINCT name FROM CurrentWorkout, Exercises WHERE exerciseID = ID" +
                        " ORDER BY position ASC;";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        String[] erg = new String[resultSet.getCount()];
        if (resultSet.getCount() == 0) {
            return erg;
        }
        for (int i = 0; i < resultSet.getCount(); i++) {
            erg[i] = resultSet.getString(0);
            resultSet.moveToNext();
        }
        resultSet.close();
        return erg;
    }

    public static void replaceExercise(int indexOfNewExercise, int indexOfOldExercise,
                                       int positionInWorkout) {
        for (int i = 1; i < 4; i++) {
            insertSetIntoCurrentWorkout(indexOfNewExercise, positionInWorkout,
                    new Set(i, 0, 0));
        }

        //Delete all but 3 sets
        String query = String.format(l,
                "DELETE FROM CurrentWorkout WHERE exerciseID = %d;",
                        indexOfOldExercise);
        db.execSQL(query);
    }

    public static void removeSet(int exerciseID, int lastIndex) {
        String query = String.format(l,
                "DELETE FROM CurrentWorkout " +
                        "WHERE exerciseID = %d and setIndex = %d;",
                        exerciseID, lastIndex);
        db.execSQL(query);
    }

    public static boolean arePreviousSetsDone(int exerciseID, int setIndex) {
        String query = String.format(l,
                "SELECT reps FROM CurrentWorkout " +
                        "WHERE exerciseID = %d AND setIndex < %d ORDER BY reps ASC;",
                        exerciseID, setIndex);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        boolean erg = resultSet.getCount() == 0 || resultSet.getInt(0) != 0;
        resultSet.close();
        return erg;
    }

    public static void moveExerciseUp(int exerciseID, int oldPosition) {
        String query = String.format(l,
                "UPDATE CurrentWorkout SET position = position + 1 " +
                        "WHERE position = %d;",
                    oldPosition - 1);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE CurrentWorkout SET position = position - 1 " +
                        "WHERE exerciseID = %d;",
                        exerciseID);
        db.execSQL(query);
    }

    public static void moveExerciseDown(int exerciseID, int oldPosition) {
        String query = String.format(l,
                "UPDATE CurrentWorkout SET position = position - 1 " +
                        "WHERE position = %d;",
                oldPosition + 1);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE CurrentWorkout SET position = position + 1 " +
                        "WHERE exerciseID = %d;",
                        exerciseID);
        db.execSQL(query);
    }

    public static boolean saveCurrentWorkout() {
        if (isCurrentWorkoutEmpty()) {
            return false;
        }

        //Get metadata
        int workoutID = getNextWorkoutID();
        String workoutName = getCurrentWorkoutName();
        long duration = (System.currentTimeMillis() - getCurrentWorkoutStartTime()) / 1000;
        String currentDate = getCurrentWorkoutDate();
        float totalWeight = getTotalWeightOfCurrentWorkout();
        int numberOfPRs = 0;

        //Get distinct exercise IDs
        String query = "SELECT DISTINCT exerciseID FROM CurrentWorkout WHERE reps <> 0 " +
                        "ORDER BY position ASC;";
        Cursor resultSet = db.rawQuery(query, null);
        int numberOfExercises = resultSet.getCount();
        int[] exercisesIDs = new int[numberOfExercises];
        resultSet.moveToFirst();
        for (int i = 0; i < numberOfExercises; i++) {
            exercisesIDs[i] = resultSet.getInt(0);
            resultSet.moveToNext();
        }
        resultSet.close();

        //Loop through exercises
        for (int currentExerciseID : exercisesIDs) {

            //Get sets
            query = String.format(l,
                    "SELECT reps, weight, setIndex, note FROM CurrentWorkout " +
                            "WHERE exerciseID = %d AND reps <> 0 ORDER BY setIndex ASC;",
                            currentExerciseID);
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();

            //Current personal records, including the current workout
            Set volumeSet = getVolumePRSet(currentExerciseID);
            Set weightSet = getWeightPRSet(currentExerciseID);

            //Loop through sets and insert
            for (int i = 0; i < resultSet.getCount(); i++) {
                String note = "";
                if (i == 0) {
                    note = resultSet.getString(3);
                }
                int reps = resultSet.getInt(0);
                String weight = Formatter.formatFloat(resultSet.getFloat(1));

                //tendency
                int tendency = - 1;
                Set lastSet = getLastSet(currentExerciseID, i + 1);
                float volume = resultSet.getInt(0) * resultSet.getFloat(1);
                if (lastSet == null || lastSet.getVolume() < volume) {
                    tendency = 1;
                }
                else if (lastSet.getVolume() == volume) {
                    tendency = 0;
                }

                //isPR
                int isPR = 0;
                if (volumeSet == null || weightSet == null || volume > volumeSet.getVolume()
                        || resultSet.getFloat(1) > weightSet.getWeight() ||
                        (volumeSet.isPR() & volumeSet.getIndex() == resultSet.getInt(2)) ||
                        (weightSet.isPR() & weightSet.getIndex() == resultSet.getInt(2))) {
                    isPR = 1;
                    numberOfPRs++;
                }

                //insert into history
                query = String.format(l,
                        "INSERT INTO History " +
                        "(workoutID, exerciseID, setIndex, reps, weight, tendency, isPR, note) " +
                        "VALUES (%d, %d, %d, %d, '%s', %d, %d, '%s');",
                        workoutID, currentExerciseID, i + 1, reps, weight, tendency, isPR,
                        note);
                db.execSQL(query);
                resultSet.moveToNext();
            }
            resultSet.close();
        }

        //Insert into workouts
        query = String.format(l,
                "INSERT INTO Workouts VALUES (%d, '%s', %d, '%s', '%s', %d)",
                        workoutID, workoutName, duration, currentDate,
                        Formatter.formatFloat(totalWeight), numberOfPRs);
        db.execSQL(query);

        dropTable("CurrentWorkout");
        dropTable("CurrentWorkoutMetadata");

        return true;
    }

    public static Boolean isCurrentWorkoutEmpty() {
        String query = "SELECT reps FROM CurrentWorkout ORDER BY reps DESC";
        Cursor resultSet = db.rawQuery(query, null);
        boolean erg = resultSet.getCount() == 0;
        if (!erg)
        {
            resultSet.moveToFirst();
            erg = resultSet.getInt(0) == 0;
        }
        resultSet.close();
        return erg;
    }

    public static Workout getCurrentWorkout() {
        if (!doesTableExist("CurrentWorkout")) {
            return null;
        }

        //Get exercise IDs
        String query = "SELECT DISTINCT exerciseID FROM CurrentWorkout ORDER BY position ASC;";
        Cursor resultSet = db.rawQuery(query, null);
        int numberOfExercises = resultSet.getCount();
        int[] exercisesIDs = new int[numberOfExercises];
        resultSet.moveToFirst();
        for (int i = 0; i < numberOfExercises; i++) {
            exercisesIDs[i] = resultSet.getInt(0);
            resultSet.moveToNext();
        }

        //create Workout
        ArrayList<Exercise> exercises = new ArrayList<>();
        for (int i = 0; i < numberOfExercises; i++) {
            ArrayList<Set> sets = new ArrayList<>();
            query = String.format(l,
                    "SELECT setIndex, reps, weight, note FROM CurrentWorkout " +
                            "WHERE exerciseID = %d ORDER BY position ASC, setIndex ASC;",
                            exercisesIDs[i]);
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();
            String note = resultSet.getString(3);
            do {
                sets.add(new Set(resultSet.getInt(0), resultSet.getInt(1),
                        resultSet.getFloat(2)));
            } while (resultSet.moveToNext());
            exercises.add(new Exercise(exercisesIDs[i], sets, note));
        }
        resultSet.close();

        return new Workout(getCurrentWorkoutName(), exercises);
    }

    public static float getTotalWeightOfCurrentWorkout() {
        String query = "SELECT (weight * reps) FROM CurrentWorkout;";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() == 0) {
            return 0;
        }
        float totalWeight = 0;
        do {
            totalWeight += resultSet.getFloat(0);
        }
        while (resultSet.moveToNext());
        resultSet.close();
        return totalWeight;
    }

    public static void addNoteToExercise(String note, int exerciseID){
        String query = String.format(l,
                "UPDATE CurrentWorkout " +
                        "SET note = '%s' " +
                        "WHERE exerciseID = %d AND setIndex = 1;",
                        note, exerciseID);
        db.execSQL(query);
    }

    public static void deleteNoteFromExercise(int exerciseID){
        String query = String.format(l,
                "UPDATE CurrentWorkout " +
                        "SET note = '' " +
                        "WHERE exerciseID = %d AND setIndex = 1;",
                        exerciseID);
        db.execSQL(query);
    }

    public static String getCurrentNote(int exerciseID){
        String query = String.format(l,
                "SELECT note FROM CurrentWorkout " +
                        "WHERE exerciseID = %d AND setIndex = 1;",
                        exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        if (resultSet.getCount() == 0){
            resultSet.close();
            return "";
        }
        resultSet.moveToFirst();
        String erg = resultSet.getString(0);
        resultSet.close();
        return erg;
    }

    /*##############################################################################################
    #####################################CURRENTWORKOUTMETADATA#####################################
    ##############################################################################################*/
    public static void createCurrentWorkoutMetadataTable() {
        String query = "CREATE TABLE IF NOT EXISTS CurrentWorkoutMetadata" +
                "(workoutName VARCHAR, startTime INT, date VARCHAR, " +
                "timerStart BIGINT, timerEnd BIGINT," +
                "timerIsActive INT);";
        db.execSQL(query);
    }

    public static void setCurrentWorkoutMetadata(String workoutName) {
        long startTime = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());
        String query = String.format(l,
                "INSERT INTO CurrentWorkoutMetadata VALUES ('%s', %d, '%s', -1, -1, 0);",
                workoutName, startTime, date);
        db.execSQL(query);
    }

    public static void changeCurrentWorkoutName(String newName) {
        String query = String.format(
                "UPDATE CurrentWorkoutMetadata SET workoutName = '%s';", newName);
        db.execSQL(query);
    }

    public static String getCurrentWorkoutName() {
        String query = "SELECT workoutName FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        String name = rs.getString(0);
        rs.close();
        return name;
    }

    private static long getCurrentWorkoutStartTime() {
        String query = "SELECT startTime FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        long startTime = rs.getLong(0);
        rs.close();
        return startTime;
    }

    private static String getCurrentWorkoutDate() {
        String query = "SELECT date FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        String workoutDate = rs.getString(0);
        rs.close();
        return workoutDate;
    }

    public static long getCurrentWorkoutTimerStart() {
        String query = "SELECT timerStart FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        long value = rs.getLong(0);
        rs.close();
        return value;
    }

    public static long getCurrentWorkoutTimerEnd() {
        String query = "SELECT timerEnd FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        long value = rs.getLong(0);
        rs.close();
        return value;
    }

    public static void setCurrentWorkoutTimerStart(long value) {
        String query = String.format(l,
                "UPDATE CurrentWorkoutMetadata SET timerStart = %d;", value);
        db.execSQL(query);
    }

    public static void setCurrentWorkoutTimerEnd(long value) {
        String query = String.format(l,
                "UPDATE CurrentWorkoutMetadata SET timerEnd = %d;", value);
        db.execSQL(query);
    }

    public static boolean getCurrentWorkoutTimerIsActive() {
        String query = "SELECT timerIsActive FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        int value = rs.getInt(0);
        rs.close();
        return value != 0;
    }

    public static void setCurrentWorkoutTimerIsActive(boolean value) {
        if (!doesTableExist("CurrentWorkoutMetadata")){
            return;
        }
        int i = 0;
        if (value)
            i = 1;
        String query = String.format(l,
                "UPDATE CurrentWorkoutMetadata SET timerIsActive = %d;", i);
        db.execSQL(query);
    }


    /*##############################################################################################
    ############################################WORKOUTS############################################
    ##############################################################################################*/
    public static void createWorkoutsTable() {
        String query = "CREATE TABLE IF NOT EXISTS Workouts(" +
                "ID INT, name VARCHAR, duration INT, date VARCHAR, totalWeight REAL, " +
                "numberOfPRs INT);";
        db.execSQL(query);
    }

    private static int getNextWorkoutID() {
        String query = "SELECT MAX(ID) FROM Workouts";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        int nextID = resultSet.getInt(0) + 1;
        resultSet.close();
        return nextID;
    }

    private static int[] getWorkoutIDsForExercise(int exerciseID) {
        String query = String.format(l,
                "SELECT DISTINCT ID FROM Workouts, History " +
                    "WHERE Workouts.ID = History.workoutID AND exerciseID = %d " +
                    "ORDER BY DATE DESC;", exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        int numberOfIDs = resultSet.getCount();
        int[] IDs = new int[numberOfIDs];
        resultSet.moveToFirst();
        for (int i = 0; i < numberOfIDs; i++) {
            IDs[i] = resultSet.getInt(0);
            resultSet.moveToNext();
        }
        resultSet.close();
        return IDs;
    }

    /*##############################################################################################
    #############################################HISTORY############################################
    ##############################################################################################*/
    public static void createHistoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS History(" +
                "workoutID INT, exerciseID INT, setIndex INT, reps INT, weight REAL, " +
                "tendency INT, isPR INT, note VARCHAR DEFAULT '');";
        db.execSQL(query);
    }

    public static Set getLastSet(int exerciseID, int setIndex) {
        //this prevents sets from different workouts to load
        String query = String.format(l,
                "SELECT ID FROM History, Workouts " +
                        "WHERE History.workoutID = Workouts.ID " +
                        "AND exerciseID = %d " +
                        "AND setIndex = 1 " +
                        "ORDER BY date DESC LIMIT 1;",
                        exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() == 0){
            resultSet.close();
            return null;
        }
        int workoutIdOfLastWorkout = resultSet.getInt(0);

        //get last set
        query = String.format(l,
                "SELECT reps, weight, ID FROM History, Workouts " +
                        "WHERE History.workoutID = Workouts.ID " +
                        "AND ID = %d " +
                        "AND exerciseID = %d " +
                        "AND setIndex = %d " +
                        "ORDER BY date DESC LIMIT 1;",
                        workoutIdOfLastWorkout, exerciseID, setIndex);
        resultSet = db.rawQuery(query, null);
        if (resultSet.getCount() == 0){
            resultSet.close();
            return null;
        }
        resultSet.moveToFirst();
        Set erg = new Set(setIndex, resultSet.getInt(0), resultSet.getFloat(1));
        resultSet.close();
        return erg;
    }

    public static String getLastNote(int exerciseID){
        String query = String.format(l,
                "SELECT note FROM History, Workouts " +
                        "WHERE History.workoutID = Workouts.ID AND " +
                        "exerciseID = %d AND setIndex = 1 " +
                        "ORDER BY date DESC LIMIT 1;",
                exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        if (resultSet.getCount() == 0){
            resultSet.close();
            return "";
        }
        resultSet.moveToFirst();
        String erg = resultSet.getString(0);
        resultSet.close();
        return erg;
    }

    public static void deleteWorkoutFromHistory(int ID) {
        String query = String.format(l,
                "DELETE FROM History WHERE workoutID = %d",
                ID);
        db.execSQL(query);

        query = String.format(l,
                "DELETE FROM Workouts WHERE ID = %d",
                ID);
        db.execSQL(query);
    }

    public static History getHistory(int top, int offset) {
        //Get IDs of workouts to be returned
        String query = String.format(l,
                "SELECT DISTINCT ID FROM Workouts " +
                        "ORDER BY date DESC LIMIT %d OFFSET %d;",
                        top, offset);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        int[] workoutIDs = new int[resultSet.getCount()];
        for (int i = 0; i < resultSet.getCount(); i++) {
            workoutIDs[i] = resultSet.getInt(0);
            resultSet.moveToNext();
        }

        //Fill workout objects
        ArrayList<Workout> workouts = new ArrayList<>();
        for (int currentWorkoutID : workoutIDs) {
            //Get exercises in current workout
            query = String.format(l,
                    "SELECT DISTINCT exerciseID FROM History " +
                            "WHERE workoutID = %d ORDER BY setIndex ASC;",
                            currentWorkoutID);
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();
            int[] exerciseIDs = new int[resultSet.getCount()];
            for (int i = 0; i < resultSet.getCount(); i++) {
                exerciseIDs[i] = resultSet.getInt(0);
                resultSet.moveToNext();
            }
            resultSet.close();

            //Fill exercise objects for current workout
            ArrayList<Exercise> exercises = new ArrayList<>();
            for (int currentExerciseID : exerciseIDs) {
                query = String.format(l,
                        "SELECT reps, weight, tendency, isPR, note FROM History " +
                                "WHERE workoutID = %d AND exerciseID = %d " +
                                "ORDER BY setIndex ASC;",
                                currentWorkoutID, currentExerciseID);
                resultSet = db.rawQuery(query, null);
                resultSet.moveToFirst();

                //Fill sets objects for current exercise
                ArrayList<Set> sets = new ArrayList<>();
                String note = "";
                for (int i = 0; i < resultSet.getCount(); i++) {
                    if (i == 0){
                        note = resultSet.getString(4);
                    }
                    sets.add(new Set(i + 1, resultSet.getInt(0), resultSet.getFloat(1),
                            resultSet.getInt(2), resultSet.getInt(3) > 0));
                    resultSet.moveToNext();

                }
                exercises.add(new Exercise(currentExerciseID, sets, note));
            }

            //Get meta data
            query = String.format(l,
                    "SELECT name, duration, date, totalWeight, numberOfPRs, ID " +
                            "FROM Workouts WHERE ID = %d LIMIT 1",
                            currentWorkoutID);
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();

            //create new workout object
            workouts.add(new Workout(resultSet.getString(0), resultSet.getInt(1),
                    resultSet.getString(2), exercises, resultSet.getFloat(3),
                    resultSet.getInt(4), resultSet.getInt(5)));
        }
        resultSet.close();
        return new History(workouts);
    }

    private static Set getVolumePRSet(int exerciseID) {
        String query = String.format(l,
                "SELECT setIndex, reps, weight FROM History " +
                        "WHERE exerciseID = %d ORDER BY (reps * weight) DESC LIMIT 1;",
                        exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();

        //save values from history to compare to values from currentWorkout
        int setIndex;
        int maxReps;
        float maxWeight;
        boolean isInCurrentWorkout = false;
        if (resultSet.getCount() == 0) {
            resultSet.close();
            setIndex = 0;
            maxReps = 0;
            maxWeight = 0;
        }
        else {
            setIndex = resultSet.getInt(0);
            maxReps = resultSet.getInt(1);
            maxWeight = resultSet.getFloat(2);
        }
        query = String.format(l,
                "SELECT setIndex, reps, weight FROM CurrentWorkout " +
                        "WHERE exerciseID = %d ORDER BY (reps * weight) DESC LIMIT 1;",
                        exerciseID);
        resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();

        //exercise was never done
        if (resultSet.getCount() == 0 && setIndex == 0) {
            resultSet.close();
            return null;
        }

        //Current workout has the best set
        else if (resultSet.getCount() != 0 &&
                resultSet.getInt(1) * resultSet.getFloat(2) > maxReps * maxWeight) {
            setIndex = resultSet.getInt(0);
            maxReps = resultSet.getInt(1);
            maxWeight = resultSet.getFloat(2);
            isInCurrentWorkout = true;
        }

        Set erg = new Set(setIndex, maxReps, maxWeight, isInCurrentWorkout);
        resultSet.close();
        return erg;
    }

    private static Set getWeightPRSet(int exerciseID) {
        String query = String.format(l,
                "SELECT setIndex, reps, weight FROM History " +
                "WHERE exerciseID = %d ORDER BY weight DESC LIMIT 1;",
                exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();

        //save values from history to compare to values from currentWorkout
        int setIndex;
        int maxReps;
        float maxWeight;
        boolean isInCurrentWorkout = false;
        if (resultSet.getCount() == 0) {
            resultSet.close();
            setIndex = 0;
            maxReps = 0;
            maxWeight = 0;
        }
        else {
            setIndex = resultSet.getInt(0);
            maxReps = resultSet.getInt(1);
            maxWeight = resultSet.getFloat(2);
        }
        query = String.format(l,
                "SELECT setIndex, reps, weight FROM CurrentWorkout " +
                "WHERE exerciseID = %d ORDER BY weight DESC LIMIT 1;",
                exerciseID);
        resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();

        //exercise was never done
        if (resultSet.getCount() == 0 && setIndex == 0) {
            resultSet.close();
            return null;
        }

        //Current workout has the best set
        else if (resultSet.getCount() != 0 && resultSet.getFloat(2) > maxWeight) {
            setIndex = resultSet.getInt(0);
            maxReps = resultSet.getInt(1);
            maxWeight = resultSet.getFloat(2);
            isInCurrentWorkout = true;
        }

        Set erg = new Set(setIndex, maxReps, maxWeight, isInCurrentWorkout);
        resultSet.close();
        return erg;
    }



    /*##############################################################################################
    ############################################TEMPLATES###########################################
    ##############################################################################################*/
    public static void createTemplatesTable() {
        String query = "CREATE TABLE IF NOT EXISTS Templates(" +
                "name VARCHAR, exerciseID INT, numberOfSets INT, position INT);";
        db.execSQL(query);
    }

    public static void loadTemplate(String templateName) {
        //start workout
        createCurrentWorkoutMetadataTable();
        createCurrentWorkoutTable();
        setCurrentWorkoutMetadata(templateName);

        //insert all sets from template into current workout
        int position = 0;
        for (Exercise exercise : getExercisesInTemplate(templateName)) {
            for (int i = 0; i < exercise.getSets().size(); i++) {
                String note = "";
                if (i == 0) {
                    note = getLastNote(exercise.getExerciseID());
                }
                String query = String.format(l,
                        "INSERT INTO CurrentWorkout VALUES (%d, %d, %d, %d, '%s', '%s');",
                        exercise.getExerciseID(), position, i + 1,
                        exercise.getSets().get(i).getReps(),
                        Formatter.formatFloat(exercise.getSets().get(i).getWeight()),
                        note);
                db.execSQL(query);
            }
            position++;
        }
    }

    public static void saveTemplate(String name, ArrayList<String> exercises, int[] numberOfSets) {
        int position = 0;

        //get distinct names
        String query = "SELECT DISTINCT name FROM Templates";
        Cursor resultSet = db.rawQuery(query, null);
        String[] names = new String[resultSet.getCount()];
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            names[i] = resultSet.getString(0);
            resultSet.moveToNext();
        }

        //Get position if updating
        boolean templateExists = false;
        for(String s : names) {
            if (s.equals(name)){
                query = String.format(l,
                        "SELECT position FROM Templates WHERE name = '%s'", name);
                resultSet = db.rawQuery(query, null);
                resultSet.moveToFirst();
                position = resultSet.getInt(0) + 1;
                templateExists = true;
                break;
            }
        }

        if (!templateExists) {
            query = "SELECT MAX(position) FROM Templates";
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();
            if (resultSet.getCount() > 0) {
                position = resultSet.getInt(0) + 1;
            }
        }
        resultSet.close();

        query = String.format(l,
                "DELETE FROM Templates WHERE name = '%s'", name);
        db.execSQL(query);

        int counter = 0;
        for (String exerciseName : exercises) {
            query = String.format(l,
                    "INSERT INTO Templates VALUES ('%s', %d, %d, %d);",
                    name, getExerciseID(exerciseName), numberOfSets[counter], position);
            db.execSQL(query);
            counter++;
        }
    }

    public static Exercise[] getExercisesInTemplate(String templateName) {
        String query = String.format(
                        "SELECT exerciseID, numberOfSets FROM Templates " +
                        "WHERE name = '%s'",
                        templateName);
        Cursor resultSet = db.rawQuery(query, null);
        Exercise[] erg = new Exercise[resultSet.getCount()];
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            erg[i] = new Exercise(resultSet.getInt(0), resultSet.getInt(1));
            resultSet.moveToNext();
        }
        resultSet.close();
        return erg;
    }

    public static ArrayList<Workout> getTemplates() {
        //get distinct names
        String query = "SELECT DISTINCT name FROM Templates ORDER BY position ASC";
        Cursor resultSet = db.rawQuery(query, null);
        String[] names = new String[resultSet.getCount()];
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            names[i] = resultSet.getString(0);
            resultSet.moveToNext();
        }

        //fill workouts
        ArrayList<Workout> workouts = new ArrayList<>();
        for (String name : names) {
            query = String.format(
                    "SELECT exerciseID, numberOfSets FROM Templates " +
                    "WHERE name = '%s';",
                    name);
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();

            ArrayList<Exercise> exercises = new ArrayList<>();
            for (int i = 0; i < resultSet.getCount(); i++) {
                int exerciseID = resultSet.getInt(0);

                ArrayList<Set> sets = new ArrayList<>();
                for (int j = 0; j < resultSet.getInt(1); j++) {
                    sets.add(new Set(j + 1, 0, 0));
                }
                exercises.add(new Exercise(exerciseID, sets));
                resultSet.moveToNext();
            }
            workouts.add(new Workout(name, exercises));
        }

        resultSet.close();
        return workouts;
    }

    public static void deleteTemplate(String templateName) {
        int positionOfWorkout = 0;
        String query = String.format(
                "SELECT position FROM Templates WHERE name = '%s'", templateName);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() > 0) {
            positionOfWorkout = resultSet.getInt(0);
        }

        query = String.format(
                "DELETE FROM Templates WHERE name = '%s';",
                templateName);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Templates SET position = position - 1 WHERE position > %d;",
                positionOfWorkout);
        db.execSQL(query);
    }

    public static void renameTemplate(String templateName, String newTemplateName) {
        String query = String.format(l,
                "UPDATE Templates SET name = '%s' WHERE name = '%s';",
                newTemplateName, templateName);
        db.execSQL(query);
    }

    public static boolean doesTemplateExist(String name) {
        String query = String.format(
                "SELECT name FROM Templates WHERE name = '%s';",
                name);
        Cursor resultSet = db.rawQuery(query, null);
        boolean erg = resultSet.getCount() != 0;
        resultSet.close();
        return erg;
    }

    public static String getLastDateDoneTemplate(String templateName) {
        String query = String.format(
                "SELECT date FROM workouts WHERE name = '%s' " +
                "ORDER BY date desc LIMIT 1;",
                templateName);
        Cursor resultSet = db.rawQuery(query, null);
        String erg;
        if (resultSet.getCount() == 0) {
            erg = null;
        }
        else {
            resultSet.moveToFirst();
            erg = resultSet.getString(0);
        }
        resultSet.close();
        return erg;
    }

    public static boolean moveTemplateUp(String templateName) {
        int positionOfWorkout = 0;
        String query = String.format(
                "SELECT position FROM Templates WHERE name = '%s'", templateName);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() > 0) {
            positionOfWorkout = resultSet.getInt(0);
        }

        int minPosition = 0;
        query = "SELECT MIN(position) FROM Templates";
        resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() > 0) {
            minPosition = resultSet.getInt(0);
        }

        if(minPosition == positionOfWorkout) {
            return false;
        }

        query = String.format(l,
                "UPDATE Templates SET position = position + 1 " +
                        "WHERE position = %d;",
                positionOfWorkout - 1);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Templates SET position = position - 1 " +
                        "WHERE name = '%s'", templateName);
        db.execSQL(query);

        resultSet.close();
        return true;
    }

    public static boolean moveTemplateDown(String templateName) {
        int positionOfWorkout = 0;
        String query = String.format(
                "SELECT position FROM Templates WHERE name = '%s'", templateName);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() > 0) {
            positionOfWorkout = resultSet.getInt(0);
        }

        int maxPosition = 0;
        query = "SELECT MAX(position) FROM Templates";
        resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() > 0) {
            maxPosition = resultSet.getInt(0);
        }

        if(maxPosition == positionOfWorkout) {
            return false;
        }
        Log.d("TEMPLATELOG", "adfafdaffd");
        query = String.format(l,
                "UPDATE Templates SET position = position + 1 " +
                        "WHERE position = %d;",
                positionOfWorkout);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Templates SET position = position - 1 " +
                        "WHERE position = %d AND name <> '%s'", positionOfWorkout + 1, templateName);
        db.execSQL(query);

        resultSet.close();
        return true;
    }

    /*##############################################################################################
    ############################################EXERCISES###########################################
    ##############################################################################################*/
    public static void createExercisesTable(String[] exercises) {
        if (doesTableExist("Exercises")) {
            return;
        }
        String query = "CREATE TABLE IF NOT EXISTS Exercises(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR);";
        db.execSQL(query);

        //add basic exercises
        for (String s : exercises) {
            query = String.format(
                    "INSERT INTO Exercises (name) VALUES (\"%s\");",
                    s);
            db.execSQL(query);
        }
    }

    public static void createNewExercise(String newName) {
        String query = String.format(
                "INSERT INTO Exercises (name) VALUES ('%s');",
                newName);
        db.execSQL(query);
    }

    public static void deleteExercise(String exercise) {
        int exerciseID = getExerciseID(exercise);

        String query = String.format(l,
                "DELETE FROM Templates WHERE exerciseID = %d;",
                exerciseID);
        db.execSQL(query);

        query = String.format(l,
                "DELETE FROM Exercises WHERE ID = %d;",
                exerciseID);
        db.execSQL(query);

        //find workouts that only had this exercise in them and delete them
        query = String.format(l,
                "SELECT workoutID FROM (" +
                "SELECT workoutID, COUNT(exerciseID) AS distinctExercises " +
                "FROM History WHERE exerciseID = %d GROUP BY workoutID) " +
                "WHERE distinctExercises = 1;",
                exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            query = String.format(l, "DELETE FROM Workouts WHERE ID = %d;",
                    resultSet.getInt(0));
            db.execSQL(query);
        }
        resultSet.close();

        query = String.format(l,
                "DELETE FROM History WHERE exerciseID = %d;",
                exerciseID);
        db.execSQL(query);
    }

    public static void renameExercise(String exerciseName, String newExerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l,
                "UPDATE Exercises SET name = '%s' " +
                    "WHERE ID = %d;",
                    newExerciseName, exerciseID);
        db.execSQL(query);
    }

    public static void mergeExercises(String nameOfMainExercise, String nameOfOldExercise) {
        //TODO Handle the setIndexes when both exercises are in the same workout
        int newID = getExerciseID(nameOfMainExercise);
        int oldID = getExerciseID(nameOfOldExercise);
        String query = String.format(l,
                "UPDATE History SET exerciseID = %d " +
                "WHERE ID = %d;",
                newID, oldID);
        db.execSQL(query);

        deleteExercise(nameOfOldExercise);
    }

    public static String[] getExercises() {
        String query = "SELECT name FROM Exercises";
        Cursor resultSet = db.rawQuery(query, null);
        String[] erg = new String[resultSet.getCount()];
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            erg[i] = resultSet.getString(0);
            resultSet.moveToNext();
        }
        resultSet.close();
        return erg;
    }

    public static String[] getExercisesDoneAtLeastOnce() {
        String query = "SELECT name FROM Exercises";
        Cursor resultSet = db.rawQuery(query, null);
        ArrayList<String> arrayList = new ArrayList<>();
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            if (!wasExerciseNeverDone(resultSet.getString(0))) {
                arrayList.add(resultSet.getString(0));
            }
            resultSet.moveToNext();
        }
        resultSet.close();
        String[] erg = new String[arrayList.size()];
        erg = arrayList.toArray(erg);
        return erg;
    }

    public static int getExerciseID(String exerciseName) {
        String query = String.format(
                "SELECT ID FROM Exercises WHERE name = '%s';",
                exerciseName);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        int erg = resultSet.getInt(0);
        resultSet.close();
        return erg;
    }

    public static String getExerciseName(int ID) {
        String query = String.format(l,
                "SELECT name FROM Exercises WHERE ID = '%d';",
                ID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        String erg = resultSet.getString(0);
        resultSet.close();
        return erg;
    }

    public static boolean wasExerciseNeverDone(String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l,
                "SELECT * FROM History WHERE exerciseID = '%d';",
                        exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        boolean erg = resultSet.getCount() == 0;
        resultSet.close();
        return erg;
    }

    /*##############################################################################################
    ##############################################STATS#############################################
    ##############################################################################################*/
    public static ArrayList<WorkoutEntry> getWorkoutEntries(int daysToShow, int daysToAverageOver) {
        //Create an empty array for every day back to daysToShow + daysToAverageOver before today
        WorkoutEntry[] durationPerDay = new WorkoutEntry[daysToShow + daysToAverageOver];
        String today = String.valueOf(LocalDate.now());
        for (int i = 0; i < durationPerDay.length; i++) {
            durationPerDay[i] = new WorkoutEntry(
                    0, String.valueOf(LocalDate.now().minusDays(i)));
        }

        //add duration for each day in the array
        String query = "SELECT duration, date FROM Workouts ORDER BY date DESC";
        Cursor resultSet = db.rawQuery(query, null);
        if (resultSet.getCount() == 0) {
            return null;
        }
        resultSet.moveToFirst();
        do {
            String date = resultSet.getString(1).substring(0, 10);
            int dateDiffInDays = Formatter.getDateDiff(date, today);
            if (dateDiffInDays >= daysToShow + daysToAverageOver) {
                break;
            }
            durationPerDay[dateDiffInDays].addToDuration(resultSet.getInt(0));
        }
        while (resultSet.moveToNext());
        resultSet.close();

        //duration of the first day
        int duration = 0;
        for (int i = 0; i < daysToAverageOver; i++){
           duration += durationPerDay[i].getDuration();
        }

        //add duration for each day
        ArrayList<WorkoutEntry> entries = new ArrayList<>();
        for (int i = 0; i < daysToShow; i++){
            String currentDate = String.valueOf(LocalDate.now().minusDays(i));
            entries.add(new WorkoutEntry(duration, currentDate));

            //update duration for the next day
            duration -= durationPerDay[i].getDuration();
            duration += durationPerDay[i + daysToAverageOver].getDuration();
        }

        return entries;
    }

    public static ExerciseHistory getExerciseHistory(String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        int[] workoutIDs = getWorkoutIDsForExercise(exerciseID);

        if (workoutIDs.length == 0) {
            return null;
        }

        ArrayList<ExerciseEntry> entries = new ArrayList<>();
        for (int ID : workoutIDs) {
            String query = String.format(l,
                    "SELECT setIndex, reps, weight, tendency, isPR, date, note " +
                    "FROM History, Workouts WHERE History.WorkoutID = Workouts.ID " +
                    "AND exerciseID = %d AND ID = %d ORDER BY date DESC;",
                    exerciseID, ID);
            Cursor resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();
            ArrayList<Set> sets = new ArrayList<>();
            do {
                sets.add(new Set(
                        resultSet.getInt(0),
                        resultSet.getInt(1),
                        resultSet.getFloat(2),
                        resultSet.getInt(3),
                        resultSet.getInt(4) > 0));
            }
            while (resultSet.moveToNext());
            resultSet.moveToFirst();
            entries.add((new ExerciseEntry(
                        sets,
                        resultSet.getString(5),
                        resultSet.getString(6))));
            resultSet.close();
        }
        return new ExerciseHistory(exerciseID, exerciseName, entries);
    }

    public static PersonalRecord getPersonalRecordVolume(String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l,
                "SELECT weight, reps, date FROM History, Workouts " +
                "WHERE History.workoutID = Workouts.ID AND exerciseID = %d " +
                "ORDER BY (weight * reps) DESC LIMIT 1;",
                exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() == 0) {
            return null;
        }
        float weight = resultSet.getFloat(0);
        int reps = resultSet.getInt(1);
        String date = resultSet.getString(2);
        resultSet.close();
        return new PersonalRecord(date, exerciseName, weight, reps);
    }

    public static PersonalRecord getPersonalRecordWeight(String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l,
                "SELECT weight, reps, date FROM History, Workouts " +
                        "WHERE History.workoutID = Workouts.ID AND exerciseID = %d " +
                        "ORDER BY weight DESC LIMIT 1;",
                        exerciseID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() == 0) {
            return null;
        }
        float weight = resultSet.getFloat(0);
        int reps = resultSet.getInt(1);
        String date = resultSet.getString(2);
        resultSet.close();
        return new PersonalRecord(date, exerciseName, weight, reps);
    }

    public static int getWorkoutCount() {
        String query = "SELECT COUNT(ID) FROM Workouts";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        int erg = resultSet.getInt(0);
        resultSet.close();
        return erg;
    }

    public static int getTotalDuration() {
        String query = "SELECT SUM(duration) FROM Workouts";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() == 0) {
            return 0;
        }
        int erg = resultSet.getInt(0);
        resultSet.close();
        return erg;
    }

    public static float getTotalWeight() {
        String query = "SELECT SUM(totalWeight) FROM Workouts";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        if (resultSet.getCount() == 0) {
            return 0;
        }
        float erg = resultSet.getFloat(0);
        resultSet.close();
        return erg;
    }

    /*##############################################################################################
    ############################################SETTINGS###########################################
    ##############################################################################################*/
    public static void createSettingsTable() {
        boolean tableExists = doesTableExist("Settings");
        String query = "CREATE TABLE IF NOT EXISTS Settings(" +
                "timerDuration INT, timerAutoPlay INT, timerPlay3Seconds INT, timerPlay10Seconds INT, " +
                "timerVibrateAt3 INT, timerVibrateAt10 INT);";
        db.execSQL(query);

        if (!tableExists) {
            query = "INSERT INTO SETTINGS VALUES(120, 0, 1, 1, 1, 1);";
            db.execSQL(query);
        }
    }

    public static Settings getSettings() {
        String query = "SELECT * FROM Settings";
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();

        Settings settings = new Settings();
        settings.timerDuration = resultSet.getInt(0);
        settings.timerAutoPlay = resultSet.getInt(1) == 1;
        settings.timerPlay3Seconds = resultSet.getInt(2) == 1;
        settings.timerPlay10Seconds = resultSet.getInt(3) == 1;
        settings.timerVibrateAt10Seconds = resultSet.getInt(4) == 1;
        settings.timerVibrateAt3Seconds = resultSet.getInt(5) == 1;

        resultSet.close();
        return settings;
    }

    public static void setSettings(Settings settings) {
        String query = String.format(l,
                "UPDATE Settings SET timerDuration = %d;", settings.timerDuration);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Settings SET timerAutoPlay = %d;", settings.timerAutoPlay ? 1 : 0);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Settings SET timerPlay3Seconds = %d;", settings.timerPlay3Seconds ? 1 : 0);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Settings SET timerPlay10Seconds = %d;", settings.timerPlay10Seconds ? 1 : 0);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Settings SET timerVibrateAt3 = %d;", settings.timerVibrateAt3Seconds ? 1 : 0);
        db.execSQL(query);

        query = String.format(l,
                "UPDATE Settings SET timerVibrateAt10 = %d;", settings.timerVibrateAt10Seconds ? 1 : 0);
        db.execSQL(query);
    }

    /*##############################################################################################
    #############################################GENERAL############################################
    ##############################################################################################*/
    public static void dropTable(String table) {
        if (doesTableExist(table)) {
            String query = String.format("DROP TABLE \"%s\";", table);
            db.execSQL(query);
        }
    }

    public static void deleteFakeEntries() {
        String query = "SELECT ID FROM workouts WHERE name = 'Autogenerated';";
        Cursor resultSet = db.rawQuery(query, null);
        if (resultSet.getCount() == 0) {
            resultSet.close();
            return;
        }
        int[] workoutIDs = new int[resultSet.getCount()];
        resultSet.moveToFirst();
        for (int i = 0; i < resultSet.getCount(); i++) {
            workoutIDs[i] = resultSet.getInt(0);
            resultSet.moveToNext();
        }
        for (int id : workoutIDs) {
            query = String.format(l,
                    "DELETE FROM history WHERE workoutID = %d;",
                    id);
            db.execSQL(query);
        }
        query = "DELETE FROM workouts WHERE name = 'Autogenerated'";
        db.execSQL(query);
        resultSet.close();
    }

    public static boolean doesTableExist(String table) {
        String query = String.format(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='%s';",
                table);
        Cursor rs = db.rawQuery(query, null);
        boolean erg = rs.getCount() != 0;
        rs.close();
        return erg;
    }

    public static void fillWorkoutsTable(int numberOfFakeEntries) {
        String query = "SELECT name FROM workouts WHERE name = 'Autogenerated'";
        Cursor resultSet = db.rawQuery (query, null);
        if (resultSet.getCount() != 0) {
            resultSet.close();
            return;
        }
        String workoutName = "Autogenerated";
        float totalWeight = 0;
        int numberOfPRs = 0;
        for (int i = 0; i < numberOfFakeEntries; i++) {
            int workoutID = getNextWorkoutID();

            String date = LocalDate.now().minusDays(i) + " 00:00:00";
            int duration = ThreadLocalRandom.current().nextInt(3000, 4800);

            query = String.format(l,
                    "INSERT INTO Workouts VALUES (%d, '%s', %d, '%s', '%s', %d)",
                    workoutID, workoutName, duration, date,
                    Formatter.formatFloat(totalWeight), numberOfPRs);
            db.execSQL(query);
            for (int j = 1; j < 4; j++) {
                float weight = ThreadLocalRandom.current().nextInt(75, 85);
                int reps = ThreadLocalRandom.current().nextInt(8, 14);
                query = String.format(l,
                        "INSERT INTO History VALUES (%d, 1, %d, %d, %s, 0, 0, '')",
                        workoutID, j, reps, Formatter.formatFloat(weight));
                db.execSQL(query);
            }
        }
    }
}
