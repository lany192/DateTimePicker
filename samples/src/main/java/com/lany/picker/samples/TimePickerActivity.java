package com.lany.picker.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.lany.picker.HMPicker;
import com.lany.picker.HMPickerDialog;

import java.util.Calendar;

public class TimePickerActivity extends AppCompatActivity {
    int hour;
    int minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        findViewById(R.id.show_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HMPickerDialog(TimePickerActivity.this,
                        new HMPickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(HMPicker view, int hourOfDay, int minuteOfHour) {
                                hour = hourOfDay;
                                minute = minuteOfHour;
                            }
                        },
                        hour, minute, false).show();
            }
        });
        HMPicker timePicker = (HMPicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);
        timePicker.setSelectionDivider(new ColorDrawable(0xffff0000));
        timePicker.setSelectionDividerHeight(2);
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
