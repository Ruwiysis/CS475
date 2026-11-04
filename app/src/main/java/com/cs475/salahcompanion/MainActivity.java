
package com.cs475.salahcompanion;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView nextPrayerText;
    private TextView prayerProgressText;
    private TextView streakText;
    private TextView xpText;
    private TextView levelText;

    private DatabaseHelper databaseHelper;
    private StatsManager statsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        statsManager = new StatsManager();

        nextPrayerText = findViewById(R.id.nextPrayerText);
        prayerProgressText = findViewById(R.id.prayerProgressText);
        streakText = findViewById(R.id.streakText);
        xpText = findViewById(R.id.xpText);
        levelText = findViewById(R.id.levelText);

        Button btnPrayerTimes = findViewById(R.id.btnPrayerTimes);
        Button btnPrayerTracker = findViewById(R.id.btnPrayerTracker);
        Button btnLearnSalah = findViewById(R.id.btnLearnSalah);
        Button btnAthkar = findViewById(R.id.btnAthkar);
        Button btnFlashcards = findViewById(R.id.btnFlashcards);
        Button btnQuiz = findViewById(R.id.btnQuiz);

        btnPrayerTimes.setOnClickListener(v ->
                startActivity(new Intent(this, PrayerTimesActivity.class)));

        btnPrayerTracker.setOnClickListener(v ->
                startActivity(new Intent(this, PrayerTrackerActivity.class)));

        btnLearnSalah.setOnClickListener(v ->
                startActivity(new Intent(this, LearnSalahActivity.class)));

        btnAthkar.setOnClickListener(v ->
                startActivity(new Intent(this, AthkarActivity.class)));

        btnFlashcards.setOnClickListener(v ->
                startActivity(new Intent(this, FlashcardsActivity.class)));

        btnQuiz.setOnClickListener(v ->
                startActivity(new Intent(this, QuizActivity.class)));

        updateHomeScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHomeScreen();
    }

    private void updateHomeScreen() {
        updateNextPrayer();
        updatePrayerProgress();

        streakText.setText("Streak: " + statsManager.getStreak(this));
        xpText.setText("XP: " + statsManager.getXp(this));
        levelText.setText("Level: " + statsManager.getLevel(this));
    }

    private void updatePrayerProgress() {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        int completed = 0;

        Cursor cursor = db.rawQuery(
                "SELECT status FROM prayer_log WHERE date = ?",
                new String[]{today}
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String status = cursor.getString(0);

                if ("On Time".equalsIgnoreCase(status)
                        || "Late".equalsIgnoreCase(status)) {
                    completed++;
                }
            }

            cursor.close();
        }

        prayerProgressText.setText(
                "Today's Prayer Progress\n" +
                        completed + " / 5 prayers completed"
        );
    }

    private void updateNextPrayer() {

        try {
            // Default calculation location: Riyadh
            Coordinates coordinates = new Coordinates(
                    24.7136,
                    46.6753
            );

            Calendar calendar = Calendar.getInstance();

            DateComponents today = DateComponents.from(
                    calendar.getTime()
            );

            PrayerTimes prayerTimes = new PrayerTimes(
                    coordinates,
                    today,
                    CalculationMethod.UMM_AL_QURA.getParameters()
            );

            Prayer nextPrayer = prayerTimes.nextPrayer();
            Date nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer);

            // If today's prayers have passed, display tomorrow's Fajr
            if (nextPrayer == Prayer.NONE || nextPrayerTime == null) {

                calendar.add(Calendar.DAY_OF_YEAR, 1);

                DateComponents tomorrow = DateComponents.from(
                        calendar.getTime()
                );

                PrayerTimes tomorrowPrayerTimes = new PrayerTimes(
                        coordinates,
                        tomorrow,
                        CalculationMethod.UMM_AL_QURA.getParameters()
                );

                nextPrayer = Prayer.FAJR;
                nextPrayerTime = tomorrowPrayerTimes.timeForPrayer(
                        Prayer.FAJR
                );
            }

            if (nextPrayerTime != null) {

                String prayerName = nextPrayer.toString();

                String formattedTime = new SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                ).format(nextPrayerTime);

                nextPrayerText.setText(
                        "Next Prayer\n" +
                                prayerName + " at " + formattedTime
                );

            } else {
                nextPrayerText.setText(
                        "Next Prayer\nUnable to calculate prayer time"
                );
            }

        } catch (Exception e) {
            nextPrayerText.setText(
                    "Next Prayer\nUnable to calculate prayer time"
            );
        }
    }
}
