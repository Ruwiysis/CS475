package com.cs475.salahcompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PrayerLogAdapter extends RecyclerView.Adapter<PrayerLogAdapter.LogViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(PrayerLog log);
    }

    private final List<PrayerLog> logs = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public PrayerLogAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void setLogs(List<PrayerLog> newLogs) {
        logs.clear();
        logs.addAll(newLogs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prayer_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        PrayerLog log = logs.get(position);

        holder.prayerName.setText(log.getPrayerName());
        holder.date.setText(log.getDate());

        String status = log.getStatus();
        if ("on_time".equals(status)) {
            holder.status.setText("On Time");
        } else if ("late".equals(status)) {
            holder.status.setText("Late");
        } else {
            holder.status.setText("Missed");
        }

        holder.deleteButton.setOnClickListener(v ->
                listener.onDeleteClick(log));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView prayerName, date, status;
        ImageButton deleteButton;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            prayerName = itemView.findViewById(R.id.logPrayerName);
            date = itemView.findViewById(R.id.logDate);
            status = itemView.findViewById(R.id.logStatus);
            deleteButton = itemView.findViewById(R.id.deleteLogButton);
        }
    }
}
