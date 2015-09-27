package com.luk.timetable2.listeners.SettingsActivity;

import android.content.SharedPreferences;
import android.preference.Preference;

import com.luk.timetable2.activities.SettingsActivity;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by luk on 9/22/15.
 */
public class URLChangeListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SharedPreferences.Editor editor = preference.getSharedPreferences().edit();
        editor.putString("school", fixURL((String) newValue));
        editor.apply();

        SettingsActivity.getInstance().recreate();

        return false;
    }

    private String fixURL(String url) {
        if (FilenameUtils.getExtension(url).length() == 0) {
            return FilenameUtils.getFullPath(url) + FilenameUtils.getBaseName(url);
        }

        return FilenameUtils.getFullPathNoEndSeparator(url);
    }
}