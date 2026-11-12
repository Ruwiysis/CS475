package com.cs475.salahcompanion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class AthkarActivity extends AppCompatActivity implements AthkarAdapter.Listener {

    private DatabaseHelper db;
    private AthkarAdapter adapter;
    private TextView tvEmpty, tvProgress;
    private SearchView searchView;

    private String currentTab = "morning";   // morning | evening | favorites
    private HashMap<Integer, Integer> counts = new HashMap<>();
    private final HashSet<Integer> favoriteIds = new HashSet<>();
    private ArrayList<Dhikr> currentList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_athkar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString("tab", "morning");
            HashMap<Integer, Integer> saved =
                    (HashMap<Integer, Integer>) savedInstanceState.getSerializable("counts");
            if (saved != null) counts = saved;
        }

        db = new DatabaseHelper(this);
        favoriteIds.addAll(db.getFavoriteIds());

        tvEmpty = findViewById(R.id.tvEmptyAthkar);
        tvProgress = findViewById(R.id.tvAthkarProgress);
        searchView = findViewById(R.id.searchAthkar);

        RecyclerView rv = findViewById(R.id.rvAthkar);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AthkarAdapter(currentList, counts, favoriteIds, this);
        rv.setAdapter(adapter);

        // Morning / Evening / Favourites
        MaterialButtonToggleGroup toggle = findViewById(R.id.toggleCategory);
        if (currentTab.equals("evening")) toggle.check(R.id.btnEvening);
        else if (currentTab.equals("favorites")) toggle.check(R.id.btnFavorites);
        else toggle.check(R.id.btnMorning);

        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnMorning) currentTab = "morning";
            else if (checkedId == R.id.btnEvening) currentTab = "evening";
            else currentTab = "favorites";
            searchView.setQuery("", false);
            searchView.clearFocus();
            loadTab();
        });

        // SEARCH
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (runSearch(query).isEmpty()) {
                    Toast.makeText(AthkarActivity.this, "No athkar found", Toast.LENGTH_SHORT).show();
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                runSearch(newText);
                return true;
            }
        });

        loadTab();
    }

    private void loadTab() {
        ArrayList<Dhikr> list = currentTab.equals("favorites")
                ? db.getAllFavorites()          // SELECT ALL favourites
                : db.getAthkarByCategory(currentTab);
        showList(list, currentTab.equals("favorites")
                ? "No favourites yet. Tap the star on any dhikr to save it."
                : "No athkar found");
    }

    private ArrayList<Dhikr> runSearch(String text) {
        String q = text.trim();
        if (q.isEmpty()) {
            loadTab();
            return currentList;
        }
        ArrayList<Dhikr> result = db.searchAthkar(q);
        showList(result, "No athkar found for \"" + q + "\"");
        return result;
    }

    private void showList(ArrayList<Dhikr> list, String emptyMessage) {
        currentList = list;
        adapter.updateList(list);
        tvEmpty.setText(emptyMessage);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        updateProgress();
    }

    private boolean isSearching() {
        return searchView.getQuery().toString().trim().length() > 0;
    }

    private void updateProgress() {
        if (currentTab.equals("favorites") || isSearching() || currentList.isEmpty()) {
            tvProgress.setVisibility(View.GONE);
            return;
        }
        int done = 0;
        for (Dhikr d : currentList) {
            if (counts.getOrDefault(d.getId(), 0) >= d.getRepeatCount()) done++;
        }
        tvProgress.setVisibility(View.VISIBLE);
        tvProgress.setText(done + " of " + currentList.size() + " completed");
    }

    @Override
    public void onDhikrTapped(Dhikr d, int count, boolean alreadyDone) {
        if (alreadyDone) {
            Toast.makeText(this, "Already completed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (count == d.getRepeatCount()) {
            Toast.makeText(this, "Completed ✓", Toast.LENGTH_SHORT).show();
            updateProgress();
            checkSetComplete();
        }
    }

    private void checkSetComplete() {
        if (currentTab.equals("favorites") || isSearching()) return;
        for (Dhikr d : currentList) {
            if (counts.getOrDefault(d.getId(), 0) < d.getRepeatCount()) return;
        }
        String name = currentTab.equals("morning") ? "Morning" : "Evening";
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
        SharedPreferences sp = getSharedPreferences("athkar_rewards", MODE_PRIVATE);
        String key = currentTab + "_" + today;

        if (!sp.getBoolean(key, false)) {
            StatsManager.addXp(this, 15);   // XP only once per set per day
            sp.edit().putBoolean(key, true).apply();
            Toast.makeText(this, name + " athkar complete – +15 XP", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, name + " athkar complete", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFavoriteClicked(Dhikr d, int position) {
        if (favoriteIds.contains(d.getId())) {
            // DELETE
            if (db.deleteFavorite(d.getId())) {
                favoriteIds.remove(d.getId());
                Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                if (currentTab.equals("favorites") && !isSearching()) loadTab();
                else adapter.notifyItemChanged(position);
            }
        } else {
            // INSERT
            if (db.insertFavorite(d.getId())) {
                favoriteIds.add(d.getId());
                Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onDhikrLongPressed(Dhikr d) {
        AthkarDetailDialogFragment.newInstance(d)
                .show(getSupportFragmentManager(), "athkar_detail");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", currentTab);
        outState.putSerializable("counts", counts);
    }
}