package com.example.gymmonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    TextView tvSelectedDate, tvWorkoutDetails;
    EditText etReminder;

    // ATENTIE: Folosim 'Button' simplu, pentru ca in XML am pus text, nu iconita
    Button btnSaveReminder, btnSetAlarm;

    DatabaseHelper dbHelper;

    // Variabila care tine minte data selectata curent (format text "zi/luna/an")
    String currentSelectedDate = "";

    // Variabile pentru a tine minte componentele datei pentru alarma
    int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // 1. Initializare UI
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvWorkoutDetails = findViewById(R.id.tvWorkoutDetails);
        etReminder = findViewById(R.id.etReminder);

        btnSaveReminder = findViewById(R.id.btnSaveReminder);
        btnSetAlarm = findViewById(R.id.btnSetAlarm);

        // 2. Initializare Baza de Date
        dbHelper = new DatabaseHelper(this);

        // 3. Setare data initiala (Azi) la pornirea paginii
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Nota: Luna in Java incepe de la 0, deci adaugam 1 pentru afisare/salvare
        currentSelectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
        tvSelectedDate.setText("Data: " + currentSelectedDate);

        // Incarcam datele pentru azi
        loadDataForDate(currentSelectedDate);

        // 4. Listener Calendar - Se activeaza cand dai click pe o zi
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Actualizam variabilele globale
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;

                int realMonth = month + 1;
                currentSelectedDate = dayOfMonth + "/" + realMonth + "/" + year;

                // Actualizam textul si incarcam datele din baza de date
                tvSelectedDate.setText("Data: " + currentSelectedDate);
                loadDataForDate(currentSelectedDate);
            }
        });

        // 5. Listener Buton Salvare Notita
        btnSaveReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = etReminder.getText().toString();

                if (currentSelectedDate.isEmpty()) {
                    Toast.makeText(CalendarActivity.this, "Selectează o dată!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Salvam in tabelul simplu
                boolean isSaved = dbHelper.saveWorkout(currentSelectedDate, reminderText);

                if (isSaved) {
                    Toast.makeText(CalendarActivity.this, "Notiță salvată!", Toast.LENGTH_SHORT).show();
                    etReminder.clearFocus(); // Scoatem cursorul din casuta
                } else {
                    Toast.makeText(CalendarActivity.this, "Eroare la salvare.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 6. Listener Buton Alarma (Ceas)
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
    }

    // --- METODA CARE INCARCA DATELE DIN DB ---
    private void loadDataForDate(String date) {
        // A. Reminderul simplu (textul de sus)
        String savedReminder = dbHelper.getWorkoutForDate(date);
        etReminder.setText(savedReminder);

        // B. Istoricul complex (antrenamentele facute - textul de jos)
        String details = dbHelper.getFullWorkoutDetails(date);
        tvWorkoutDetails.setText(details);
    }

    // --- LOGICA ALARMEI ---
    private void showTimePicker() {
        // Luam ora curenta ca punct de plecare
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Dupa ce utilizatorul alege ora si apasa OK:
                        scheduleAlarm(hourOfDay, minute);
                    }
                }, hour, minute, true); // true = format 24h
        timePickerDialog.show();
    }

    private void scheduleAlarm(int hour, int minute) {
        // 1. Configuram data si ora exacta pentru alarma
        Calendar alarmCalendar = Calendar.getInstance();
        alarmCalendar.set(Calendar.YEAR, selectedYear);
        alarmCalendar.set(Calendar.MONTH, selectedMonth);
        alarmCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
        alarmCalendar.set(Calendar.MINUTE, minute);
        alarmCalendar.set(Calendar.SECOND, 0);

        // Verificam daca timpul ales este in trecut
        if (alarmCalendar.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Nu poți seta alarma în trecut!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Pregatim AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Intentul catre "AlarmReceiver" (clasa care va afisa notificarea)
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Trimitem textul notitei ca mesaj in notificare
        String message = etReminder.getText().toString();
        if(message.isEmpty()) message = "Ai un antrenament programat!";
        intent.putExtra("message", message);

        // PendingIntent (cu FLAG_IMMUTABLE pentru Android 12+)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Setam alarma
        if (alarmManager != null) {
            try {
                // Folosim setExact pentru precizie
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);

                // Afisam confirmare
                String timeString = String.format("%02d:%02d", hour, minute);
                Toast.makeText(this, "Alarmă setată pentru " + timeString, Toast.LENGTH_SHORT).show();

            } catch (SecurityException e) {
                // Pe Android 12+, daca nu avem permisiunea SCHEDULE_EXACT_ALARM
                Toast.makeText(this, "Eroare permisiune alarmă.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}