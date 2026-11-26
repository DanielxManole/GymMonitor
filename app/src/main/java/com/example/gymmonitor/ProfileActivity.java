package com.example.gymmonitor;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    EditText etFirstName, etLastName, etDOB, etHeight, etWeight;
    TextView tvCalculatedAge;
    RadioGroup rgSex, rgGoal;
    Spinner spinnerActivity;
    Button btnSave, btnDeleteAccount;
    DatabaseHelper dbHelper;

    int calculatedAge = 0;
    boolean hasAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        // --- Initializare Elemente UI ---
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDOB = findViewById(R.id.etDOB);
        tvCalculatedAge = findViewById(R.id.tvCalculatedAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        rgSex = findViewById(R.id.rgSex);
        rgGoal = findViewById(R.id.rgGoal);
        spinnerActivity = findViewById(R.id.spinnerActivity);

        btnSave = findViewById(R.id.btnSaveProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Configurare Spinner
        String[] activityLevels = {"Sedentar", "Ușor Activ", "Moderat", "Foarte Activ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityLevels);
        spinnerActivity.setAdapter(adapter);

        // --- CALENDAR ---
        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasAccount) return;
                showDatePicker();
            }
        });

        // --- BUTON STERGERE ---
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        // --- VERIFICARE INITIALA ---
        SharedPreferences prefs = getSharedPreferences("GymPrefs", MODE_PRIVATE);
        hasAccount = prefs.getBoolean("hasAccount", false);

        btnSave.setOnClickListener(v -> handleSaveOrUpdate());

        if (hasAccount) {
            setupViewMode();
        } else {
            setupCreateMode();
        }
    }

    // =========================================================
    //               LOGICA BUTONULUI PRINCIPAL
    // =========================================================

    private void handleSaveOrUpdate() {
        if (!hasAccount) {
            createProfile();
        } else {
            updateProfileSettings();
        }
    }

    // =========================================================
    //               MODURI DE AFISARE
    // =========================================================

    private void setupCreateMode() {
        btnSave.setText("Creează Cont");
        btnSave.setVisibility(View.VISIBLE);
        btnDeleteAccount.setVisibility(View.GONE);

        // La CREARE, greutatea TREBUIE sa fie vizibila
        etWeight.setVisibility(View.VISIBLE);
    }

    private void setupViewMode() {
        btnSave.setText("Salvează Modificările");
        btnSave.setVisibility(View.VISIBLE);
        btnDeleteAccount.setVisibility(View.VISIBLE);

        loadUserData();
        disableFixedFields();

        // --- MODIFICARE: ASCUNDEM GREUTATEA LA VIZUALIZARE ---
        // Datele raman in baza de date, dar utilizatorul nu vede campul aici.
        etWeight.setVisibility(View.GONE);
    }

    // =========================================================
    //               INCARCARE SI AFISARE DATE
    // =========================================================

    private void loadUserData() {
        Cursor cursor = dbHelper.getAllProfileData();
        if (cursor.moveToLast()) {

            etFirstName.setText(cursor.getString(1));
            etLastName.setText(cursor.getString(2));
            etDOB.setText(cursor.getString(3));
            tvCalculatedAge.setText("Vârsta: " + cursor.getInt(4) + " ani");
            etHeight.setText(String.valueOf(cursor.getInt(6)));

            // Setam textul pentru greutate (chiar daca e ascuns, e bine sa fie populat)
            etWeight.setText(String.valueOf(cursor.getDouble(7)));

            // 1. Setam Spinner-ul (Activitate)
            String savedActivity = cursor.getString(8);
            if (savedActivity != null) {
                ArrayAdapter myAdap = (ArrayAdapter) spinnerActivity.getAdapter();
                int spinnerPosition = myAdap.getPosition(savedActivity);
                spinnerActivity.setSelection(spinnerPosition);
            }

            // 2. Setam RadioGroup-ul (Obiectiv)
            String savedGoal = cursor.getString(9);
            if (savedGoal != null) {
                if (savedGoal.equals("Slabire")) {
                    rgGoal.check(R.id.rbLoseWeight);
                } else if (savedGoal.equals("Mentinere")) {
                    rgGoal.check(R.id.rbMaintain);
                } else if (savedGoal.equals("Muschi")) {
                    rgGoal.check(R.id.rbGainMuscle);
                }
            }

            // 3. Setam RadioGroup-ul (Sex)
            String savedSex = cursor.getString(5);
            if (savedSex != null) {
                if (savedSex.equals("M")) rgSex.check(R.id.rbMale);
                else if (savedSex.equals("F")) rgSex.check(R.id.rbFemale);
            }
        }
    }

    private void disableFixedFields() {
        etFirstName.setEnabled(false);
        etLastName.setEnabled(false);
        etDOB.setEnabled(false);
        etHeight.setEnabled(false);
        // etWeight e oricum GONE (ascuns), deci enable/disable nu mai conteaza vizual,
        // dar il lasam disable pentru siguranta.
        etWeight.setEnabled(false);

        rgSex.setEnabled(false);
        for(int i=0; i<rgSex.getChildCount(); i++) rgSex.getChildAt(i).setEnabled(false);

        rgGoal.setEnabled(true);
        spinnerActivity.setEnabled(true);
        for(int i=0; i<rgGoal.getChildCount(); i++) rgGoal.getChildAt(i).setEnabled(true);
    }

    // =========================================================
    //               SALVARE (CREATE) vs ACTUALIZARE (UPDATE)
    // =========================================================

    private void createProfile() {
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String dob = etDOB.getText().toString();
        String heightStr = etHeight.getText().toString();
        String weightStr = etWeight.getText().toString();
        String activity = spinnerActivity.getSelectedItem().toString();

        String sex = "";
        if (rgSex.getCheckedRadioButtonId() == R.id.rbMale) sex = "M";
        else if (rgSex.getCheckedRadioButtonId() == R.id.rbFemale) sex = "F";

        String goal = "";
        if (rgGoal.getCheckedRadioButtonId() == R.id.rbLoseWeight) goal = "Slabire";
        else if (rgGoal.getCheckedRadioButtonId() == R.id.rbMaintain) goal = "Mentinere";
        else if (rgGoal.getCheckedRadioButtonId() == R.id.rbGainMuscle) goal = "Muschi";

        if (firstName.isEmpty() || dob.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
            return;
        }

        int height = Integer.parseInt(heightStr);
        double weight = Double.parseDouble(weightStr);

        boolean isInserted = dbHelper.addProfileData(firstName, lastName, dob, calculatedAge, sex, height, weight, activity, goal, "");

        if (isInserted) {
            SharedPreferences prefs = getSharedPreferences("GymPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("hasAccount", true).apply();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void updateProfileSettings() {
        // 1. Luam valorile selectate de utilizator
        String activity = spinnerActivity.getSelectedItem().toString();
        String goal = "";

        if (rgGoal.getCheckedRadioButtonId() == R.id.rbLoseWeight) goal = "Slabire";
        else if (rgGoal.getCheckedRadioButtonId() == R.id.rbMaintain) goal = "Mentinere";
        else if (rgGoal.getCheckedRadioButtonId() == R.id.rbGainMuscle) goal = "Muschi";

        // 2. APELAM BAZA DE DATE (Asta lipsea!)
        boolean isUpdated = dbHelper.updateGoalAndActivity(activity, goal);

        // 3. Verificam daca a mers
        if (isUpdated) {
            Toast.makeText(this, "Obiectiv și Activitate actualizate!", Toast.LENGTH_SHORT).show();

            // Optional: Inchidem activitatea ca sa se intoarca in meniu si sa vada noile calorii calculate
            finish();
        } else {
            Toast.makeText(this, "Eroare la salvare.", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================
    //               CALENDAR & STERGERE
    // =========================================================

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    etDOB.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                    calculateAge(year1, monthOfYear, dayOfMonth);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void calculateAge(int birthYear, int birthMonth, int birthDay) {
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        calculatedAge = currentYear - birthYear;
        if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
            calculatedAge--;
        }
        tvCalculatedAge.setText("Vârsta: " + calculatedAge + " ani");
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Ștergere Cont");
        builder.setMessage("Ești sigur? Aplicația se va reseta complet.");
        builder.setPositiveButton("DA, ȘTERGE", (dialog, which) -> performFactoryReset());
        builder.setNegativeButton("NU", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void performFactoryReset() {
        try {
            this.deleteDatabase("GymMonitor.db");
            SharedPreferences prefs = getSharedPreferences("GymPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Toast.makeText(this, "Resetare completă!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}