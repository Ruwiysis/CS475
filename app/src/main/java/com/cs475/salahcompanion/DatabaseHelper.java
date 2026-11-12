package com.cs475.salahcompanion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "salah_companion.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private Dhikr cursorToDhikr(Cursor c) {
        return new Dhikr(c.getInt(0), c.getString(1), c.getString(2),
                c.getString(3), c.getString(4), c.getInt(5));
    }

    // INSERT
    public boolean insertFavorite(int athkarId) {
        if (isFavorite(athkarId)) return false;
        ContentValues v = new ContentValues();
        v.put("athkar_id", athkarId);
        v.put("saved_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",
                java.util.Locale.ENGLISH).format(new java.util.Date()));
        return getWritableDatabase().insert("athkar_favorites", null, v) != -1;
    }

    // DELETE
    public boolean deleteFavorite(int athkarId) {
        return getWritableDatabase().delete("athkar_favorites", "athkar_id = ?",
                new String[]{String.valueOf(athkarId)}) > 0;
    }

    // SELECT ALL (favourites, joined with the athkar table)
    public ArrayList<Dhikr> getAllFavorites() {
        ArrayList<Dhikr> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT a.id, a.category, a.arabic_text, a.transliteration, a.meaning, a.repeat_count " +
                        "FROM athkar a JOIN athkar_favorites f ON a.id = f.athkar_id " +
                        "ORDER BY f.id DESC", null);
        try {
            while (c.moveToNext()) list.add(cursorToDhikr(c));
        } finally {
            c.close();
        }
        return list;
    }

    // SEARCH
    public ArrayList<Dhikr> searchAthkar(String q) {
        ArrayList<Dhikr> list = new ArrayList<>();
        String like = "%" + q + "%";
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM athkar WHERE arabic_text LIKE ? OR transliteration LIKE ? OR meaning LIKE ? ORDER BY id",
                new String[]{like, like, like});
        try {
            while (c.moveToNext()) list.add(cursorToDhikr(c));
        } finally {
            c.close();
        }
        return list;
    }

    public boolean isFavorite(int athkarId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id FROM athkar_favorites WHERE athkar_id = ?",
                new String[]{String.valueOf(athkarId)});
        try {
            return c.getCount() > 0;
        } finally {
            c.close();
        }
    }

    public ArrayList<Integer> getFavoriteIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT athkar_id FROM athkar_favorites", null);
        try {
            while (c.moveToNext()) ids.add(c.getInt(0));
        } finally {
            c.close();
        }
        return ids;
    }
    // ===================== MEMBER 2 =====================

    private void addStep(SQLiteDatabase db, int order, String title, String arabic,
                         String translit, String meaning, String desc, String image) {
        ContentValues v = new ContentValues();
        v.put("step_order", order);
        v.put("title", title);
        v.put("arabic_text", arabic);
        v.put("transliteration", translit);
        v.put("meaning", meaning);
        v.put("description", desc);
        v.put("image_name", image);
        db.insert("salah_steps", null, v);
    }

    private void seedSalahSteps(SQLiteDatabase db) {
        addStep(db, 1, "Niyyah (Intention)", "", "", "",
                "Make the intention in your heart for the prayer you are about to pray. It is not said out loud.", "pos_standing");
        addStep(db, 2, "Takbirat al-Ihram", "اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest",
                "Raise both hands to your shoulders or ears and say the takbir to begin the prayer.", "pos_standing");
        addStep(db, 3, "Opening Du'a",
                "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكَ، وَلَا إِلَهَ غَيْرُكَ",
                "Subhanaka Allahumma wa bihamdika, wa tabarakasmuka, wa ta'ala jadduka, wa la ilaha ghayruk",
                "Glory be to You, O Allah, and praise. Blessed is Your name, exalted is Your majesty, and there is no god but You.",
                "Place your right hand over your left on your chest and say this quietly in the first rak'ah.", "pos_standing");
        addStep(db, 4, "Ta'awwudh and Basmala",
                "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ، بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                "A'udhu billahi minash-shaytanir-rajim, Bismillahir-Rahmanir-Rahim",
                "I seek refuge in Allah from the accursed Satan. In the name of Allah, the Most Gracious, the Most Merciful.",
                "Say this quietly before reciting Al-Fatiha.", "pos_standing");
        addStep(db, 5, "Al-Fatiha",
                "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                "Alhamdu lillahi Rabbil-'alamin...",
                "All praise is for Allah, Lord of the worlds... Guide us to the straight path.",
                "Al-Fatiha is recited in every rak'ah; the prayer is not valid without it. Say 'Ameen' after it.", "pos_standing");
        addStep(db, 6, "Another Surah",
                "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                "Qul huwa Allahu ahad, Allahus-samad, lam yalid wa lam yulad, wa lam yakun lahu kufuwan ahad",
                "Say: He is Allah, the One. Allah, the Eternal. He neither begets nor is born, and there is none equal to Him.",
                "In the first two rak'ahs, recite any surah after Al-Fatiha. Al-Ikhlas is shown as an example.", "pos_standing");
        addStep(db, 7, "Ruku (Bowing)", "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
                "Subhana Rabbiyal-'Azim (3 times)", "Glory be to my Lord, the Most Great",
                "Say 'Allahu Akbar' and bow with your back straight and hands on your knees.", "pos_ruku");
        addStep(db, 8, "Rising from Ruku", "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ، رَبَّنَا وَلَكَ الْحَمْدُ",
                "Sami'a Allahu liman hamidah, Rabbana wa lakal-hamd",
                "Allah hears the one who praises Him. Our Lord, to You belongs all praise.",
                "Stand up straight again, raising your hands, and say this.", "pos_standing");
        addStep(db, 9, "Sujood (Prostration)", "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                "Subhana Rabbiyal-A'la (3 times)", "Glory be to my Lord, the Most High",
                "Say 'Allahu Akbar' and prostrate with forehead, nose, palms, knees and toes touching the ground.", "pos_sujood");
        addStep(db, 10, "Sitting Between Sujood", "رَبِّ اغْفِرْ لِي",
                "Rabbighfir li", "My Lord, forgive me",
                "Say 'Allahu Akbar' and sit up calmly between the two prostrations.", "pos_sitting");
        addStep(db, 11, "Second Sujood", "سُبْحَانَ رَبِّيَ الْأَعْلَى",
                "Subhana Rabbiyal-A'la (3 times)", "Glory be to my Lord, the Most High",
                "Prostrate again exactly like the first sujood. This completes one rak'ah.", "pos_sujood");
        addStep(db, 12, "Tashahhud",
                "التَّحِيَّاتُ لِلَّهِ، وَالصَّلَوَاتُ، وَالطَّيِّبَاتُ، السَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ، السَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                "At-tahiyyatu lillahi was-salawatu wat-tayyibat...",
                "All greetings, prayers and good things are for Allah... I bear witness that there is no god but Allah and that Muhammad is His servant and messenger.",
                "Sit after the second rak'ah and in the last rak'ah and recite the tashahhud.", "pos_sitting");
        addStep(db, 13, "Salawat Ibrahimiyya",
                "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
                "Allahumma salli 'ala Muhammad wa 'ala ali Muhammad...",
                "O Allah, send prayers upon Muhammad and the family of Muhammad, as You sent prayers upon Ibrahim...",
                "In the final sitting, recite this after the tashahhud.", "pos_sitting");
        addStep(db, 14, "Tasleem", "السَّلَامُ عَلَيْكُمْ وَرَحْمَةُ اللَّهِ",
                "As-salamu 'alaykum wa rahmatullah", "Peace and the mercy of Allah be upon you",
                "Turn your head to the right and say it, then to the left and say it again. The prayer is complete.", "pos_sitting");
    }

    private void addDhikr(SQLiteDatabase db, String category, String arabic,
                          String translit, String meaning, int repeat) {
        ContentValues v = new ContentValues();
        v.put("category", category);
        v.put("arabic_text", arabic);
        v.put("transliteration", translit);
        v.put("meaning", meaning);
        v.put("repeat_count", repeat);
        db.insert("athkar", null, v);
    }

    private void seedAthkar(SQLiteDatabase db) {
        addDhikr(db, "morning",
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "Asbahna wa asbahal-mulku lillah...",
                "We have reached the morning and the dominion belongs to Allah, and all praise is for Allah.", 1);
        addDhikr(db, "evening",
                "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "Amsayna wa amsal-mulku lillah...",
                "We have reached the evening and the dominion belongs to Allah, and all praise is for Allah.", 1);
        addDhikr(db, "evening", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                "A'udhu bikalimatillahit-tammati min sharri ma khalaq",
                "I seek refuge in the perfect words of Allah from the evil of what He has created.", 3);

        // Said in both morning and evening
        String[] both = {"morning", "evening"};
        for (String cat : both) {
            addDhikr(db, cat, "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
                    "Surat Al-Ikhlas", "Say: He is Allah, the One...", 3);
            addDhikr(db, cat, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                    "Surat Al-Falaq", "Say: I seek refuge in the Lord of daybreak...", 3);
            addDhikr(db, cat, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
                    "Surat An-Nas", "Say: I seek refuge in the Lord of mankind...", 3);
            addDhikr(db, cat, "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                    "Bismillahil-ladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa huwas-Sami'ul-'Alim",
                    "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.", 3);
            addDhikr(db, cat, "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
                    "Raditu billahi Rabba, wa bil-Islami dina, wa bi-Muhammadin nabiyya",
                    "I am pleased with Allah as my Lord, Islam as my religion, and Muhammad as my Prophet.", 3);
            addDhikr(db, cat, "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                    "Subhanallahi wa bihamdih", "Glory be to Allah and praise be to Him.", 100);
        }
    }

    private SalahStep cursorToStep(Cursor c) {
        return new SalahStep(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3),
                c.getString(4), c.getString(5), c.getString(6), c.getString(7));
    }

    public ArrayList<SalahStep> getAllSteps() {
        ArrayList<SalahStep> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM salah_steps ORDER BY step_order", null);
        try {
            while (c.moveToNext()) list.add(cursorToStep(c));
        } finally {
            c.close();
        }
        return list;
    }

    public ArrayList<SalahStep> searchSteps(String q) {
        ArrayList<SalahStep> list = new ArrayList<>();
        String like = "%" + q + "%";
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM salah_steps WHERE title LIKE ? OR transliteration LIKE ? OR meaning LIKE ? ORDER BY step_order",
                new String[]{like, like, like});
        try {
            while (c.moveToNext()) list.add(cursorToStep(c));
        } finally {
            c.close();
        }
        return list;
    }

    public SalahStep getStepById(int id) {
        SalahStep step = null;
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM salah_steps WHERE id = ?", new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) step = cursorToStep(c);
        } finally {
            c.close();
        }
        return step;
    }

    public ArrayList<Dhikr> getAthkarByCategory(String category) {
        ArrayList<Dhikr> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM athkar WHERE category = ? ORDER BY id", new String[]{category});
        try {
            while (c.moveToNext()) {
                list.add(new Dhikr(c.getInt(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getInt(5)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    // =================== END MEMBER 2 ===================
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE prayer_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT NOT NULL, " +
                "prayer_name TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE salah_steps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "step_order INTEGER, title TEXT, arabic_text TEXT, " +
                "transliteration TEXT, meaning TEXT, description TEXT, image_name TEXT)");

        db.execSQL("CREATE TABLE athkar (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT, arabic_text TEXT, transliteration TEXT, " +
                "meaning TEXT, repeat_count INTEGER)");

        db.execSQL("CREATE TABLE athkar_favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "athkar_id INTEGER, saved_at TEXT, " +
                "FOREIGN KEY(athkar_id) REFERENCES athkar(id))");

        db.execSQL("CREATE TABLE flashcards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "arabic_text TEXT, transliteration TEXT, meaning TEXT, " +
                "category TEXT, is_custom INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE quiz_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, score INTEGER, total INTEGER, xp_earned INTEGER)");

        seedSalahSteps(db);   // M2
        seedAthkar(db);       // M2


        // M3: Seed flashcards here.
    }

    public boolean insertPrayerLog(String date, String prayer, String status) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("prayer_name", prayer);
        values.put("status", status);

        long result = db.insert("prayer_log", null, values);
        return result != -1;
    }

    public ArrayList<PrayerLog> getAllPrayerLogs() {
        ArrayList<PrayerLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, date, prayer_name, status FROM prayer_log ORDER BY date DESC, id DESC",
                null
        );

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

        return logs;
    }

    public ArrayList<PrayerLog> searchPrayerLogs(String query) {
        ArrayList<PrayerLog> logs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, date, prayer_name, status FROM prayer_log " +
                        "WHERE prayer_name LIKE ? OR date LIKE ? " +
                        "ORDER BY date DESC, id DESC",
                new String[]{"%" + query + "%", "%" + query + "%"}
        );

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

        return logs;
    }

    public boolean deletePrayerLog(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("prayer_log", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS athkar_favorites");
        db.execSQL("DROP TABLE IF EXISTS prayer_log");
        db.execSQL("DROP TABLE IF EXISTS salah_steps");
        db.execSQL("DROP TABLE IF EXISTS athkar");
        db.execSQL("DROP TABLE IF EXISTS flashcards");
        db.execSQL("DROP TABLE IF EXISTS quiz_results");
        onCreate(db);
    }
}
