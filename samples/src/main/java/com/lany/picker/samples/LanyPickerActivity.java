package com.lany.picker.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lany.picker.lanypicker.LanyPicker;
import com.lany.picker.lanypicker.LanyPickerDialog;

import java.util.Calendar;

public class LanyPickerActivity extends AppCompatActivity {
    private TextView showText;
    private int hour;
    private int minute;
    private int second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lany_picker);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.show_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LanyPickerDialog(LanyPickerActivity.this,
                        new LanyPickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(LanyPicker view, int hourOfDay, int minuteOfHour, int scd) {
                                hour = hourOfDay;
                                minute = minuteOfHour;
                                second = scd;
                                updateDisplay();
                            }
                        },
                        hour, minute, second).show();
            }
        });

        LanyPicker lanyPicker = (LanyPicker) findViewById(R.id.lanyPicker);
        lanyPicker.setSelectionDivider(new ColorDrawable(0xff000000));
        lanyPicker.setSelectionDividerHeight(2);
        lanyPicker.setOnTimeChangedListener(new LanyPicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(LanyPicker view, int hourOfDay, int minuteOfHour,
                                      int scd) {
                hour = hourOfDay;
                minute = minuteOfHour;
                second = scd;
                updateDisplay();
            }
        });

        showText = (TextView) findViewById(R.id.lany_picker_show_text);
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        second = c.get(Calendar.SECOND);

        updateDisplay();
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

    private void updateDisplay() {
        showText.setText(new StringBuilder()
                .append(hour).append(":")
                .append(minute).append(":")
                .append(second));
    }

}
