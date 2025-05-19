package com.example.biblequotewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ConfigurationActivity}
 */
public class BibleQuoteWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "com.example.biblequotewidget.WidgetPrefs";
    private static final String PREF_PREFIX_KEY = "widget_";
    private static final String PREF_THEME_KEY = "theme_";
    private static final String PREF_APPEARANCE_KEY = "appearance_";
    
    public static final String ACTION_UPDATE_QUOTE = "com.example.biblequotewidget.ACTION_UPDATE_QUOTE";
    public static final String ACTION_SAVE_QUOTE = "com.example.biblequotewidget.ACTION_SAVE_QUOTE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (ACTION_UPDATE_QUOTE.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        } else if (ACTION_SAVE_QUOTE.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Toggle save status of current quote
                toggleSaveQuote(context, appWidgetId);
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
            }
        }
    }

    private void toggleSaveQuote(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String quoteKey = PREF_PREFIX_KEY + appWidgetId + "_current_quote";
        String referenceKey = PREF_PREFIX_KEY + appWidgetId + "_current_reference";
        String themeKey = PREF_PREFIX_KEY + appWidgetId + "_current_theme";
        String savedKey = PREF_PREFIX_KEY + appWidgetId + "_is_saved";
        
        boolean isSaved = prefs.getBoolean(savedKey, false);
        
        if (isSaved) {
            // Remove from saved quotes
            editor.putBoolean(savedKey, false);
        } else {
            // Add to saved quotes
            String quote = prefs.getString(quoteKey, "");
            String reference = prefs.getString(referenceKey, "");
            String theme = prefs.getString(themeKey, "");
            
            if (!quote.isEmpty() && !reference.isEmpty()) {
                // Save the current quote
                editor.putBoolean(savedKey, true);
                
                // Also add to saved quotes list
                int savedCount = prefs.getInt("saved_quotes_count", 0);
                editor.putString("saved_quote_" + savedCount, quote);
                editor.putString("saved_reference_" + savedCount, reference);
                editor.putString("saved_theme_" + savedCount, theme);
                editor.putInt("saved_quotes_count", savedCount + 1);
            }
        }
        
        editor.apply();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        for (int appWidgetId : appWidgetIds) {
            prefs.remove(PREF_THEME_KEY + appWidgetId);
            prefs.remove(PREF_APPEARANCE_KEY + appWidgetId);
        }
        prefs.apply();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        QuoteUpdateService.scheduleUpdates(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        QuoteUpdateService.cancelUpdates(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Get preferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String theme = prefs.getString(PREF_THEME_KEY + appWidgetId, "wisdom");
        String appearance = prefs.getString(PREF_APPEARANCE_KEY + appWidgetId, "light");
        
        // Create RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        
        // Set appearance based on user preference
        if ("dark".equals(appearance)) {
            views.setInt(R.id.widget_layout, "setBackgroundResource", R.color.black);
            views.setTextColor(R.id.quote_text, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.reference_text, context.getResources().getColor(R.color.lightGray));
            views.setTextColor(R.id.theme_text, context.getResources().getColor(R.color.lightGray));
        } else {
            views.setInt(R.id.widget_layout, "setBackgroundResource", R.color.white);
            views.setTextColor(R.id.quote_text, context.getResources().getColor(R.color.black));
            views.setTextColor(R.id.reference_text, context.getResources().getColor(R.color.darkGray));
            views.setTextColor(R.id.theme_text, context.getResources().getColor(R.color.darkGray));
        }
        
        // Check if we have a saved quote
        String quoteText = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_current_quote", "");
        String referenceText = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_current_reference", "");
        String themeText = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "_current_theme", "");
        boolean isSaved = prefs.getBoolean(PREF_PREFIX_KEY + appWidgetId + "_is_saved", false);
        
        if (!quoteText.isEmpty() && !referenceText.isEmpty()) {
            // We have a cached quote, use it
            views.setTextViewText(R.id.quote_text, quoteText);
            views.setTextViewText(R.id.reference_text, referenceText);
            views.setTextViewText(R.id.theme_text, themeText.toUpperCase());
            views.setImageViewResource(R.id.save_icon, 
                    isSaved ? R.drawable.ic_saved : R.drawable.ic_not_saved);
        } else {
            // No cached quote, fetch a new one
            fetchNewQuote(context, appWidgetManager, appWidgetId, theme, views);
        }
        
        // Set up click intent for configuration
        Intent configIntent = new Intent(context, ConfigurationActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, 
                configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.quote_text, configPendingIntent);
        
        // Set up long-press intent for saving
        Intent saveIntent = new Intent(context, BibleQuoteWidgetProvider.class);
        saveIntent.setAction(ACTION_SAVE_QUOTE);
        saveIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent savePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, 
                saveIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.save_icon, savePendingIntent);
        
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void fetchNewQuote(Context context, AppWidgetManager appWidgetManager, 
                                     int appWidgetId, String theme, RemoteViews views) {
        // Set default values while loading
        views.setTextViewText(R.id.quote_text, "Loading quote...");
        views.setTextViewText(R.id.reference_text, "");
        views.setTextViewText(R.id.theme_text, theme.toUpperCase());
        
        // Update widget with loading state
        appWidgetManager.updateAppWidget(appWidgetId, views);
        
        // Create quote manager and fetch quote
        BibleQuoteManager quoteManager = new BibleQuoteManager(context);
        quoteManager.getRandomQuoteByTheme(theme, new BibleQuoteManager.QuoteCallback() {
            @Override
            public void onQuoteReceived(BibleQuoteManager.BibleQuote quote) {
                // Update widget with new quote
                views.setTextViewText(R.id.quote_text, quote.getText());
                views.setTextViewText(R.id.reference_text, quote.getReference());
                views.setTextViewText(R.id.theme_text, quote.getTheme().toUpperCase());
                views.setImageViewResource(R.id.save_icon, R.drawable.ic_not_saved);
                
                // Save the quote to preferences
                SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_current_quote", quote.getText());
                prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_current_reference", quote.getReference());
                prefs.putString(PREF_PREFIX_KEY + appWidgetId + "_current_theme", quote.getTheme());
                prefs.putBoolean(PREF_PREFIX_KEY + appWidgetId + "_is_saved", false);
                prefs.apply();
                
                // Update widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            @Override
            public void onError(String error) {
                // Show error in widget
                views.setTextViewText(R.id.quote_text, "Could not load quote. Please check your internet connection.");
                views.setTextViewText(R.id.reference_text, "");
                
                // Update widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }
}
