package com.example.biblequotewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class QuoteUpdateService extends Service {
    private static final String TAG = "QuoteUpdateService";
    private static final long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        // Update all widgets
        updateAllWidgets();
        
        return START_NOT_STICKY;
    }

    private void updateAllWidgets() {
        Intent updateIntent = new Intent(this, BibleQuoteWidgetProvider.class);
        updateIntent.setAction(BibleQuoteWidgetProvider.ACTION_UPDATE_QUOTE);
        sendBroadcast(updateIntent);
    }

    public static void scheduleUpdates(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, QuoteUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set alarm to trigger at 6:00 AM every day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past 6:00 AM, schedule for next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule the alarm
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    INTERVAL_DAY,
                    pendingIntent
            );
            Log.d(TAG, "Daily updates scheduled");
        }
    }

    public static void cancelUpdates(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, QuoteUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Daily updates canceled");
        }
    }
}
