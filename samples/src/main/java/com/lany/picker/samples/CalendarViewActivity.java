package com.lany.picker.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.lany.picker.CalendarView;

public class CalendarViewActivity extends AppCompatActivity {

    private CalendarView mCalendarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCalendarView=(CalendarView)findViewById(R.id.calendarView);
//        mCalendarView.setFocusedMonthDateColor(Color.parseColor("#555555"));
//        mCalendarView.setSelectedWeekBackgroundColor(Color.parseColor("#80dfdfdf"));
//        mCalendarView.setUnfocusedMonthDateColor(Color.parseColor("#bbbbbb"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
