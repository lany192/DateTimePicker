package com.lany.picker.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lany.picker.DatePicker;
import com.lany.picker.DateTimePicker;
import com.lany.picker.HourMinutePicker;
import com.lany.picker.TimePicker;
import com.lany.picker.YMDHPicker;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView datePickerShowText = findViewById(R.id.date_picker_show_text);
        DatePicker datePicker1 = findViewById(R.id.date_picker_1);
        DatePicker datePicker2 = findViewById(R.id.date_picker_2);
        TextView timePickerShowText = findViewById(R.id.time_picker_show_text);
        TimePicker timePicker = findViewById(R.id.time_picker);
        TextView hourMinutePickerShowText = findViewById(R.id.hour_minute_picker_show_text);
        HourMinutePicker hourMinutePicker = findViewById(R.id.hour_minute_picker);
        TextView ymdhPickerShowText = findViewById(R.id.ymdh_picker_show_text);
        YMDHPicker ymdhPicker = findViewById(R.id.ymdhPicker);
        TextView dateTimePickerShowText = findViewById(R.id.date_time_picker_show_text);
        DateTimePicker dateTimePicker = findViewById(R.id.dateTimePicker);


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

        ymdhPicker.setSelectionDivider(new ColorDrawable(0xff000000));
        ymdhPicker.setSelectionDividerHeight(2);
        ymdhPicker.setOnDateChangedListener((view, year, monthOfYear, dayOfMonth, hourOfDay) -> ymdhPickerShowText.setText(new StringBuilder()
                .append("YMDHPicker:")
                .append(year).append("年")
                .append(monthOfYear + 1).append("月")
                .append(dayOfMonth).append("日")
                .append(hourOfDay).append("时")));

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
