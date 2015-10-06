package com.luk.timetable2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Spinner;

import com.luk.timetable2.R;
import com.luk.timetable2.Utils;
import com.luk.timetable2.listeners.MainActivity.DayChangeListener;
import com.luk.timetable2.services.RegisterReceivers;
import com.luk.timetable2.tasks.ClassesTask;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private int mCurrentTheme;
    private int mDay;

    private ViewPager mViewPager;
    private Spinner mDaySelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTheme(mCurrentTheme = Utils.getCurrentTheme(this));
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_main);

        // Set toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar;

        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Get current day
        mDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (mDay == -1 || mDay == 5) mDay = 0; // set monday

        // Set current day
        mDaySelector = (Spinner) findViewById(R.id.day);
        mDaySelector.setOnItemSelectedListener(new DayChangeListener(this));
        mDaySelector.setSelection(mDay);

        // Set view pager
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.addOnPageChangeListener(new DayChangeListener(this));

        // Start services
        sendBroadcast(new Intent(this, RegisterReceivers.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCurrentTheme != Utils.getCurrentTheme(this)) {
            recreate();
            return;
        }

        refreshContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                new ClassesTask(this).execute();
                break;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }

        return false;
    }

    public void refreshContent() {
        mViewPager.setAdapter(new MainActivityAdapter(getSupportFragmentManager()));
    }

    public ViewPager getPager() {
        return mViewPager;
    }

    public Spinner getDaySelector() {
        return mDaySelector;
    }

    public int getDay() {
        return mDay;
    }

    public void setDay(int day) {
        mDay = day;
    }
}
