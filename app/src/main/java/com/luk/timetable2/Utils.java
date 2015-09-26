package com.luk.timetable2;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LuK on 2015-05-01.
 */
public class Utils {
    private static String TAG = "Utils";

    public boolean isOnline(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static ArrayList<List<String>> getHours(Context context, Integer day) {
        ArrayList<List<String>> array = new ArrayList<>();
        File dbFile = context.getDatabasePath("db");
        SQLiteDatabase db = context.openOrCreateDatabase(dbFile.getAbsolutePath(), Context.MODE_PRIVATE, null);

        try {
            Cursor c = db.rawQuery(String.format("SELECT time FROM lessons WHERE day = '%d' AND hidden = '0' GROUP by time ORDER by _id", day), null);

            if (c.moveToFirst()) {
                do {
                    array.add(Collections.singletonList(c.getString(0)));
                } while (c.moveToNext());
            }

            c.close();
            db.close();
        } catch(Exception e) {
            Log.e(TAG, "", e);
            return null;
        }

        return array;
    }

    public static ArrayList<List<String>> getLessonsForHour(Context context, Integer day, String hour) {
        ArrayList<List<String>> array = new ArrayList<>();
        File dbFile = context.getDatabasePath("db");
        SQLiteDatabase db = context.openOrCreateDatabase(dbFile.getAbsolutePath(), Context.MODE_PRIVATE, null);

        try {
            Cursor c = db.rawQuery(String.format("SELECT * from lessons WHERE day = '%d' AND time = '%s' AND hidden = '0'", day, hour), null);

            if (c.moveToFirst()) {
                do {
                    array.add(Arrays.asList(c.getString(2), c.getString(3), c.getString(4), String.valueOf(c.getInt(0))));
                } while (c.moveToNext());
            }

            c.close();
            db.close();
        } catch(Exception e) {
            Log.e(TAG, "", e);
            return null;
        }

        return array;
    }

    public static ArrayList<List<String>> getHiddenLessons(Context context, Integer day) {
        ArrayList<List<String>> array = new ArrayList<>();
        File dbFile = context.getDatabasePath("db");
        SQLiteDatabase db = context.openOrCreateDatabase(dbFile.getAbsolutePath(), Context.MODE_PRIVATE, null);

        try {
            Cursor c = db.rawQuery(String.format("SELECT * from lessons WHERE day = '%d' AND hidden = '1'", day), null);

            if (c.moveToFirst()) {
                do {
                    array.add(Arrays.asList(c.getString(2), c.getString(3), c.getString(4), String.valueOf(c.getInt(0))));
                } while (c.moveToNext());
            }

            c.close();
            db.close();
        } catch(Exception e) {
            Log.e(TAG, "", e);
            return null;
        }

        return array;
    }

    public static void refreshWidgets(Context context) {
        HashMap<Integer, int[]> widgets = new HashMap<>();

        widgets.put(0, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.dark.WidgetProvider.class)));
        widgets.put(1, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.dark.red.WidgetProvider.class)));
        widgets.put(2, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.dark.green.WidgetProvider.class)));
        widgets.put(3, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.dark.blue.WidgetProvider.class)));

        widgets.put(4, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.light.WidgetProvider.class)));
        widgets.put(5, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.light.red.WidgetProvider.class)));
        widgets.put(6, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.light.green.WidgetProvider.class)));
        widgets.put(7, AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, com.luk.timetable2.widget.light.blue.WidgetProvider.class)));

        for (int i = 0; i < widgets.size(); i++) {
            for (int widgetID : widgets.get(i)) {
                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetID, R.id.widget);
            }
        }
    }

    public static int getCurrentTheme(Activity activity) {
        HashMap<String, Integer> themes = new HashMap<>();

        themes.put("dark", R.style.AppTheme_Dark);
        themes.put("dark_red", R.style.AppTheme_Dark_Red);
        themes.put("dark_green", R.style.AppTheme_Dark_Green);
        themes.put("dark_blue", R.style.AppTheme_Dark_Blue);

        themes.put("light", R.style.AppTheme_Light);
        themes.put("light_red", R.style.AppTheme_Light_Red);
        themes.put("light_green", R.style.AppTheme_Light_Green);
        themes.put("light_blue", R.style.AppTheme_Light_Blue);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String theme = sharedPref.getString("theme", "dark");
        String themeAccent = sharedPref.getString("themeAccent", "normal");

        if (!themeAccent.equals("normal")) {
            return themes.get(String.format("%s_%s", theme, themeAccent));
        }

        return themes.get(theme);
    }

    public static Integer[] getWidgetColorsForVariant(String variant) {
        HashMap<String, Integer[]> variants = new HashMap<>();

        variants.put("dark", new Integer[] { R.color.colorPrimary_Dark, R.color.colorWidgetBG_Dark, android.R.color.white });
        variants.put("dark_red", new Integer[] { R.color.colorPrimary_Red, R.color.colorWidgetBG_Dark, android.R.color.white });
        variants.put("dark_green", new Integer[] { R.color.colorPrimary_Green, R.color.colorWidgetBG_Dark, android.R.color.white });
        variants.put("dark_blue", new Integer[] { R.color.colorPrimary_Blue, R.color.colorWidgetBG_Dark, android.R.color.white });

        variants.put("light", new Integer[] { R.color.colorPrimary_Light, R.color.colorWidgetBG_Light, android.R.color.black });
        variants.put("light_red", new Integer[] { R.color.colorPrimary_Red, R.color.colorWidgetBG_Light, android.R.color.black });
        variants.put("light_green", new Integer[] { R.color.colorPrimary_Green, R.color.colorWidgetBG_Light, android.R.color.black });
        variants.put("light_blue", new Integer[] { R.color.colorPrimary_Blue, R.color.colorWidgetBG_Light, android.R.color.black });

        return variants.get(variant);
    }

    public static Integer[] getColorsForVariant(int variant) {
        HashMap<Integer, Integer[]> variants = new HashMap<>();

        variants.put(R.style.AppTheme_Dark, new Integer[] { R.color.colorPrimary_Dark, android.R.color.white });
        variants.put(R.style.AppTheme_Dark_Red, new Integer[] { R.color.colorPrimary_Red, android.R.color.white });
        variants.put(R.style.AppTheme_Dark_Green, new Integer[] { R.color.colorPrimary_Green, android.R.color.white });
        variants.put(R.style.AppTheme_Dark_Blue, new Integer[] { R.color.colorPrimary_Blue, android.R.color.white });

        variants.put(R.style.AppTheme_Light, new Integer[] { R.color.colorPrimary_Light, android.R.color.black });
        variants.put(R.style.AppTheme_Light_Red, new Integer[] { R.color.colorPrimary_Red, android.R.color.black });
        variants.put(R.style.AppTheme_Light_Green, new Integer[] { R.color.colorPrimary_Green, android.R.color.black });
        variants.put(R.style.AppTheme_Light_Blue, new Integer[] { R.color.colorPrimary_Blue, android.R.color.black });

        return variants.get(variant);
    }
}
