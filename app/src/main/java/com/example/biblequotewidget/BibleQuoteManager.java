package com.example.biblequotewidget;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BibleQuoteManager {
    private static final String TAG = "BibleQuoteManager";
    private static final String BASE_URL = "https://bible-api.com/";
    private static final String TRANSLATION = "kjv";
    
    // Theme to Bible books mapping
    private static final Map<String, List<String>> THEME_TO_BOOKS = new HashMap<>();
    
    static {
        // Hope theme - books with verses about hope
        List<String> hopeBooks = new ArrayList<>();
        hopeBooks.add("ROM"); // Romans
        hopeBooks.add("PSA"); // Psalms
        hopeBooks.add("ISA"); // Isaiah
        hopeBooks.add("JER"); // Jeremiah
        THEME_TO_BOOKS.put("hope", hopeBooks);
        
        // Love theme - books with verses about love
        List<String> loveBooks = new ArrayList<>();
        loveBooks.add("1CO"); // 1 Corinthians
        loveBooks.add("1JN"); // 1 John
        loveBooks.add("SNG"); // Song of Solomon
        loveBooks.add("JHN"); // John
        THEME_TO_BOOKS.put("love", loveBooks);
        
        // Strength theme - books with verses about strength
        List<String> strengthBooks = new ArrayList<>();
        strengthBooks.add("PSA"); // Psalms
        strengthBooks.add("ISA"); // Isaiah
        strengthBooks.add("PHP"); // Philippians
        strengthBooks.add("2CO"); // 2 Corinthians
        THEME_TO_BOOKS.put("strength", strengthBooks);
        
        // Motivation theme - books with verses about motivation and encouragement
        List<String> motivationBooks = new ArrayList<>();
        motivationBooks.add("PHP"); // Philippians
        motivationBooks.add("JOS"); // Joshua
        motivationBooks.add("2TI"); // 2 Timothy
        motivationBooks.add("HEB"); // Hebrews
        THEME_TO_BOOKS.put("motivation", motivationBooks);
        
        // Wisdom theme - books with verses about wisdom
        List<String> wisdomBooks = new ArrayList<>();
        wisdomBooks.add("PRO"); // Proverbs
        wisdomBooks.add("ECC"); // Ecclesiastes
        wisdomBooks.add("JOB"); // Job
        wisdomBooks.add("JAM"); // James
        THEME_TO_BOOKS.put("wisdom", wisdomBooks);
        
        // Comfort theme - books with verses about comfort
        List<String> comfortBooks = new ArrayList<>();
        comfortBooks.add("PSA"); // Psalms
        comfortBooks.add("ISA"); // Isaiah
        comfortBooks.add("MAT"); // Matthew
        comfortBooks.add("2CO"); // 2 Corinthians
        THEME_TO_BOOKS.put("comfort", comfortBooks);
        
        // Philosophical theme - books with philosophical content
        List<String> philosophicalBooks = new ArrayList<>();
        philosophicalBooks.add("ECC"); // Ecclesiastes
        philosophicalBooks.add("JOB"); // Job
        philosophicalBooks.add("PRO"); // Proverbs
        philosophicalBooks.add("ROM"); // Romans
        THEME_TO_BOOKS.put("philosophical", philosophicalBooks);
    }
    
    private final Context context;
    private final Executor executor;
    
    public BibleQuoteManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public interface QuoteCallback {
        void onQuoteReceived(BibleQuote quote);
        void onError(String error);
    }
    
    public void getRandomQuoteByTheme(String theme, QuoteCallback callback) {
        executor.execute(() -> {
            try {
                List<String> books = THEME_TO_BOOKS.get(theme.toLowerCase());
                if (books == null || books.isEmpty()) {
                    // If theme not found, use all books
                    fetchRandomQuote("NT", callback);
                } else {
                    // Get a random book from the theme
                    String randomBook = books.get(new Random().nextInt(books.size()));
                    fetchRandomQuote(randomBook, callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting quote by theme", e);
                callback.onError("Failed to get quote: " + e.getMessage());
            }
        });
    }
    
    private void fetchRandomQuote(String bookFilter, QuoteCallback callback) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            // Construct URL for random verse from specified book(s)
            String urlString = BASE_URL + "data/" + TRANSLATION + "/random/" + bookFilter;
            URL url = new URL(urlString);
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                callback.onError("Server returned code: " + responseCode);
                return;
            }
            
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            BibleQuote quote = parseQuoteResponse(response.toString());
            callback.onQuoteReceived(quote);
            
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
            callback.onError("Network error: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
            callback.onError("Error parsing response: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }
    }
    
    private BibleQuote parseQuoteResponse(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        
        String reference = jsonObject.getString("reference");
        String text = jsonObject.getString("text").trim();
        String translation = jsonObject.getString("translation_name");
        
        return new BibleQuote(text, reference, translation, determineThemeFromReference(reference));
    }
    
    private String determineThemeFromReference(String reference) {
        // Simple logic to determine theme from reference
        // In a real app, this would be more sophisticated
        String bookCode = extractBookCode(reference);
        
        for (Map.Entry<String, List<String>> entry : THEME_TO_BOOKS.entrySet()) {
            if (entry.getValue().contains(bookCode)) {
                return entry.getKey();
            }
        }
        
        return "wisdom"; // Default theme
    }
    
    private String extractBookCode(String reference) {
        // Extract book code from reference like "John 3:16"
        // This is a simplified version
        String book = reference.split(" ")[0].toUpperCase();
        
        // Map common book names to codes
        switch (book) {
            case "JOHN": return "JHN";
            case "PSALMS": case "PSALM": return "PSA";
            case "PROVERBS": return "PRO";
            case "ISAIAH": return "ISA";
            case "MATTHEW": return "MAT";
            case "ROMANS": return "ROM";
            case "PHILIPPIANS": return "PHP";
            case "ECCLESIASTES": return "ECC";
            // Add more mappings as needed
            default: return book.substring(0, Math.min(3, book.length()));
        }
    }
    
    public static class BibleQuote {
        private final String text;
        private final String reference;
        private final String translation;
        private final String theme;
        private boolean isSaved;
        
        public BibleQuote(String text, String reference, String translation, String theme) {
            this.text = text;
            this.reference = reference;
            this.translation = translation;
            this.theme = theme;
            this.isSaved = false;
        }
        
        public String getText() {
            return text;
        }
        
        public String getReference() {
            return reference;
        }
        
        public String getTranslation() {
            return translation;
        }
        
        public String getTheme() {
            return theme;
        }
        
        public boolean isSaved() {
            return isSaved;
        }
        
        public void setSaved(boolean saved) {
            isSaved = saved;
        }
    }
}
