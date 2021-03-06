package com.luk.timetable2.listeners.SettingsActivity.v7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.preference.Preference;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luk.timetable2.R;
import com.luk.timetable2.Utils;
import com.luk.timetable2.models.Lesson;
import com.luk.timetable2.tasks.RestoreLessonTask;

import java.util.Arrays;
import java.util.List;

/**
 * Created by luk on 9/22/15.
 */
public class RestoreLessonsListener implements Preference.OnPreferenceClickListener {
    private final Activity mActivity;

    public RestoreLessonsListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        final Context context = preference.getContext();
        final Resources resources = context.getResources();
        final View layout = View.inflate(context, R.layout.dialog_restore_lessons, null);
        final LinearLayout view = (LinearLayout) layout.findViewById(R.id.lessons);
        final List<String> days = Arrays.asList(resources.getStringArray(R.array.days));

        for (int day = 0; day < 5; day++) {
            List<Lesson> lessons = Utils.getHiddenLessons(day);

            if (lessons.size() > 0) {
                TextView textView = new TextView(context);
                textView.setText(String.format("%s:", days.get(day)));
                textView.setPadding(0, 15, 0, 15);

                view.addView(textView);

                for (Lesson lesson : lessons) {
                    String name = lesson.getName();

                    if (lesson.getGroupNumber() != null) {
                        name += String.format(" (%s)", lesson.getGroupNumber());
                    }

                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setTag(lesson.getId());
                    checkBox.setText(String.format("%s: %s", lesson.getTime(), name));

                    view.addView(checkBox);
                }
            }
        }

        if (view.getChildCount() > 0) {
            new AlertDialog.Builder(context)
                    .setTitle(resources.getString(R.string.restore_title))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < view.getChildCount(); i++) {
                                if (view.getChildAt(i).getTag() != null) {
                                    CheckBox checkBox = (CheckBox) view.getChildAt(i);

                                    if (checkBox.isChecked()) {
                                        new RestoreLessonTask(checkBox.getTag().toString()).execute();
                                    }
                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setView(layout)
                    .show();
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(resources.getString(R.string.error_title))
                    .setMessage(resources.getString(R.string.restore_empty))
                    .setPositiveButton(android.R.string.yes, null)
                    .show();
        }

        return false;
    }
}
