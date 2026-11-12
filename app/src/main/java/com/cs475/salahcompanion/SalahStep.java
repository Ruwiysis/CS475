package com.cs475.salahcompanion;

public class SalahStep {
    private int id, stepOrder;
    private String title, arabicText, transliteration, meaning, description, imageName;

    public SalahStep(int id, int stepOrder, String title, String arabicText,
                     String transliteration, String meaning, String description, String imageName) {
        this.id = id;
        this.stepOrder = stepOrder;
        this.title = title;
        this.arabicText = arabicText;
        this.transliteration = transliteration;
        this.meaning = meaning;
        this.description = description;
        this.imageName = imageName;
    }

    public int getId() { return id; }
    public int getStepOrder() { return stepOrder; }
    public String getTitle() { return title; }
    public String getArabicText() { return arabicText; }
    public String getTransliteration() { return transliteration; }
    public String getMeaning() { return meaning; }
    public String getDescription() { return description; }
    public String getImageName() { return imageName; }
}