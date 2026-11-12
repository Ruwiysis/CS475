package com.cs475.salahcompanion;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StepDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int currentId;
    private int totalSteps;

    private TextView tvCounter, tvTitle, tvArabic, tvTranslit, tvMeaning, tvDescription;
    private ImageView imgPosition;
    private View cardArabic;
    private Button btnPrevious, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_step_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        totalSteps = db.getAllSteps().size();

        tvCounter = findViewById(R.id.tvStepCounter);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvArabic = findViewById(R.id.tvDetailArabic);
        tvTranslit = findViewById(R.id.tvDetailTranslit);
        tvMeaning = findViewById(R.id.tvDetailMeaning);
        tvDescription = findViewById(R.id.tvDetailDescription);
        imgPosition = findViewById(R.id.imgPosition);
        cardArabic = findViewById(R.id.cardArabic);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        // DATA PASSING #1: receive the values sent by LearnSalahActivity
        int stepId = getIntent().getIntExtra("step_id", -1);
        String stepTitle = getIntent().getStringExtra("step_title");

        if (savedInstanceState != null) {
            stepId = savedInstanceState.getInt("current_id", stepId);
        }

        if (stepId == -1) {
            Toast.makeText(this, "Step not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (stepTitle != null) setTitle(stepTitle);
        loadStep(stepId);

        btnPrevious.setOnClickListener(v -> loadStep(currentId - 1));
        btnNext.setOnClickListener(v -> loadStep(currentId + 1));
    }

    private void loadStep(int id) {
        SalahStep step = db.getStepById(id);
        if (step == null) {
            Toast.makeText(this, "Step not found", Toast.LENGTH_SHORT).show();
            return;
        }
        currentId = id;

        tvCounter.setText("Step " + step.getStepOrder() + " of " + totalSteps);
        tvTitle.setText(step.getTitle());
        setTitle(step.getTitle());

        if (step.getArabicText() == null || step.getArabicText().isEmpty()) {
            cardArabic.setVisibility(View.GONE);
        } else {
            cardArabic.setVisibility(View.VISIBLE);
            tvArabic.setText(step.getArabicText());
            tvTranslit.setText(step.getTransliteration());
            tvMeaning.setText(step.getMeaning());
        }
        tvDescription.setText(step.getDescription());

        // Show the position picture only if a drawable with that name exists
        int resId = getResources().getIdentifier(step.getImageName(), "drawable", getPackageName());
        if (resId != 0) {
            imgPosition.setImageResource(resId);
            imgPosition.setVisibility(View.VISIBLE);
        } else {
            imgPosition.setVisibility(View.GONE);
        }

        btnPrevious.setEnabled(step.getStepOrder() > 1);
        btnNext.setEnabled(step.getStepOrder() < totalSteps);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_id", currentId);
    }
}