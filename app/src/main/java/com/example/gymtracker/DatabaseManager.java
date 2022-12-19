package com.example.gymtracker;



import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.util.Log;

import com.example.gymtracker.datastructures.Exercise;
import com.example.gymtracker.datastructures.Set;
import com.example.gymtracker.datastructures.Workout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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

    public static void insertSetIntoCurrentWorkout(int exerciseID, int position, Set set) {
        String query = String.format(l, "INSERT INTO CurrentWorkout VALUES " +
                        "(%d, %d, %d, %d, '%s')",
                        exerciseID, position, set.getIndex(), set.getReps(), Formatter.formatFloat(set.getWeight()));
        db.execSQL(query);
    }

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
        String query = String.format(l, "UPDATE CurrentWorkout SET reps = %d, weight = '%s' " +
                        "WHERE exerciseID = %d AND setIndex = %d;",
                        set.getReps(), Formatter.formatFloat(set.getWeight()), exerciseID, set.getIndex());
        db.execSQL(query);
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
        createWorkoutsTable();
        printTable("CurrentWorkout");
        int workoutID = getNextWorkoutID();
        String workoutName = getCurrentWorkoutName();
        int duration = (int) (System.currentTimeMillis() - getCurrentWorkoutStartTime());
        String currentDate = getCurrentWorkoutDate();
        float totalWeight = getTotalWeightOfCurrentWorkout();

        String query = String.format(l, "INSERT INTO History " +
                        "(workoutID, exerciseID, setIndex, reps, weight) " +
                        "SELECT %d, exerciseID, setIndex, reps, weight " +
                        "FROM CurrentWorkout WHERE reps <> 0;",
                        workoutID);
        db.execSQL(query);

        query = String.format(l, "INSERT INTO Workouts VALUES (%d, '%s', %d, '%s', '%s')",
                        workoutID, workoutName, duration, currentDate, Formatter.formatFloat(totalWeight));
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
        String date = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());
        String query = String.format(l,
                "INSERT INTO CurrentWorkoutMetadata VALUES ('%s', %d, %s);",
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

    public static int getCurrentWorkoutStartTime() {
        String query = "SELECT startTime FROM CurrentWorkoutMetadata;";
        Cursor rs = db.rawQuery(query, null);
        rs.moveToFirst();
        int startTime = rs.getInt(0);
        rs.close();
        return startTime;
    }

    public static String getCurrentWorkoutDate() {
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
                "ID INT, name VARCHAR, duration INT, date VARCHAR, totalWeight REAL);";
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

    /*##############################################################################################
    #############################################HISTORY############################################
    ##############################################################################################*/
    public static void createHistoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS History(" +
                "workoutID INT, exerciseID INT, setIndex INT, reps INT, weight REAL);";
        db.execSQL(query);
    }


    /*##############################################################################################
    ############################################TEMPLATES###########################################
    ##############################################################################################*/
    public static void createTemplatesTable() {
        String query = "CREATE TABLE IF NOT EXISTS Templates(" +
                "name VARCHAR, exerciseID INT, numbrerOfSets INT);";
        db.execSQL(query);
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

    /*##############################################################################################
    #############################################GENERAL############################################
    ##############################################################################################*/
    public static void dropTable(String table) {
        if (doesTableExist(table)) {
            Log.d("DROPPING TABLE", table);
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

    public static void printTable(String tableName) {
        Log.d("PRINTING", tableName);
        String query = String.format("SELECT * FROM %s;", tableName);
        Cursor resultSet = db.rawQuery(query, null);
        resultSet.moveToFirst();
        int i = 0;

        if(Objects.equals(tableName, "Exercises")) {
            do {
                Log.d(String.valueOf(i), String.format(
                        "ID: %d, " +
                        "name: %s",
                        resultSet.getInt(0), resultSet.getString(1)));
                i++;
            }
            while (resultSet.moveToNext());
        }
        else if (Objects.equals(tableName, "CurrentWorkout")) {
            do {
                Log.d(String.valueOf(i), String.format(
                        "exerciseID: %d, " +
                        "position: %d, " +
                        "setIndex: %d, " +
                        "reps: %d, " +
                        "weight: %f",
                        resultSet.getInt(0), resultSet.getInt(1), resultSet.getInt(2),
                        resultSet.getInt(3), resultSet.getFloat(4)));
                i++;
            }
            while (resultSet.moveToNext());
        }
        else if (Objects.equals(tableName, "History")) {
            do {
                Log.d(String.valueOf(i), String.format(
                        "workoutID: %d, " +
                        "exerciseID: %d, " +
                        "setIndex: %d, " +
                        "reps: %d, " +
                        "weight: %f",
                        resultSet.getInt(0), resultSet.getInt(1), resultSet.getInt(2),
                        resultSet.getInt(3), resultSet.getFloat(4)));
                i++;
            }
            while (resultSet.moveToNext());
        }
        resultSet.close();
    }

}
