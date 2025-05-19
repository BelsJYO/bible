package com.example.biblequotewidget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class SavedQuotesActivity extends Activity {

    private static final String PREFS_NAME = "com.example.biblequotewidget.WidgetPrefs";
    private ListView savedQuotesList;
    private TextView emptyView;
    private List<SavedQuote> quotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_quotes);

        savedQuotesList = findViewById(R.id.saved_quotes_list);
        emptyView = findViewById(R.id.empty_view);
        
        loadSavedQuotes();
        
        if (quotes.isEmpty()) {
            savedQuotesList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            savedQuotesList.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            
            SavedQuotesAdapter adapter = new SavedQuotesAdapter(this, quotes);
            savedQuotesList.setAdapter(adapter);
        }
    }
    
    private void loadSavedQuotes() {
        quotes = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedCount = prefs.getInt("saved_quotes_count", 0);
        
        for (int i = 0; i < savedCount; i++) {
            String quote = prefs.getString("saved_quote_" + i, "");
            String reference = prefs.getString("saved_reference_" + i, "");
            String theme = prefs.getString("saved_theme_" + i, "");
            
            if (!quote.isEmpty() && !reference.isEmpty()) {
                quotes.add(new SavedQuote(quote, reference, theme, i));
            }
        }
    }
    
    private void deleteQuote(int position) {
        if (position >= 0 && position < quotes.size()) {
            SavedQuote quoteToDelete = quotes.get(position);
            quotes.remove(position);
            
            // Update the adapter
            ((SavedQuotesAdapter) savedQuotesList.getAdapter()).notifyDataSetChanged();
            
            // If no quotes left, show empty view
            if (quotes.isEmpty()) {
                savedQuotesList.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
            
            // Update preferences
            updateSavedQuotesPreferences();
            
            Toast.makeText(this, "Quote deleted", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void shareQuote(int position) {
        if (position >= 0 && position < quotes.size()) {
            SavedQuote quoteToShare = quotes.get(position);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    quoteToShare.getQuote() + "\n\n" + quoteToShare.getReference() + 
                    "\n\nShared from Bible Quote Widget");
            startActivity(Intent.createChooser(shareIntent, "Share Quote"));
        }
    }
    
    private void updateSavedQuotesPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        
        // Clear all saved quotes
        editor.remove("saved_quotes_count");
        
        // Save the current list
        for (int i = 0; i < quotes.size(); i++) {
            SavedQuote quote = quotes.get(i);
            editor.putString("saved_quote_" + i, quote.getQuote());
            editor.putString("saved_reference_" + i, quote.getReference());
            editor.putString("saved_theme_" + i, quote.getTheme());
        }
        
        editor.putInt("saved_quotes_count", quotes.size());
        editor.apply();
    }
    
    private class SavedQuotesAdapter extends ArrayAdapter<SavedQuote> {
        
        public SavedQuotesAdapter(Context context, List<SavedQuote> quotes) {
            super(context, 0, quotes);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SavedQuote quote = getItem(position);
            
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.saved_quote_item, parent, false);
            }
            
            TextView quoteText = convertView.findViewById(R.id.saved_quote_text);
            TextView referenceText = convertView.findViewById(R.id.saved_reference_text);
            TextView themeText = convertView.findViewById(R.id.saved_theme_text);
            ImageButton deleteButton = convertView.findViewById(R.id.delete_button);
            ImageButton shareButton = convertView.findViewById(R.id.share_button);
            
            quoteText.setText(quote.getQuote());
            referenceText.setText(quote.getReference());
            themeText.setText(quote.getTheme().toUpperCase());
            
            final int quotePosition = position;
            
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteQuote(quotePosition);
                }
            });
            
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareQuote(quotePosition);
                }
            });
            
            return convertView;
        }
    }
    
    public static class SavedQuote {
        private final String quote;
        private final String reference;
        private final String theme;
        private final int id;
        
        public SavedQuote(String quote, String reference, String theme, int id) {
            this.quote = quote;
            this.reference = reference;
            this.theme = theme;
            this.id = id;
        }
        
        public String getQuote() {
            return quote;
        }
        
        public String getReference() {
            return reference;
        }
        
        public String getTheme() {
            return theme;
        }
        
        public int getId() {
            return id;
        }
    }
}
