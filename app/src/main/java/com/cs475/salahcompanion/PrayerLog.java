package com.cs475.salahcompanion;

public class PrayerLog {
    private final int id;
    private final String date;
    private final String prayerName;
    private final String status;

    public PrayerLog(int id, String date, String prayerName, String status) {
        this.id = id;
        this.date = date;
        this.prayerName = prayerName;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getPrayerName() {
        return prayerName;
    }

    public String getStatus() {
        return status;
    }
}
