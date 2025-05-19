package com.example.biblequotewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class ConfigurationActivity extends Activity {

    private static final String PREFS_NAME = "com.example.biblequotewidget.WidgetPrefs";
    private static final String PREF_PREFIX_KEY = "widget_";
    private static final String PREF_THEME_KEY = "theme_";
    private static final String PREF_APPEARANCE_KEY = "appearance_";
    private static final String PREF_NOTIFICATIONS_KEY = "notifications_";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private RadioGroup themeRadioGroup;
    private RadioGroup themeModeRadioGroup;
    private Switch notificationsSwitch;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an invalid widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        themeRadioGroup = findViewById(R.id.theme_radio_group);
        themeModeRadioGroup = findViewById(R.id.theme_mode_radio_group);
        notificationsSwitch = findViewById(R.id.notifications_switch);
        saveButton = findViewById(R.id.save_button);

        // Load saved preferences
        loadPreferences();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                updateWidget();
                
                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Load theme preference
        String theme = prefs.getString(PREF_THEME_KEY + appWidgetId, "wisdom");
        int themeId = getThemeRadioButtonId(theme);
        if (themeId != -1) {
            themeRadioGroup.check(themeId);
        }
        
        // Load appearance preference
        String appearance = prefs.getString(PREF_APPEARANCE_KEY + appWidgetId, "light");
        if ("dark".equals(appearance)) {
            themeModeRadioGroup.check(R.id.theme_dark);
        } else {
            themeModeRadioGroup.check(R.id.theme_light);
        }
        
        // Load notifications preference
        boolean notifications = prefs.getBoolean(PREF_NOTIFICATIONS_KEY + appWidgetId, true);
        notificationsSwitch.setChecked(notifications);
    }

    private void savePreferences() {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        
        // Save theme preference
        int selectedThemeId = themeRadioGroup.getCheckedRadioButtonId();
        String theme = getThemeFromRadioButtonId(selectedThemeId);
        prefs.putString(PREF_THEME_KEY + appWidgetId, theme);
        
        // Save appearance preference
        int selectedAppearanceId = themeModeRadioGroup.getCheckedRadioButtonId();
        String appearance = (selectedAppearanceId == R.id.theme_dark) ? "dark" : "light";
        prefs.putString(PREF_APPEARANCE_KEY + appWidgetId, appearance);
        
        // Save notifications preference
        boolean notifications = notificationsSwitch.isChecked();
        prefs.putBoolean(PREF_NOTIFICATIONS_KEY + appWidgetId, notifications);
        
        prefs.apply();
        
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        BibleQuoteWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
        
        // Schedule daily updates if notifications are enabled
        if (notificationsSwitch.isChecked()) {
            QuoteUpdateService.scheduleUpdates(this);
        }
    }

    private int getThemeRadioButtonId(String theme) {
        switch (theme.toLowerCase()) {
            case "hope":
                return R.id.theme_hope;
            case "love":
                return R.id.theme_love;
            case "strength":
                return R.id.theme_strength;
            case "motivation":
                return R.id.theme_motivation;
            case "wisdom":
                return R.id.theme_wisdom;
            case "comfort":
                return R.id.theme_comfort;
            case "philosophical":
                return R.id.theme_philosophical;
            default:
                return R.id.theme_wisdom;
        }
    }

    private String getThemeFromRadioButtonId(int radioButtonId) {
        if (radioButtonId == R.id.theme_hope) {
            return "hope";
        } else if (radioButtonId == R.id.theme_love) {
            return "love";
        } else if (radioButtonId == R.id.theme_strength) {
            return "strength";
        } else if (radioButtonId == R.id.theme_motivation) {
            return "motivation";
        } else if (radioButtonId == R.id.theme_wisdom) {
            return "wisdom";
        } else if (radioButtonId == R.id.theme_comfort) {
            return "comfort";
        } else if (radioButtonId == R.id.theme_philosophical) {
            return "philosophical";
        } else {
            return "wisdom"; // Default
        }
    }
}
