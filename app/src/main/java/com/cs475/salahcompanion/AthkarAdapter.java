package com.cs475.salahcompanion;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AthkarAdapter extends RecyclerView.Adapter<AthkarAdapter.ViewHolder> {

    public interface Listener {
        void onDhikrTapped(Dhikr d, int count, boolean alreadyDone);
        void onFavoriteClicked(Dhikr d, int position);
        void onDhikrLongPressed(Dhikr d);
    }

    private ArrayList<Dhikr> items;
    private final HashMap<Integer, Integer> counts;
    private final HashSet<Integer> favoriteIds;
    private final Listener listener;

    public AthkarAdapter(ArrayList<Dhikr> items, HashMap<Integer, Integer> counts,
                         HashSet<Integer> favoriteIds, Listener listener) {
        this.items = items;
        this.counts = counts;
        this.favoriteIds = favoriteIds;
        this.listener = listener;
    }

    public void updateList(ArrayList<Dhikr> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dhikr, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Dhikr d = items.get(position);
        int count = counts.getOrDefault(d.getId(), 0);
        boolean done = count >= d.getRepeatCount();

        h.tvArabic.setText(d.getArabicText());
        h.tvTranslit.setText(d.getTransliteration());
        h.tvMeaning.setText(d.getMeaning());
        h.tvCounter.setText(done ? "Completed ✓" : count + " / " + d.getRepeatCount());

        if (done) {
            h.card.setCardBackgroundColor(Color.parseColor("#E5F4E4"));
        } else {
            h.card.setCardBackgroundColor(h.defaultColor);
        }

        h.btnFavorite.setImageResource(favoriteIds.contains(d.getId())
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);

        h.itemView.setOnClickListener(v -> {
            int current = counts.getOrDefault(d.getId(), 0);
            if (current >= d.getRepeatCount()) {
                listener.onDhikrTapped(d, current, true);
                return;
            }
            current++;
            counts.put(d.getId(), current);
            notifyItemChanged(h.getBindingAdapterPosition());
            listener.onDhikrTapped(d, current, false);
        });

        h.itemView.setOnLongClickListener(v -> {
            listener.onDhikrLongPressed(d);
            return true;
        });

        h.btnFavorite.setOnClickListener(v ->
                listener.onFavoriteClicked(d, h.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvCounter, tvArabic, tvTranslit, tvMeaning;
        ImageButton btnFavorite;
        ColorStateList defaultColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardDhikr);
            tvCounter = itemView.findViewById(R.id.tvCounter);
            tvArabic = itemView.findViewById(R.id.tvDhikrArabic);
            tvTranslit = itemView.findViewById(R.id.tvDhikrTranslit);
            tvMeaning = itemView.findViewById(R.id.tvDhikrMeaning);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            defaultColor = card.getCardBackgroundColor();
        }
    }
}