package com.cs475.salahcompanion;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrayerTrackerActivity extends AppCompatActivity
        implements ConfirmDeleteDialogFragment.ConfirmDeleteListener {

    private DatabaseHelper databaseHelper;
    private EditText dateInput;
    private Spinner prayerSpinner, statusSpinner;
    private PrayerLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_tracker);

        databaseHelper = new DatabaseHelper(this);

        dateInput = findViewById(R.id.prayerDateInput);
        prayerSpinner = findViewById(R.id.prayerNameSpinner);
        statusSpinner = findViewById(R.id.prayerStatusSpinner);

        Button addButton = findViewById(R.id.addPrayerButton);
        SearchView searchView = findViewById(R.id.prayerSearchView);
        RecyclerView recyclerView = findViewById(R.id.prayerLogsRecyclerView);

        String today = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dateInput.setText(today);

        adapter = new PrayerLogAdapter(log ->
                ConfirmDeleteDialogFragment.newInstance(log.getId())
                        .show(getSupportFragmentManager(), "delete_dialog"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> addPrayerRecord());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPrayerLogs(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPrayerLogs(newText);
                return true;
            }
        });

        loadPrayerLogs();
    }

    private void addPrayerRecord() {
        String date = dateInput.getText().toString().trim();

        if (!isValidDate(date)) {
            dateInput.setError("Enter date as yyyy-MM-dd");
            return;
        }

        String prayer = prayerSpinner.getSelectedItem().toString();
        String selectedStatus = statusSpinner.getSelectedItem().toString();

        String status;
        if ("On Time".equals(selectedStatus)) {
            status = "on_time";
        } else if ("Late".equals(selectedStatus)) {
            status = "late";
        } else {
            status = "missed";
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor existing = db.rawQuery(
                "SELECT id FROM prayer_log WHERE date = ? AND prayer_name = ?",
                new String[]{date, prayer});

        boolean alreadyExists;
        try {
            alreadyExists = existing.moveToFirst();
        } finally {
            existing.close();
        }

        if (alreadyExists) {
            Toast.makeText(this,
                    "This prayer already has a record for that date.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("prayer_name", prayer);
        values.put("status", status);
        values.put("created_at", System.currentTimeMillis());

        long result = db.insert("prayer_log", null, values);

        if (result != -1) {
            if ("on_time".equals(status)) {
                StatsManager.addXp(this, 10);
            } else if ("late".equals(status)) {
                StatsManager.addXp(this, 5);
            }

            Toast.makeText(this, "Prayer record added!",
                    Toast.LENGTH_SHORT).show();

            loadPrayerLogs();
        } else {
            Toast.makeText(this, "Could not save prayer record.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);

        try {
            Date parsed = format.parse(date);
            return parsed != null && format.format(parsed).equals(date);
        } catch (Exception e) {
            return false;
        }
    }

    private void loadPrayerLogs() {
        List<PrayerLog> logs = new ArrayList<>();

        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT id, date, prayer_name, status FROM prayer_log ORDER BY date DESC, id DESC",
                null);

        try {
            while (cursor.moveToNext()) {
                logs.add(new PrayerLog(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        } finally {
            cursor.close();
        }

        adapter.setLogs(logs);
    }

    private void searchPrayerLogs(String query) {
        List<PrayerLog> logs = new ArrayList<>();

        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                "SELECT id, date, prayer_name, status FROM prayer_log " +
                        "WHERE prayer_name LIKE ? OR date LIKE ? " +
                        "ORDER BY date DESC, id DESC",
                new String[]{"%" + query + "%", "%" + query + "%"});

        try {
            while (cursor.moveToNext()) {
                logs.add(new PrayerLog(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            }
        } finally {
            cursor.close();
        }

        adapter.setLogs(logs);
    }

    @Override
    public void onDeleteConfirmed(int logId) {
        int deleted = databaseHelper.getWritableDatabase().delete(
                "prayer_log",
                "id = ?",
                new String[]{String.valueOf(logId)});

        if (deleted > 0) {
            Toast.makeText(this, "Prayer record deleted.",
                    Toast.LENGTH_SHORT).show();
            loadPrayerLogs();
        } else {
            Toast.makeText(this, "Could not delete record.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
