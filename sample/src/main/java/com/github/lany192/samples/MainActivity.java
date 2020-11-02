package com.github.lany192.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.lany192.picker.DatePicker;
import com.github.lany192.picker.DateTimePicker;
import com.github.lany192.picker.HourMinutePicker;
import com.github.lany192.picker.TimePicker;


public class MainActivity extends AppCompatActivity {
    DatePicker datePicker1;
    DatePicker datePicker2;
    TimePicker timePicker;
    HourMinutePicker hourMinutePicker;
    DateTimePicker dateTimePicker;

    TextView datePickerShowText;
    TextView timePickerShowText;
    TextView hourMinutePickerShowText;
    TextView dateTimePickerShowText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        init();
    }

    private void findView() {
        datePickerShowText = findViewById(R.id.date_picker_show_text);
        datePicker1 = findViewById(R.id.date_picker_1);
        datePicker2 = findViewById(R.id.date_picker_2);
        timePickerShowText = findViewById(R.id.time_picker_show_text);
        timePicker = findViewById(R.id.time_picker);
        hourMinutePickerShowText = findViewById(R.id.hour_minute_picker_show_text);
        hourMinutePicker = findViewById(R.id.hour_minute_picker);
        dateTimePickerShowText = findViewById(R.id.date_time_picker_show_text);
        dateTimePicker = findViewById(R.id.dateTimePicker);
    }

    private void init() {
        datePicker1.setSelectionDivider(new ColorDrawable(0xffff0000));
        datePicker1.setSelectionDividerHeight(2);
        //datePicker1.setDayViewShown(false);
        datePicker1.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> datePickerShowText.setText(new StringBuilder()
                .append("DatePicker:")
                .append(year).append("年")
                .append(monthOfYear + 1).append("月")
                .append(dayOfMonth).append("日")));

        datePicker2.setSelectionDivider(new ColorDrawable(0xff008B00));
        datePicker2.setSelectionDividerHeight(4);
        //datePicker2.setDayViewShown(false);
        datePicker2.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth) -> datePickerShowText.setText(new StringBuilder()
                .append("DatePicker:")
                .append(year).append("年")
                .append(monthOfYear + 1).append("月")
                .append(dayOfMonth).append("日")));

        timePicker.setSelectionDivider(new ColorDrawable(0xff000000));
        timePicker.setSelectionDividerHeight(2);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minuteOfHour, scd) -> timePickerShowText.setText(new StringBuilder()
                .append("TimePicker:")
                .append(hourOfDay).append("时")
                .append(minuteOfHour).append("分")
                .append(scd).append("秒")));

        hourMinutePicker.setIs24HourView(false);
        hourMinutePicker.setSelectionDivider(new ColorDrawable(0xff436EEE));
        hourMinutePicker.setSelectionDividerHeight(4);
        hourMinutePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> hourMinutePickerShowText.setText(new StringBuilder()
                .append("HourMinutePicker:")
                .append(hourOfDay).append("时")
                .append(minute).append("分")));

        dateTimePicker.setSelectionDivider(new ColorDrawable(0xff000000));
        dateTimePicker.setSelectionDividerHeight(2);
        dateTimePicker.setOnChangedListener((view, year, monthOfYear, dayOfMonth, hourOfDay, minute, second) -> dateTimePickerShowText.setText(new StringBuilder()
                .append("DateTimePicker:")
                .append(year).append("年")
                .append(monthOfYear + 1).append("月")
                .append(dayOfMonth).append("日")
                .append(hourOfDay).append("时")
                .append(minute).append("分")
                .append(second).append("秒")));
    }
}
