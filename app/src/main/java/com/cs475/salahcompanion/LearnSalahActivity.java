package com.cs475.salahcompanion;

import android.content.Intent;
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

import java.util.ArrayList;

public class LearnSalahActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private StepAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_learn_salah);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmptySteps);

        RecyclerView rv = findViewById(R.id.rvSteps);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // SELECT ALL: load every step from SQLite
        adapter = new StepAdapter(db.getAllSteps(), step -> {
            // DATA PASSING #1: send the step id and title to the detail screen
            Intent intent = new Intent(LearnSalahActivity.this, StepDetailActivity.class);
            intent.putExtra("step_id", step.getId());
            intent.putExtra("step_title", step.getTitle());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        // SEARCH
        SearchView searchView = findViewById(R.id.searchSteps);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (filter(query).isEmpty()) {
                    Toast.makeText(LearnSalahActivity.this,
                            "No step found for \"" + query + "\"", Toast.LENGTH_SHORT).show();
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private ArrayList<SalahStep> filter(String text) {
        String q = text.trim();
        ArrayList<SalahStep> result = q.isEmpty() ? db.getAllSteps() : db.searchSteps(q);
        adapter.updateList(result);
        tvEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        return result;
    }
}