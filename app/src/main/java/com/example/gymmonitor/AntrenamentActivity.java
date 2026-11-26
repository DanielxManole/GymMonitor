package com.example.gymmonitor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AntrenamentActivity extends AppCompatActivity {

    // UI Elements
    EditText etSessionName, etWorkoutDate, etCustomExercise, etKg, etReps;
    Spinner spinnerMuscle, spinnerExercise;
    Button btnAddExercise, btnFinish;
    Button btnKgMinus, btnKgPlus, btnRepsMinus, btnRepsPlus;
    LinearLayout llExercisesContainer;

    DatabaseHelper dbHelper;
    String[] muscleGroups = {"Alege Grupa...", "Piept", "Spate", "Picioare", "Umeri", "Biceps", "Triceps", "Abdomen"};
    HashMap<String, String[]> exercisesMap = new HashMap<>();
    ArrayList<TempExercise> currentSessionExercises = new ArrayList<>();

    // CALENDARUL PENTRU DATA ALEASA
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antrenament);

        initData();
        initViews();
        setupSpinners();
        setupSteppers();
        setupDatePicker();

        btnAddExercise.setOnClickListener(v -> addExerciseToUI());
        btnFinish.setOnClickListener(v -> saveSessionToDatabase());
    }

    private void initData() {
        exercisesMap.put("Piept", new String[]{"Împins Culcat", "Împins Inclinat", "Fluturări", "Cabluri", "Custom"});
        exercisesMap.put("Spate", new String[]{"Tracțiuni", "Ramat Bara", "Helcometru", "Ramat Scripete", "Custom"});
        exercisesMap.put("Picioare", new String[]{"Genuflexiuni", "Presa Picioare", "Extensii", "Fandări", "Custom"});
        exercisesMap.put("Umeri", new String[]{"Presa Militară", "Ridicări Laterale", "Ridicări Frontale", "Custom"});
        exercisesMap.put("Biceps", new String[]{"Flexii Bara", "Flexii Ciocan", "Flexii Scott", "Custom"});
        exercisesMap.put("Triceps", new String[]{"Extensii Scripete", "Skullcrushers", "Dips", "Custom"});
        exercisesMap.put("Abdomen", new String[]{"Crunch", "Plank", "Ridicări picioare", "Custom"});
    }

    private void initViews() {
        dbHelper = new DatabaseHelper(this);
        etSessionName = findViewById(R.id.etSessionName);
        etWorkoutDate = findViewById(R.id.etWorkoutDate);
        etCustomExercise = findViewById(R.id.etCustomExercise);
        etKg = findViewById(R.id.etKg);
        etReps = findViewById(R.id.etReps);
        spinnerMuscle = findViewById(R.id.spinnerMuscleGroup);
        spinnerExercise = findViewById(R.id.spinnerExercises);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnFinish = findViewById(R.id.btnFinishWorkout);
        llExercisesContainer = findViewById(R.id.llExercisesContainer);
        btnKgMinus = findViewById(R.id.btnKgMinus);
        btnKgPlus = findViewById(R.id.btnKgPlus);
        btnRepsMinus = findViewById(R.id.btnRepsMinus);
        btnRepsPlus = findViewById(R.id.btnRepsPlus);
    }

    // --- CONFIGURARE DATE PICKER ---
    private void setupDatePicker() {
        updateLabel();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };

        etWorkoutDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AntrenamentActivity.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etWorkoutDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupSteppers() {
        btnKgMinus.setOnClickListener(v -> adjustValue(etKg, -2.5));
        btnKgPlus.setOnClickListener(v -> adjustValue(etKg, 2.5));
        btnRepsMinus.setOnClickListener(v -> adjustValue(etReps, -1));
        btnRepsPlus.setOnClickListener(v -> adjustValue(etReps, 1));
    }

    private void adjustValue(EditText input, double amount) {
        String currentText = input.getText().toString();
        double val = 0;
        try { val = Double.parseDouble(currentText); } catch (NumberFormatException e) { val = 0; }
        val += amount;
        if (val < 0) val = 0;
        if (val == (long) val) {
            input.setText(String.format(Locale.US, "%d", (long)val));
        } else {
            input.setText(String.format(Locale.US, "%.1f", val));
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, muscleGroups);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscle.setAdapter(muscleAdapter);

        // Listener pentru Grupa Musculara
        spinnerMuscle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateExerciseSpinner(muscleGroups[position]);
                // RESETAM campurile cand schimbam grupa
                etKg.setText("");
                etReps.setText("");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Listener pentru Exercitiu
        spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerExercise.getSelectedItem().toString().equals("Custom")) {
                    etCustomExercise.setVisibility(View.VISIBLE);
                } else {
                    etCustomExercise.setVisibility(View.GONE);
                }
                // RESETAM campurile cand schimbam exercitiul
                etKg.setText("");
                etReps.setText("");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateExerciseSpinner(String muscleGroup) {
        String[] exercises = exercisesMap.get(muscleGroup);
        if (exercises == null) exercises = new String[]{"Selectează grupa"};
        ArrayAdapter<String> exAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exercises);
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExercise.setAdapter(exAdapter);
    }

    private void addExerciseToUI() {
        String muscle = spinnerMuscle.getSelectedItem().toString();
        String exerciseName = spinnerExercise.getSelectedItem().toString();
        if (exerciseName.equals("Custom")) exerciseName = etCustomExercise.getText().toString();
        String kgStr = etKg.getText().toString();
        String repsStr = etReps.getText().toString();

        if (muscle.equals("Alege Grupa...") || exerciseName.isEmpty() || kgStr.isEmpty() || repsStr.isEmpty()) {
            Toast.makeText(this, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
            return;
        }

        double kg = Double.parseDouble(kgStr);
        int reps = (int) Double.parseDouble(repsStr);
        TempExercise temp = new TempExercise(muscle, exerciseName, kg, reps);
        currentSessionExercises.add(temp);
        addVisualGroupedRow(temp);

        // Resetam valorile si dupa ce adaugam setul (ca in cerinta anterioara)
        etKg.setText("");
        etReps.setText("");
        etKg.requestFocus();
    }

    private void addVisualGroupedRow(TempExercise temp) {
        View existingCard = null;
        for (int i = 0; i < llExercisesContainer.getChildCount(); i++) {
            View child = llExercisesContainer.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals(temp.exercise)) {
                existingCard = child;
                break;
            }
        }
        LinearLayout setsContainer;
        if (existingCard == null) {
            existingCard = LayoutInflater.from(this).inflate(R.layout.item_exercise_row, llExercisesContainer, false);
            existingCard.setTag(temp.exercise);
            TextView tvTitle = existingCard.findViewById(R.id.tvGroupExerciseName);
            tvTitle.setText(temp.exercise);
            llExercisesContainer.addView(existingCard);
        }
        setsContainer = existingCard.findViewById(R.id.llSetsContainer);
        View setRow = LayoutInflater.from(this).inflate(R.layout.item_set_line, setsContainer, false);
        TextView tvDetails = setRow.findViewById(R.id.tvSetDetails);
        ImageButton btnDelete = setRow.findViewById(R.id.btnDeleteSet);
        tvDetails.setText(String.format(Locale.US, "%.1f kg x %d reps", temp.kg, temp.reps));

        final View cardReference = existingCard;
        btnDelete.setOnClickListener(v -> {
            setsContainer.removeView(setRow);
            currentSessionExercises.remove(temp);
            if (setsContainer.getChildCount() == 0) llExercisesContainer.removeView(cardReference);
        });
        setsContainer.addView(setRow);
    }

    private void saveSessionToDatabase() {
        String sessionName = etSessionName.getText().toString();
        if (sessionName.isEmpty()) {
            Toast.makeText(this, "Pune un nume sesiunii!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSessionExercises.isEmpty()) {
            Toast.makeText(this, "Lista e goală!", Toast.LENGTH_SHORT).show();
            return;
        }
        int year = myCalendar.get(Calendar.YEAR);
        int month = myCalendar.get(Calendar.MONTH) + 1;
        int day = myCalendar.get(Calendar.DAY_OF_MONTH);
        String selectedDate = day + "/" + month + "/" + year;
        long id = dbHelper.createWorkoutSession(sessionName, selectedDate);

        if (id != -1) {
            for (TempExercise ex : currentSessionExercises) {
                dbHelper.addExerciseToSession(id, ex.muscle, ex.exercise, ex.kg, ex.reps);
            }
            Toast.makeText(this, "Antrenament Salvat pe " + selectedDate + "!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Eroare la salvare", Toast.LENGTH_SHORT).show();
        }
    }

    class TempExercise {
        String muscle, exercise;
        double kg;
        int reps;
        public TempExercise(String m, String e, double k, int r) {
            muscle = m; exercise = e; kg = k; reps = r;
        }
    }
}