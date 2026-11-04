package com.cs475.salahcompanion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.data.DateComponents;
import com.batoulapps.adhan.Prayer;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.CalculationParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PrayerTimesActivity extends AppCompatActivity {

    private Spinner citySpinner;
    private TableLayout prayerTable;
    private TextView nextPrayerText;

    private final String[] cities = {
            "Riyadh", "Makkah", "Madinah", "Jeddah", "Dammam"
    };

    private final double[][] coordinates = {
            {24.7136, 46.6753},
            {21.3891, 39.8579},
            {24.5247, 39.5692},
            {21.4858, 39.1925},
            {26.4207, 50.0888}
    };

    private final String[] prayerNames = {
            "Fajr | الفجر",
            "Sunrise | الشروق",
            "Dhuhr | الظهر",
            "Asr | العصر",
            "Maghrib | المغرب",
            "Isha | العشاء"
    };

    private TextView[] timeViews = new TextView[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_times);

        citySpinner = findViewById(R.id.citySpinner);
        prayerTable = findViewById(R.id.prayerTable);
        nextPrayerText = findViewById(R.id.nextPrayerText);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cities
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        citySpinner.setAdapter(adapter);

        SharedPreferences preferences =
                getSharedPreferences("prayer_settings", MODE_PRIVATE);

        String savedCity = preferences.getString("city", "Riyadh");

        for (int i = 0; i < cities.length; i++) {
            if (cities[i].equals(savedCity)) {
                citySpinner.setSelection(i);
                break;
            }
        }

        createPrayerRows();
        calculatePrayerTimes(citySpinner.getSelectedItemPosition());

        citySpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        preferences.edit()
                                .putString("city", cities[position])
                                .apply();

                        calculatePrayerTimes(position);
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    private void createPrayerRows() {

        prayerTable.removeAllViews();

        for (int i = 0; i < prayerNames.length; i++) {

            TableRow row = new TableRow(this);
            row.setPadding(12, 18, 12, 18);

            TextView name = new TextView(this);
            name.setText(prayerNames[i]);
            name.setTextSize(16);
            name.setTextColor(getColor(R.color.text_primary));

            TextView time = new TextView(this);
            time.setText("--:--");
            time.setTextSize(16);
            time.setTextColor(getColor(R.color.primary));
            time.setGravity(Gravity.END);

            row.addView(name);
            row.addView(time);

            prayerTable.addView(row);

            timeViews[i] = time;
        }
    }

    private void calculatePrayerTimes(int cityIndex) {

        try {
            Coordinates cityCoordinates = new Coordinates(
                    coordinates[cityIndex][0],
                    coordinates[cityIndex][1]
            );

            CalculationParameters parameters =
                    CalculationMethod.UMM_AL_QURA.getParameters();

            DateComponents date = DateComponents.from(new Date());

            PrayerTimes prayerTimes = new PrayerTimes(
                    cityCoordinates,
                    date,
                    parameters
            );

            Date[] times = {
                    prayerTimes.fajr,
                    prayerTimes.sunrise,
                    prayerTimes.dhuhr,
                    prayerTimes.asr,
                    prayerTimes.maghrib,
                    prayerTimes.isha
            };

            SimpleDateFormat formatter =
                    new SimpleDateFormat("h:mm a", Locale.ENGLISH);

            formatter.setTimeZone(
                    TimeZone.getTimeZone("Asia/Riyadh")
            );

            for (int i = 0; i < times.length; i++) {
                timeViews[i].setText(formatter.format(times[i]));
                timeViews[i].setTypeface(null, Typeface.NORMAL);
                timeViews[i].setTextColor(getColor(R.color.primary));
            }

            Prayer nextPrayer = prayerTimes.nextPrayer();

            String nextName = "No upcoming prayer";

            if (nextPrayer != Prayer.NONE) {
                switch (nextPrayer) {
                    case FAJR:
                        nextName = "Fajr | الفجر";
                        break;
                    case SUNRISE:
                        nextName = "Sunrise | الشروق";
                        break;
                    case DHUHR:
                        nextName = "Dhuhr | الظهر";
                        break;
                    case ASR:
                        nextName = "Asr | العصر";
                        break;
                    case MAGHRIB:
                        nextName = "Maghrib | المغرب";
                        break;
                    case ISHA:
                        nextName = "Isha | العشاء";
                        break;
                }
            }

            nextPrayerText.setText("Next Prayer: " + nextName);

            int nextIndex = nextPrayer.ordinal();

            if (nextIndex >= 0 && nextIndex < timeViews.length) {
                timeViews[nextIndex].setTypeface(null, Typeface.BOLD);
                timeViews[nextIndex].setTextColor(
                        getColor(R.color.accent)
                );
            }

        } catch (Exception e) {
            nextPrayerText.setText("Unable to calculate prayer times.");
        }
    }
}
