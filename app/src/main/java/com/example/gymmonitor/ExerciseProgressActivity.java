package com.example.gymmonitor;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class ExerciseProgressActivity extends AppCompatActivity {

    Spinner spinMuscle, spinExercise;
    Button btnShow;
    LineChart lineChart;
    DatabaseHelper db;

    String[] muscleGroups = {"Alege Grupa...", "Piept", "Spate", "Picioare", "Umeri", "Biceps", "Triceps", "Abdomen"};
    ArrayList<String> availableExercises = new ArrayList<>(); // Aici tinem exercitiile gasite in DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_progress);

        spinMuscle = findViewById(R.id.spinnerProgMuscle);
        spinExercise = findViewById(R.id.spinnerProgExercise);
        btnShow = findViewById(R.id.btnGenerateChart);
        lineChart = findViewById(R.id.chartExercise);
        db = new DatabaseHelper(this);

        setupMuscleSpinner();

        // Cand apesi butonul, facem graficul
        btnShow.setOnClickListener(v -> loadChartData());
    }

    private void setupMuscleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, muscleGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMuscle.setAdapter(adapter);

        spinMuscle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGroup = muscleGroups[position];
                if (!selectedGroup.equals("Alege Grupa...")) {
                    loadExercisesFromDB(selectedGroup);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Aici citim din baza de date ce exercitii ai facut tu pentru grupa selectata
    private void loadExercisesFromDB(String muscle) {
        availableExercises.clear();
        Cursor cursor = db.getUniqueExercisesForMuscle(muscle);

        if (cursor.getCount() == 0) {
            availableExercises.add("Nu ai date salvate");
        } else {
            while (cursor.moveToNext()) {
                // Coloana 0 este exercise_name
                availableExercises.add(cursor.getString(0));
            }
        }
        cursor.close();

        // Actualizam al doilea Spinner
        ArrayAdapter<String> exAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableExercises);
        exAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinExercise.setAdapter(exAdapter);
    }

    private void loadChartData() {
        if (spinExercise.getSelectedItem() == null) return;
        String exercise = spinExercise.getSelectedItem().toString();

        if (exercise.equals("Nu ai date salvate")) return;

        Cursor cursor = db.getExerciseHistory(exercise);
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "Nu există date suficiente", Toast.LENGTH_SHORT).show();
            return;
        }

        while (cursor.moveToNext()) {
            String date = cursor.getString(0); // Data
            float kg = cursor.getFloat(1);     // Greutatea Maxima

            entries.add(new Entry(index, kg)); // X=index, Y=kg
            dates.add(date); // Tinem minte data ca sa o punem pe axa X
            index++;
        }
        cursor.close();

        // Configurare Grafic
        LineDataSet dataSet = new LineDataSet(entries, "Progres: " + exercise);
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextSize(12f);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleColor(Color.RED);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Punem datele pe axa X (ca sa nu apara 0, 1, 2 ci 2024-01-17)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45); // Le inclinam sa incapa

        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(1000);
        lineChart.invalidate(); // Refresh
    }
}