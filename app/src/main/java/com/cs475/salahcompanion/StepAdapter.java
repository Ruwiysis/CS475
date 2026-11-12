package com.cs475.salahcompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    public interface OnStepClickListener {
        void onStepClick(SalahStep step);
    }

    private ArrayList<SalahStep> steps;
    private final OnStepClickListener listener;

    public StepAdapter(ArrayList<SalahStep> steps, OnStepClickListener listener) {
        this.steps = steps;
        this.listener = listener;
    }

    public void updateList(ArrayList<SalahStep> newList) {
        this.steps = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalahStep step = steps.get(position);
        holder.tvNumber.setText(String.valueOf(step.getStepOrder()));
        holder.tvTitle.setText(step.getTitle());

        if (step.getArabicText() == null || step.getArabicText().isEmpty()) {
            holder.tvArabic.setVisibility(View.GONE);
        } else {
            holder.tvArabic.setVisibility(View.VISIBLE);
            holder.tvArabic.setText(step.getArabicText());
        }

        holder.itemView.setOnClickListener(v -> listener.onStepClick(step));
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvTitle, tvArabic;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvStepNumber);
            tvTitle = itemView.findViewById(R.id.tvStepTitle);
            tvArabic = itemView.findViewById(R.id.tvStepArabic);
        }
    }
}