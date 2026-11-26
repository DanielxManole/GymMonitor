package com.example.gymmonitor;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ClockActivity extends AppCompatActivity {

    // Elemente pentru Cronometru
    Chronometer chronometer;
    Button btnStart, btnPause, btnReset;

    // Variabile pentru a tine minte timpul la pauza
    long pauseOffset = 0;
    boolean isRunning = false;

    // Elemente pentru Timer (countdown)
    TimePicker timePicker;
    Button btnSetAlarm;
    TextView tvCountdown;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        // --- 1. Initializare Elemente ---
        chronometer = findViewById(R.id.chronometer);
        btnStart = findViewById(R.id.btnStartChrono);
        btnPause = findViewById(R.id.btnPauseChrono);
        btnReset = findViewById(R.id.btnResetChrono);

        timePicker = findViewById(R.id.timePicker);
        // Folosim format 24h ca să nu afișeze AM/PM (oricum îl folosim ca minute/secunde)
        timePicker.setIs24HourView(true);
        btnSetAlarm = findViewById(R.id.btnSetAlarm);
        tvCountdown = findViewById(R.id.tvCountdown);

        // --- 2. Logica CRONOMETRU ---

        // Buton START
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    // Setam timpul de pornire: Timpul actual minus cat am stat in pauza
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    chronometer.start();
                    isRunning = true;
                }
            }
        });

        // Buton PAUZA
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    chronometer.stop();
                    // Calculam cat timp a trecut ca sa stim de unde reluam
                    pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                    isRunning = false;
                }
            }
        });

        // Buton RESET
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
                chronometer.stop();
                isRunning = false;
            }
        });

        // --- 3. Logica TIMER (Countdown) ---
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    // Oprim si resetam timerul
                    resetTimer();
                } else {
                    // Folosim TimePicker ca MINUTE (hour) + SECUNDE (minute)
                    int minutes = timePicker.getHour();
                    int seconds = timePicker.getMinute();

                    // Conversie minute + secunde în milisecunde
                    long millisInput = (minutes * 60_000L) + (seconds * 1_000L);

                    if (millisInput <= 0) {
                        Toast.makeText(ClockActivity.this, "Setează o durată mai mare decât 0.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    timeLeftInMillis = millisInput;
                    updateCountdownText();
                    startTimer();
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnSetAlarm.setText("Pornește Timerul");
                Toast.makeText(ClockActivity.this, "Timer încheiat!", Toast.LENGTH_SHORT).show();
            }
        }.start();

        isTimerRunning = true;
        btnSetAlarm.setText("Oprește Timerul");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = 0;
        updateCountdownText();
        isTimerRunning = false;
        btnSetAlarm.setText("Pornește Timerul");
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        tvCountdown.setText(timeLeftFormatted);
    }
}