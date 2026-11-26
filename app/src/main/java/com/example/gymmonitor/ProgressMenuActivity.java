package com.example.gymmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ProgressMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_menu);

        Button btnBody = findViewById(R.id.btnGoToBodyWeight);
        Button btnExercises = findViewById(R.id.btnGoToExercises);

        // 1. Mergem la Graficul de Greutate Corp (Vechiul ProgressActivity)
        btnBody.setOnClickListener(v -> {
            startActivity(new Intent(ProgressMenuActivity.this, ProgressActivity.class));
        });

        // 2. Mergem la Graficul de Exercitii (Noul ExerciseProgressActivity)
        btnExercises.setOnClickListener(v -> {
            startActivity(new Intent(ProgressMenuActivity.this, ExerciseProgressActivity.class));
        });
    }
}