package com.example.gymtracker.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.gymtracker.charts.datastructures.ExerciseEntry;
import com.example.gymtracker.charts.datastructures.ExerciseHistory;
import com.example.gymtracker.charts.datastructures.PersonalRecord;
import com.example.gymtracker.charts.datastructures.WorkoutEntry;
import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.History;
import com.example.gymtracker.datastructures.Set;
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
    }

    /*##############################################################################################
    #########################################CURRENTWORKOUT#########################################
    ##############################################################################################*/
    public static void createCurrentWorkoutTable() {
        String query = "CREATE TABLE IF NOT EXISTS CurrentWorkout" +
                        "(exerciseID INT, position INT, setIndex INT, reps INT, weight REAL);";
        db.execSQL(query);
    }

    /**
     * Called from workout fragment, where position of exercise is known.
     */
    public static void insertSetIntoCurrentWorkout(int exerciseID, int position, Set set) {
        String query = String.format(l, "INSERT INTO CurrentWorkout VALUES " +
                        "(%d, %d, %d, %d, '%s')",
                        exerciseID, position, set.getIndex(), set.getReps(),
                        Formatter.formatFloat(set.getWeight()));
        db.execSQL(query);
    }

    /**
     * Called from exercise fragment, where position of exercise is not known.
     */
    public static void insertSetIntoCurrentWorkout(int exerciseID, Set set) {
        String query = String.format(l, "SELECT position FROM CurrentWorkout " +
                        "WHERE exerciseID = %d;", exerciseID);
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        int position = rs.getInt(0);
        rs.close();

        insertSetIntoCurrentWorkout(exerciseID, position, set);
    }

    public static void updateSet(int exerciseID, Set set) {
        String query = String.format(l, "UPDATE CurrentWorkout " +
                        "SET reps = %d, weight = '%s' " +
                        "WHERE exerciseID = %d AND setIndex = %d;",
                        set.getReps(), Formatter.formatFloat(set.getWeight()),
                        exerciseID, set.getIndex());
        db.execSQL(query);
    }

    public static void deleteExerciseFromCurrentWorkout(int exerciseID) {
        //find position of exercise
        String query = String.format(l,
                "SELECT position FROM CurrentWorkout WHERE exerciseID = %d;", exerciseID);
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        int oldPosition = rs.getInt(0);
        rs.close();

        //delete the exercise
        query = String.format(l, "DELETE FROM CurrentWorkout WHERE exerciseID = %d;",
                exerciseID);
        db.execSQL(query);

        //update all exercises that are positioned after the deleted one
        query = String.format(l, "UPDATE CurrentWorkout SET position = position - 1 " +
                        "WHERE position > %d;",
                        oldPosition);
        db.execSQL(query);
    }

    public static void replaceExercise(int indexOfNewExercise, int indexOfOldExercise) {
        String query = String.format(l, "UPDATE CurrentWorkout " +
                        "SET exerciseID = %d, reps = 0, weight = 0 WHERE exerciseID = %d;",
                        indexOfNewExercise, indexOfOldExercise);
        db.execSQL(query);

        //Delete all but 3 sets
        query = String.format(l, "DELETE FROM CurrentWorkout " +
                        "WHERE exerciseID = %d AND setIndex > 3;",
                        indexOfNewExercise);
        db.execSQL(query);
    }

    public static void removeLastSet(int exerciseID, int lastIndex) {
        String query = String.format(l, "DELETE FROM CurrentWorkout " +
                        "WHERE exerciseID = %d and setIndex = %d;",
                        exerciseID, lastIndex);
        db.execSQL(query);
    }

    public static boolean arePreviousSetsDone(int exerciseID, int setIndex) {
        String query = String.format(l, "SELECT reps FROM CurrentWorkout " +
                        "WHERE exerciseID = %d AND setIndex < %d ORDER BY reps ASC;",
                        exerciseID, setIndex);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        boolean erg = resultSet.getCount() == 0 || resultSet.getInt(0) != 0;
        resultSet.close();
        return erg;
    }

    public static void moveExerciseUp(int exerciseID, int oldPosition) {
        String query = String.format(l, "UPDATE CurrentWorkout SET position = position + 1 " +
                        "WHERE position = %d;", oldPosition - 1);
        db.execSQL(query);

        query = String.format(l, "UPDATE CurrentWorkout SET position = position - 1 " +
                        "WHERE exerciseID = %d;", exerciseID);
        db.execSQL(query);
    }

    public static void moveExerciseDown(int exerciseID, int oldIndex) {
        String query = String.format(l, "UPDATE CurrentWorkout SET position = position - 1 " +
                        "WHERE position = %d;", oldIndex + 1);
        db.execSQL(query);

        query = String.format(l, "UPDATE CurrentWorkout SET position = position + 1 " +
                        "WHERE exerciseID = %d;", exerciseID);
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
            query = String.format(l, "SELECT reps, weight, setIndex FROM CurrentWorkout " +
                            "WHERE exerciseID = %d AND reps <> 0 ORDER BY setIndex ASC;",
                            currentExerciseID);
            resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();

            //Current personal records, including the current workout
            Set volumeSet = getVolumePRSet(currentExerciseID);
            Set weightSet = getWeightPRSet(currentExerciseID);

            //Loop through sets and insert
            for (int i = 0; i < resultSet.getCount(); i++) {
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
                query = String.format(l, "INSERT INTO History " +
                                "(workoutID, exerciseID, setIndex, reps, weight, tendency, isPR) " +
                                "VALUES (%d, %d, %d, %d, '%s', %d, %d);",
                                workoutID, currentExerciseID, i + 1, reps, weight, tendency, isPR);
                db.execSQL(query);
                resultSet.moveToNext();
            }
            resultSet.close();
        }

        //Insert into workouts
        query = String.format(l, "INSERT INTO Workouts VALUES (%d, '%s', %d, '%s', '%s', %d)",
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
        Cursor rs = db.rawQuery(query, null);
        int numberOfExercises = rs.getCount();
        int[] exercisesIDs = new int[numberOfExercises];
        rs.moveToFirst();
        for (int i = 0; i < numberOfExercises; i++) {
            exercisesIDs[i] = rs.getInt(0);
            rs.moveToNext();
        }

        //create Workout
        ArrayList<Exercise> exercises = new ArrayList<>();
        for (int i = 0; i < numberOfExercises; i++) {
            ArrayList<Set> sets = new ArrayList<>();
            query = String.format(l,
                    "SELECT setIndex, reps, weight FROM CurrentWorkout " +
                            "WHERE exerciseID = %d ORDER BY position ASC, setIndex ASC;",
                            exercisesIDs[i]);
            rs = db.rawQuery(query, null);
            rs.moveToFirst();
            do {
                sets.add(new Set(rs.getInt(0), rs.getInt(1), rs.getFloat(2)));
            } while (rs.moveToNext());
            exercises.add(new Exercise(exercisesIDs[i], sets));
        }
        rs.close();

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

    /*##############################################################################################
    #####################################CURRENTWORKOUTMETADATA#####################################
    ##############################################################################################*/
    public static void createCurrentWorkoutMetadataTable() {
        String query = "CREATE TABLE IF NOT EXISTS CurrentWorkoutMetadata" +
                "(workoutName VARCHAR, startTime INT, date VARCHAR);";
        db.execSQL(query);
    }

    public static void setCurrentWorkoutMetadata(String workoutName) {
        long startTime = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date());
        String query = String.format(l,
                "INSERT INTO CurrentWorkoutMetadata VALUES ('%s', %d, '%s');",
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

    /*##############################################################################################
    ############################################WORKOUTS############################################
    ##############################################################################################*/
    public static void createWorkoutsTable() {
        String query = "CREATE TABLE IF NOT EXISTS Workouts(" +
                "ID INT, name VARCHAR, duration INT, date VARCHAR, totalWeight REAL, numberOfPRs INT);";
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
        String query = String.format(l, "SELECT DISTINCT ID FROM Workouts, History " +
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
                "tendency INT, isPR INT);";
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

    public static void deleteWorkoutFromHistory(int ID) {
        String query = String.format(l, "DELETE FROM History WHERE workoutID = %d", ID);
        db.execSQL(query);

        query = String.format(l, "DELETE FROM Workouts WHERE ID = %d", ID);
        db.execSQL(query);
    }

    public static History getHistory(int top, int offset) {
        //Get IDs of workouts to be returned
        String query = String.format(l,
                "SELECT DISTINCT ID FROM Workouts " +
                        "ORDER BY date DESC LIMIT %d OFFSET %d;", top, offset);
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
                        "SELECT reps, weight, tendency, isPR FROM History " +
                                "WHERE workoutID = %d AND exerciseID = %d " +
                                "ORDER BY setIndex ASC;",
                                currentWorkoutID, currentExerciseID);
                resultSet = db.rawQuery(query, null);
                resultSet.moveToFirst();

                //Fill sets objects for current exercise
                ArrayList<Set> sets = new ArrayList<>();
                for (int i = 0; i < resultSet.getCount(); i++) {
                    sets.add(new Set(i + 1, resultSet.getInt(0), resultSet.getFloat(1),
                            resultSet.getInt(2), resultSet.getInt(3) > 0));
                    resultSet.moveToNext();

                }
                exercises.add(new Exercise(currentExerciseID, sets));
            }

            //Get meta data
            query = String.format(l,
                    "SELECT name, duration, date, totalWeight, numberOfPRs, ID FROM Workouts " +
                            "WHERE ID = %d LIMIT 1",
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
        String query = String.format(l, "SELECT setIndex, reps, weight FROM History " +
                "WHERE exerciseID = %d ORDER BY (reps * weight) DESC LIMIT 1;", exerciseID);
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
        query = String.format(l, "SELECT setIndex, reps, weight FROM CurrentWorkout " +
                "WHERE exerciseID = %d ORDER BY (reps * weight) DESC LIMIT 1;", exerciseID);
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
        String query = String.format(l, "SELECT setIndex, reps, weight FROM History " +
                "WHERE exerciseID = %d ORDER BY weight DESC LIMIT 1;", exerciseID);
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
        query = String.format(l, "SELECT setIndex, reps, weight FROM CurrentWorkout " +
                "WHERE exerciseID = %d ORDER BY weight DESC LIMIT 1;", exerciseID);
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
                "name VARCHAR, exerciseID INT, numberOfSets INT);";
        db.execSQL(query);
    }

    public static void loadTemplate(String templateName) {
        //start workout
        createCurrentWorkoutMetadataTable();
        createCurrentWorkoutTable();
        setCurrentWorkoutMetadata(templateName);

        //insert all sets from template into current workout
        int position = 1;
        for (Exercise exercise : getExercisesInTemplate(templateName)) {
            for (int i = 0; i < exercise.getSets().size(); i++) {
                String query = String.format(l,
                        "INSERT INTO CurrentWorkout VALUES (%d, %d, %d, %d, '%s');",
                        exercise.getExerciseID(), position, i + 1,
                        exercise.getSets().get(i).getReps(),
                        Formatter.formatFloat(exercise.getSets().get(i).getWeight()));
                db.execSQL(query);
            }
            position++;
        }
    }

    public static void saveTemplate(String name, ArrayList<String> exercises) {
        for (String exerciseName : exercises) {
            String query = String.format(l, "INSERT INTO Templates VALUES ('%s', %d, 3);",
                    name, getExerciseID(exerciseName));
            db.execSQL(query);
        }
    }

    public static Exercise[] getExercisesInTemplate(String templateName) {
        String query = String.format("SELECT exerciseID, numberOfSets FROM Templates " +
                        "WHERE name = '%s'", templateName);
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
        String query = "SELECT DISTINCT name FROM Templates";
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
            query = String.format("SELECT exerciseID, numberOfSets FROM Templates " +
                    "WHERE name = '%s';", name);
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
        String query = String.format("DELETE FROM Templates WHERE name = '%s';", templateName);
        db.execSQL(query);
    }

    public static void addExerciseToTemplate(String templateName, String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l, "INSERT INTO Templates VALUES " +
                "('%s', %d, 3);", templateName, exerciseID);
        db.execSQL(query);
    }

    public static void deleteExerciseFromTemplate(String templateName, String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l, "DELETE FROM Templates " +
                "WHERE name = '%s' AND exerciseID = %d;", templateName, exerciseID);
        db.execSQL(query);
    }

    public static void renameTemplate(String templateName, String newTemplateName) {
        String query = String.format(l, "UPDATE Templates SET name = '%s' " +
                        "WHERE name = '%s';", newTemplateName, templateName);
        db.execSQL(query);
    }

    public static boolean doesTemplateExist(String name) {
        String query = String.format("SELECT name FROM Templates WHERE name = '%s';", name);
        Cursor resultSet = db.rawQuery(query, null);
        boolean erg = resultSet.getCount() != 0;
        resultSet.close();
        return erg;
    }

    public static String getLastDateDoneTemplate(String templateName) {
        String query = String.format("SELECT date FROM workouts WHERE name = '%s' " +
                    "ORDER BY date desc LIMIT 1;", templateName);
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
            query = String.format("INSERT INTO Exercises (name) VALUES (\"%s\");", s);
            db.execSQL(query);
        }
    }

    public static void createNewExercise(String newName) {
        String query = String.format("INSERT INTO Exercises (name) VALUES ('%s');", newName);
        db.execSQL(query);
    }

    public static void deleteExercise(String exercise) {
        int exerciseID = getExerciseID(exercise);

        String query = String.format(l,
                "DELETE FROM Templates WHERE exerciseID = %d;", exerciseID);
        db.execSQL(query);

        query = String.format(l, "DELETE FROM Exercises WHERE ID = %d;", exerciseID);
        db.execSQL(query);

        //find workouts that only had this exercise in them and delete them
        query = String.format(l, "SELECT workoutID FROM (" +
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

        query = String.format(l, "DELETE FROM History WHERE exerciseID = %d;", exerciseID);
        db.execSQL(query);
    }

    public static void renameExercise(String exerciseName, String newExerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l, "UPDATE Exercises SET name = '%s' " +
                "WHERE ID = %d;", newExerciseName, exerciseID);
        db.execSQL(query);
    }

    public static void mergeExercises(String nameOfMainExercise, String nameOfOldExercise) {
        //TODO Handle the setIndexes when both exercises are in the same workout
        int newID = getExerciseID(nameOfMainExercise);
        int oldID = getExerciseID(nameOfOldExercise);
        String query = String.format(l, "UPDATE History SET exerciseID = %d " +
                "WHERE ID = %d;", newID, oldID);
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
        String query = String.format("SELECT ID FROM Exercises WHERE name = '%s';", exerciseName);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        int erg = resultSet.getInt(0);
        resultSet.close();
        return erg;
    }

    public static String getExerciseName(int ID) {
        String query = String.format(l, "SELECT name FROM Exercises WHERE ID = '%d';", ID);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        String erg = resultSet.getString(0);
        resultSet.close();
        return erg;
    }

    public static boolean wasExerciseNeverDone(String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l, "SELECT * FROM History WHERE exerciseID = '%d';", exerciseID);
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
            durationPerDay[i] = new WorkoutEntry(0, String.valueOf(LocalDate.now().minusDays(i)));
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
            String query = String.format(l, "SELECT setIndex, reps, weight, tendency, isPR, date " +
                    "FROM History, Workouts WHERE History.WorkoutID = Workouts.ID " +
                    "AND exerciseID = %d AND ID = %d ORDER BY date DESC;", exerciseID, ID);
            Cursor resultSet = db.rawQuery(query, null);
            resultSet.moveToFirst();
            ArrayList<Set> sets = new ArrayList<>();
            do {
                sets.add(new Set(resultSet.getInt(0), resultSet.getInt(1), resultSet.getFloat(2),
                        resultSet.getInt(3), resultSet.getInt(4) > 0));
            }
            while (resultSet.moveToNext());
            resultSet.moveToFirst();
            entries.add((new ExerciseEntry(sets, resultSet.getString(5))));
            resultSet.close();
        }
        return new ExerciseHistory(exerciseID, exerciseName, entries);
    }

    public static PersonalRecord getPersonalRecordVolume(String exerciseName) {
        int exerciseID = getExerciseID(exerciseName);
        String query = String.format(l,
                "SELECT weight, reps, date FROM History, Workouts " +
                "WHERE History.workoutID = Workouts.ID AND exerciseID = %d " +
                "ORDER BY (weight * reps) DESC LIMIT 1;", exerciseID);
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
                        "ORDER BY weight DESC LIMIT 1;", exerciseID);
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
    #############################################GENERAL############################################
    ##############################################################################################*/
    public static boolean exportDatabase(String databasePath) {
        try {
            File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (download.canWrite()) {
                String backupDBPath = "GymtrackerExport.db";
                File currentDB = new File(databasePath);
                File backupDB = new File(download, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean importDatabase(String databasePath) {
        try {
            File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (download.canWrite()) {
                String backupDBPath  = "GymtrackerExport.db";
                File currentDB = new File(databasePath);
                File backupDB  = new File(download, backupDBPath);

                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void dropTable(String table) {
        if (doesTableExist(table)) {
            String query = String.format("DROP TABLE \"%s\";", table);
            db.execSQL(query);
        }
    }

    public static boolean doesTableExist(String table) {
        String query = String.format(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='%s';", table);
        Cursor rs = db.rawQuery(query, null);
        boolean erg = rs.getCount() != 0;
        rs.close();
        return erg;
    }

    public static void fillWorkoutsTable() {
        String query = String.format(
                "SELECT * FROM workouts");
        Cursor rs = db.rawQuery (query, null);
        if (rs.getCount() > 10) {
            return;
        }
        String workoutName = "Autogenerated";
        float totalWeight = 0;
        int numberOfPRs = 0;
        for (int i = 0; i < 366; i++) {
            int workoutID = getNextWorkoutID();

            String date = LocalDate.now().minusDays(i) + " 00:00:00";
            int duration = ThreadLocalRandom.current().nextInt(3000, 4800);

            query = String.format(l, "INSERT INTO Workouts VALUES (%d, '%s', %d, '%s', '%s', %d)",
                    workoutID, workoutName, duration, date,
                    Formatter.formatFloat(totalWeight), numberOfPRs);
            db.execSQL(query);
            for (int j = 1; j < 4; j++) {
                float weight = ThreadLocalRandom.current().nextInt(75, 85);
                int reps = ThreadLocalRandom.current().nextInt(8, 14);
                query = String.format(l, "INSERT INTO History VALUES (%d, 1, %d, %d, %s, 0, 0)",
                        workoutID, j, reps, Formatter.formatFloat(weight));
                db.execSQL(query);
            }
        }
    }
}
