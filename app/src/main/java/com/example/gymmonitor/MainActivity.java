package com.example.gymmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView; // <--- AM ADAUGAT ASTA
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // --- VARIABILE NOI PENTRU CALORII ---
    TextView tvCalorieRecommendation;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. VERIFICARE "PRIMA DESCHIDERE" ---
        SharedPreferences prefs = getSharedPreferences("GymPrefs", MODE_PRIVATE);
        boolean hasAccount = prefs.getBoolean("hasAccount", false);

        if (!hasAccount) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // --- 2. INCARCAREA DESIGNULUI ---
        setContentView(R.layout.activity_main);

        // --- INITIALIZARE BAZA DE DATE SI TEXT CALORII (NOU) ---
        dbHelper = new DatabaseHelper(this);
        tvCalorieRecommendation = findViewById(R.id.tvCalorieRecommendation);

        // --- 3. CONFIGURARE BUTOANE ---

        // A. Butonul Mic de Profil
        ImageButton btnProfileIcon = findViewById(R.id.btnProfileIcon);
        btnProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // B. Butonul Mare: INCEPE ANTRENAMENT
        Button btnWorkout = findViewById(R.id.btnWorkout);
        btnWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AntrenamentActivity.class);
                startActivity(intent);
            }
        });

        // C. Buton Progres (Noul meniu)
        Button btnProgressHub = findViewById(R.id.btnProgressHub);
        btnProgressHub.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProgressMenuActivity.class));
        });

        // D. Buton Calendar
        Button btnCalendar = findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        // E. Buton Unelte
        Button btnTools = findViewById(R.id.btnTools);
        btnTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClockActivity.class);
                startActivity(intent);
            }
        });
    }

    // --- METODA NOUA: SE ACTIVEAZA AUTOMAT CAND DESCHIZI ECRANUL ---
    @Override
    protected void onResume() {
        super.onResume();
        // Aici cerem calculul din baza de date
        if (dbHelper != null && tvCalorieRecommendation != null) {
            String recommendation = dbHelper.getCalorieRecommendation();
            tvCalorieRecommendation.setText(recommendation);
        }
    }
}