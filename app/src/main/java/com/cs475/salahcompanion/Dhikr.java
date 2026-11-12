package com.cs475.salahcompanion;

public class Dhikr {
    private int id, repeatCount;
    private String category, arabicText, transliteration, meaning;

    public Dhikr(int id, String category, String arabicText,
                 String transliteration, String meaning, int repeatCount) {
        this.id = id;
        this.category = category;
        this.arabicText = arabicText;
        this.transliteration = transliteration;
        this.meaning = meaning;
        this.repeatCount = repeatCount;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getArabicText() { return arabicText; }
    public String getTransliteration() { return transliteration; }
    public String getMeaning() { return meaning; }
    public int getRepeatCount() { return repeatCount; }
}