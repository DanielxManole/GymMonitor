package com.example.gymmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GymMonitor.db";
    private static final int DATABASE_VERSION = 5;

    // =========================================================================
    //                            DEFINIRE CONSTANTE
    // =========================================================================

    // --- [Odo] CONSTANTE PROFIL ---
    public static final String TABLE_PROFILE = "profile_table";
    public static final String COL_ID = "ID";
    public static final String COL_FIRST_NAME = "FIRST_NAME";
    public static final String COL_LAST_NAME = "LAST_NAME";
    public static final String COL_DOB = "DOB";
    public static final String COL_AGE = "AGE";
    public static final String COL_SEX = "SEX";
    public static final String COL_HEIGHT = "HEIGHT";
    public static final String COL_WEIGHT = "WEIGHT";
    public static final String COL_FITNESS_LEVEL = "FITNESS_LEVEL";
    public static final String COL_GOAL = "GOAL";
    public static final String COL_PASSWORD = "PASSWORD";

    // --- [Dani] CONSTANTE CALENDAR ---
    public static final String TABLE_WORKOUTS = "workouts_table";
    public static final String COL_RECORD_DATE = "record_date";
    public static final String COL_WORKOUT_PLAN = "workout_plan";

    // --- [Dani] CONSTANTE SESIUNI ---
    public static final String TABLE_SESSIONS = "sessions_table";
    public static final String COL_SESSION_ID = "id";
    public static final String COL_SESSION_NAME = "session_name";
    public static final String COL_SESSION_DATE = "session_date";

    public static final String TABLE_EXERCISES = "exercises_log_table";
    public static final String COL_EX_ID = "id";
    public static final String COL_EX_SESSION_ID = "session_id";
    public static final String COL_EX_MUSCLE = "muscle_group";
    public static final String COL_EX_NAME = "exercise_name";
    public static final String COL_EX_KG = "kg_val";
    public static final String COL_EX_REPS = "reps_val";

    // --- [Dani] CONSTANTE ISTORIC GREUTATE ---
    public static final String TABLE_WEIGHT_LOG = "weight_log_table";
    public static final String COL_LOG_ID = "id";
    public static final String COL_LOG_DATE = "log_date";
    public static final String COL_LOG_VAL = "weight_val";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // =========================================================================
    //                            CREARE TABELE (SQL)
    // =========================================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
        // [Odo] Tabel Profil
        String createProfile = "CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FIRST_NAME + " TEXT, " +
                COL_LAST_NAME + " TEXT, " +
                COL_DOB + " TEXT, " +
                COL_AGE + " INTEGER, " +
                COL_SEX + " TEXT, " +
                COL_HEIGHT + " INTEGER, " +
                COL_WEIGHT + " REAL, " +
                COL_FITNESS_LEVEL + " TEXT, " +
                COL_GOAL + " TEXT, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createProfile);

        // [Dani] Tabel Calendar Simplu
        String createWorkouts = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COL_RECORD_DATE + " TEXT PRIMARY KEY, " +
                COL_WORKOUT_PLAN + " TEXT)";
        db.execSQL(createWorkouts);

        // [Dani] Tabel Sesiuni
        String createSessions = "CREATE TABLE " + TABLE_SESSIONS + " (" +
                COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESSION_NAME + " TEXT, " +
                COL_SESSION_DATE + " TEXT)";
        db.execSQL(createSessions);

        // [Dani] Tabel Exercitii
        String createExercises = "CREATE TABLE " + TABLE_EXERCISES + " (" +
                COL_EX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EX_SESSION_ID + " INTEGER, " +
                COL_EX_MUSCLE + " TEXT, " +
                COL_EX_NAME + " TEXT, " +
                COL_EX_KG + " REAL, " +
                COL_EX_REPS + " INTEGER)";
        db.execSQL(createExercises);

        // [Dani] Tabel Istoric Greutate Corp
        String createWeightLog = "CREATE TABLE " + TABLE_WEIGHT_LOG + " (" +
                COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LOG_DATE + " TEXT, " +
                COL_LOG_VAL + " REAL)";
        db.execSQL(createWeightLog);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_LOG);
        onCreate(db);
    }

    // =========================================================================
    //                        METODE PROFIL (User Management)
    // =========================================================================

    // [Odo] Metoda completa de inregistrare
    public boolean addProfileData(String firstName, String lastName, String dob, int age, String sex,
                                  int height, double weight, String fitnessLevel, String goal, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FIRST_NAME, firstName);
        cv.put(COL_LAST_NAME, lastName);
        cv.put(COL_DOB, dob);
        cv.put(COL_AGE, age);
        cv.put(COL_SEX, sex);
        cv.put(COL_HEIGHT, height);
        cv.put(COL_WEIGHT, weight);
        cv.put(COL_FITNESS_LEVEL, fitnessLevel);
        cv.put(COL_GOAL, goal);
        cv.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_PROFILE, null, cv);

        // --- MODIFICARE AICI: ---
        // Daca profilul s-a creat cu succes, adaugam AUTOMAT greutatea in istoricul de progres
        if (result != -1) {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            addWeightLog(todayDate, weight);
        }
        // ------------------------

        return result != -1;
    }

    // [Odo] Metoda update date sensibile
    public boolean updateSensitiveData(double newWeight, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (newWeight > 0) {
            cv.put(COL_WEIGHT, newWeight);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            cv.put(COL_PASSWORD, newPassword);
        }
        long result = db.update(TABLE_PROFILE, cv, null, null);
        return result != -1;
    }

    // [Odo] Metoda actualizare Obiectiv si Activitate
    // Metoda pentru actualizarea Obiectivului si Activitatii
    public boolean updateGoalAndActivity(String activity, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FITNESS_LEVEL, activity);
        cv.put(COL_GOAL, goal);

        // Actualizam toate randurile (fiind un singur user, e ok)
        long result = db.update(TABLE_PROFILE, cv, null, null);
        return result != -1;
    }

    public Cursor getAllProfileData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PROFILE, null);
    }

    // =========================================================================
    //                        METODE CALENDAR
    // =========================================================================

    public boolean saveWorkout(String date, String plan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RECORD_DATE, date);
        cv.put(COL_WORKOUT_PLAN, plan);
        long result = db.replace(TABLE_WORKOUTS, null, cv);
        return result != -1;
    }

    public String getWorkoutForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COL_RECORD_DATE + " = ?", new String[]{date});
        if (cursor.moveToFirst()) {
            String plan = cursor.getString(1);
            cursor.close();
            return plan;
        } else {
            cursor.close();
            return "";
        }
    }

    // =========================================================================
    //                  METODE ANTRENAMENT COMPLEX & STATISTICI
    // =========================================================================

    public long createWorkoutSession(String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SESSION_NAME, name);
        cv.put(COL_SESSION_DATE, date);
        return db.insert(TABLE_SESSIONS, null, cv);
    }

    public void addExerciseToSession(long sessionId, String muscle, String exerciseName, double kg, int reps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EX_SESSION_ID, sessionId);
        cv.put(COL_EX_MUSCLE, muscle);
        cv.put(COL_EX_NAME, exerciseName);
        cv.put(COL_EX_KG, kg);
        cv.put(COL_EX_REPS, reps);
        db.insert(TABLE_EXERCISES, null, cv);
    }

    public Cursor getUniqueExercisesForMuscle(String muscleGroup) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT " + COL_EX_NAME + " FROM " + TABLE_EXERCISES + " WHERE " + COL_EX_MUSCLE + " = ?", new String[]{muscleGroup});
    }

    public Cursor getExerciseHistory(String exerciseName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s." + COL_SESSION_DATE + ", MAX(e." + COL_EX_KG + ") as max_kg " +
                "FROM " + TABLE_SESSIONS + " s " +
                "JOIN " + TABLE_EXERCISES + " e ON s." + COL_SESSION_ID + " = e." + COL_EX_SESSION_ID + " " +
                "WHERE e." + COL_EX_NAME + " = ? " +
                "GROUP BY s." + COL_SESSION_DATE + " " +
                "ORDER BY s." + COL_SESSION_DATE + " ASC";
        return db.rawQuery(query, new String[]{exerciseName});
    }

    // =========================================================================
    //                     METODE ISTORIC GREUTATE CORP
    // =========================================================================

    public boolean addWeightLog(String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LOG_DATE, date);
        cv.put(COL_LOG_VAL, weight);
        long result = db.insert(TABLE_WEIGHT_LOG, null, cv);
        return result != -1;
    }

    public Cursor getWeightHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WEIGHT_LOG + " ORDER BY " + COL_LOG_DATE + " ASC", null);
    }

    public void deleteWeightLog(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEIGHT_LOG, COL_LOG_ID + "=?", new String[]{String.valueOf(id)});
    }

    // =========================================================================
    //             METODA VIZUALIZARE GRUPATA
    // =========================================================================

    public String getFullWorkoutDetails(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder builder = new StringBuilder();

        String query = "SELECT s." + COL_SESSION_NAME + ", e." + COL_EX_MUSCLE + ", e." + COL_EX_NAME +
                ", e." + COL_EX_KG + ", e." + COL_EX_REPS +
                " FROM " + TABLE_SESSIONS + " s " +
                " LEFT JOIN " + TABLE_EXERCISES + " e ON s." + COL_SESSION_ID + " = e." + COL_EX_SESSION_ID +
                " WHERE s." + COL_SESSION_DATE + " = ?" +
                " ORDER BY e." + COL_EX_MUSCLE + " ASC, e." + COL_EX_ID + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor != null && cursor.moveToFirst()) {
            builder.append("Antrenament: ").append(cursor.getString(0)).append("\n");
            String currentMuscleGroup = "";

            do {
                if (cursor.getString(2) == null) continue;

                String muscle = cursor.getString(1);
                String exName = cursor.getString(2);
                double kg = cursor.getDouble(3);
                int reps = cursor.getInt(4);

                if (!muscle.equals(currentMuscleGroup)) {
                    builder.append("\n🔸 ").append(muscle.toUpperCase()).append("\n");
                    currentMuscleGroup = muscle;
                }
                builder.append("   • ").append(exName)
                        .append(": ").append(kg).append("kg x ").append(reps).append("\n");

            } while (cursor.moveToNext());
        } else {
            builder.append("Niciun antrenament înregistrat.");
        }

        if (cursor != null) {
            cursor.close();
        }
        return builder.toString();
    }

    // =========================================================================
    //             METODA NOUA: CALCUL CALORIC PERSONALIZAT
    // =========================================================================
    public String getCalorieRecommendation() {
        SQLiteDatabase db = this.getReadableDatabase();
        double currentWeight = 0;
        int height = 0;
        int age = 0;
        String sex = "";
        String activity = "";
        String goal = "";

        // --- PASUL 1: CAUTAM IN ISTORICUL DE GREUTATE (PRIORITATE MAXIMA) ---
        // Luam ultima inregistrare adaugata (dupa ID descrescator), ca sa fim siguri ca e cea mai recenta
        Cursor cursorWeight = db.rawQuery("SELECT " + COL_LOG_VAL + " FROM " + TABLE_WEIGHT_LOG + " ORDER BY " + COL_LOG_ID + " DESC LIMIT 1", null);

        if (cursorWeight.moveToFirst()) {
            currentWeight = cursorWeight.getDouble(0); // Am gasit greutate in istoric!
        }
        cursorWeight.close();

        // --- PASUL 2: LUAM DATELE DIN PROFIL ---
        Cursor cursorProfile = db.rawQuery("SELECT * FROM " + TABLE_PROFILE, null);
        if (cursorProfile.moveToLast()) {
            // Daca NU am gasit greutate in istoric (currentWeight e inca 0), luam din profil
            if (currentWeight == 0) {
                currentWeight = cursorProfile.getDouble(7); // COL_WEIGHT din Profil
            }

            // Restul datelor vin mereu din profil
            height = cursorProfile.getInt(6);      // COL_HEIGHT
            age = cursorProfile.getInt(4);         // COL_AGE
            sex = cursorProfile.getString(5);      // COL_SEX
            activity = cursorProfile.getString(8); // COL_FITNESS_LEVEL
            goal = cursorProfile.getString(9);     // COL_GOAL
        }
        cursorProfile.close();

        // Validare simpla
        if (height == 0 || currentWeight == 0) return "Completează profilul.";

        // --- PASUL 3: CALCUL BMR (Mifflin-St Jeor) ---
        double bmr;
        if (sex != null && sex.equalsIgnoreCase("M")) {
            bmr = (10 * currentWeight) + (6.25 * height) - (5 * age) + 5;
        } else {
            // Feminin
            bmr = (10 * currentWeight) + (6.25 * height) - (5 * age) - 161;
        }

        // --- PASUL 4: CALCUL TDEE (Factor Activitate) ---
        double tdee;
        if (activity == null) activity = "Sedentar"; // Default

        switch (activity) {
            case "Sedentar":
                tdee = bmr * 1.2;
                break;
            case "Ușor Activ":
                tdee = bmr * 1.375;
                break;
            case "Moderat":
                tdee = bmr * 1.55;
                break;
            case "Foarte Activ":
                tdee = bmr * 1.725;
                break;
            default:
                tdee = bmr * 1.2;
        }

        // --- PASUL 5: AJUSTARE PENTRU OBIECTIV ---
        double finalCalories = tdee;
        String message = "Menținere";

        if (goal != null) {
            if (goal.equals("Slabire")) {
                finalCalories = tdee - 500;
                message = "Pt. SLĂBIRE:";
            } else if (goal.equals("Mentinere")) {
                finalCalories = tdee;
                message = "Pt. MENȚINERE:";
            } else if (goal.equals("Muschi")) {
                finalCalories = tdee + 300;
                message = "Pt. MUȘCHI:";
            }
        }

        int target = (int) finalCalories;
        return message + "\n🔥 " + target + " kcal";
    }
    }
