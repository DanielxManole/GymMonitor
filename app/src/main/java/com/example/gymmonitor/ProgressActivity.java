package com.example.gymmonitor;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    EditText etWeight;
    Button btnAdd;
    LineChart lineChart;
    LinearLayout llHistoryContainer; // Containerul pentru lista
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        etWeight = findViewById(R.id.etNewWeight);
        btnAdd = findViewById(R.id.btnAddWeightLog);
        lineChart = findViewById(R.id.chartBodyWeight);
        llHistoryContainer = findViewById(R.id.llHistoryContainer);
        db = new DatabaseHelper(this);

        loadDataAndChart();

        btnAdd.setOnClickListener(v -> {
            String wStr = etWeight.getText().toString();
            if (wStr.isEmpty()) return;

            double weight = Double.parseDouble(wStr);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (db.addWeightLog(date, weight)) {
                Toast.makeText(this, "Adăugat!", Toast.LENGTH_SHORT).show();
                etWeight.setText("");
                loadDataAndChart(); // Reincarcam totul
            }
        });
    }

    private void loadDataAndChart() {
        // Curatam lista veche de pe ecran
        llHistoryContainer.removeAllViews();

        Cursor cursor = db.getWeightHistory();
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        int index = 0;
        if (cursor.getCount() == 0) {
            lineChart.clear();
            return;
        }

        while (cursor.moveToNext()) {
            // Structura: 0=id, 1=date, 2=weight
            long id = cursor.getLong(0);
            String date = cursor.getString(1);
            float kg = cursor.getFloat(2);

            // 1. Adaugam punctul in Grafic
            entries.add(new Entry(index, kg));
            dates.add(date);
            index++;

            // 2. Adaugam randul in Lista de Istoric (Visual)
            addHistoryRow(id, date, kg);
        }
        cursor.close();

        // Configurare Grafic
        LineDataSet dataSet = new LineDataSet(entries, "Evoluție Kg");
        dataSet.setColor(Color.parseColor("#673AB7"));
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(800);
        lineChart.invalidate();
    }

    private void addHistoryRow(long id, String date, float kg) {
        // Umflam (inflate) designul randului
        View row = LayoutInflater.from(this).inflate(R.layout.item_weight_row, llHistoryContainer, false);

        TextView tvDate = row.findViewById(R.id.tvWeightDate);
        TextView tvVal = row.findViewById(R.id.tvWeightValue);
        ImageButton btnDelete = row.findViewById(R.id.btnDeleteWeight);

        tvDate.setText(date);
        tvVal.setText(kg + " kg");

        // Logica Butonului X
        btnDelete.setOnClickListener(v -> {
            db.deleteWeightLog(id); // Stergem din baza de date
            loadDataAndChart();     // Reincarcam tot ecranul (graficul se va updata)
            Toast.makeText(ProgressActivity.this, "Șters!", Toast.LENGTH_SHORT).show();
        });

        // Adaugam randul in lista mare
        llHistoryContainer.addView(row); // Adaugam la final (sau addView(row, 0) pentru inceput)
    }
}