package com.github.lany192.samples;

import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.lany192.picker.DatePicker;
import com.github.lany192.picker.DateTimePicker;
import com.github.lany192.picker.HourMinutePicker;
import com.github.lany192.picker.NumberPicker;
import com.github.lany192.picker.TimePicker;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NumberPicker numberPicker1 = findViewById(R.id.number_picker_1);

        TextView datePickerShowText = findViewById(R.id.date_picker_show_text);
        DatePicker datePicker1 = findViewById(R.id.date_picker_1);
        DatePicker datePicker2 = findViewById(R.id.date_picker_2);
        TextView timePickerShowText = findViewById(R.id.time_picker_show_text);
        TimePicker timePicker = findViewById(R.id.time_picker);
        TextView hourMinutePickerShowText = findViewById(R.id.hour_minute_picker_show_text);
        HourMinutePicker hourMinutePicker = findViewById(R.id.hour_minute_picker);
        TextView dateTimePickerShowText = findViewById(R.id.date_time_picker_show_text);
        DateTimePicker dateTimePicker = findViewById(R.id.dateTimePicker);

        numberPicker1.setMaxValue(100);
        numberPicker1.setMinValue(1);
        numberPicker1.setValue(8);
        numberPicker1.setFormatter(value -> value + "个");
        numberPicker1.setDividerThickness(1);
        numberPicker1.setDividerColor(Color.RED);
        numberPicker1.setTextColor(Color.MAGENTA);
        numberPicker1.setSelectedTextColor(Color.BLACK);

        Calendar calendar= Calendar.getInstance();
        calendar.set(2000,1,1);
        datePicker1.setMinDate(calendar.getTimeInMillis());
        calendar.set(2100,1,1);


        datePicker1.setSelectionDivider(new ColorDrawable(0xffff0000));
        datePicker1.setSelectionDividerHeight(2);
//        datePicker1.setIsAutoScrollState(false);
        //datePicker1.setDayViewShown(false);
        datePicker1.setDividerThickness(1);
        datePicker1.setDividerColor(Color.RED);
        datePicker1.setTextColor(Color.MAGENTA);
        datePicker1.setSelectedTextColor(Color.BLACK);
        datePicker1.setMaxDate(calendar.getTimeInMillis());
        datePicker1.setDayViewShown(false);
        datePicker1.setFormatter("%02d年", "%02d月", "%02d日");
        datePicker1.setSelectedTextColor(Color.BLUE);
        datePicker1.setOnChangedListener((view, year, monthOfYear, dayOfMonth) -> datePickerShowText.setText(new StringBuilder()
                .append("DatePicker:")
                .append(year).append("年")
                .append(monthOfYear + 1).append("月")
                .append(dayOfMonth).append("日")))
          
        datePicker2.setSelectionDivider(new ColorDrawable(0xff008B00));
        datePicker2.setSelectionDividerHeight(4);
//        datePicker2.setIsAutoScrollState(false);
        //datePicker2.setDayViewShown(false);
        datePicker2.setFormatter("%02d年", "%02d月", "%02d日");
        datePicker2.setSelectedTextColor(Color.GREEN);
        datePicker2.setOnChangedListener((view, year, monthOfYear, dayOfMonth) -> datePickerShowText.setText(new StringBuilder()
                .append("DatePicker:")
                .append(year).append("年")
                .append(monthOfYear + 1).append("月")
                .append(dayOfMonth).append("日")));

        timePicker.setSelectionDivider(new ColorDrawable(0xff000000));
        timePicker.setSelectionDividerHeight(2);
//        timePicker.setIsAutoScrollState(false);
        timePicker.setDividerThickness(1);
        timePicker.setDividerColor(Color.RED);
        timePicker.setTextColor(Color.MAGENTA);
        timePicker.setSelectedTextColor(Color.BLACK);
        timePicker.setFormatter("%02d时", "%02d分", "%02d秒");
        timePicker.setOnChangedListener((view, hourOfDay, minuteOfHour, scd) -> timePickerShowText.setText(new StringBuilder()
                .append("TimePicker:")
                .append(hourOfDay).append("时")
                .append(minuteOfHour).append("分")
                .append(scd).append("秒")));

        hourMinutePicker.setIs24HourView(false);
        hourMinutePicker.setSelectionDivider(new ColorDrawable(0xff436EEE));
        hourMinutePicker.setSelectionDividerHeight(4);
//        hourMinutePicker.setIsAutoScrollState(false);
        hourMinutePicker.setDividerThickness(1);
        hourMinutePicker.setDividerColor(Color.RED);
        hourMinutePicker.setTextColor(Color.MAGENTA);
        hourMinutePicker.setSelectedTextColor(Color.BLACK);
        hourMinutePicker.setIs24HourView(true);
        hourMinutePicker.setFormatter("%02d时", "%02d分");
        hourMinutePicker.setOnChangedListener((view, hourOfDay, minute) -> hourMinutePickerShowText.setText(new StringBuilder()
                .append("HourMinutePicker:")
                .append(hourOfDay).append("时")
                .append(minute).append("分")));

        dateTimePicker.setSelectionDivider(new ColorDrawable(0xff000000));
        dateTimePicker.setSelectionDividerHeight(2);
//        dateTimePicker.setIsAutoScrollState(false);
        dateTimePicker.setDividerThickness(1);
        dateTimePicker.setDividerColor(Color.RED);
        dateTimePicker.setTextColor(Color.MAGENTA);
        dateTimePicker.setSelectedTextColor(Color.BLACK);
        dateTimePicker.setFormatter("%02d年", "%02d月", "%02d日", "%02d时", "%02d分", "%02d秒");
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
