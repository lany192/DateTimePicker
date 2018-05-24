package com.lany.picker.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.lany.box.activity.BaseActivity;
import com.lany.picker.DatePicker;
import com.lany.picker.DateTimePicker;
import com.lany.picker.HourMinutePicker;
import com.lany.picker.TimePicker;
import com.lany.picker.YMDHPicker;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    @BindView(R.id.date_picker_show_text)
    TextView datePickerShowText;
    @BindView(R.id.date_picker_1)
    DatePicker datePicker1;
    @BindView(R.id.date_picker_2)
    DatePicker datePicker2;
    @BindView(R.id.time_picker_show_text)
    TextView timePickerShowText;
    @BindView(R.id.time_picker)
    TimePicker timePicker;
    @BindView(R.id.hour_minute_picker_show_text)
    TextView hourMinutePickerShowText;
    @BindView(R.id.hour_minute_picker)
    HourMinutePicker hourMinutePicker;
    @BindView(R.id.ymdh_picker_show_text)
    TextView ymdhPickerShowText;
    @BindView(R.id.ymdhPicker)
    YMDHPicker ymdhPicker;
    @BindView(R.id.date_time_picker_show_text)
    TextView dateTimePickerShowText;
    @BindView(R.id.dateTimePicker)
    DateTimePicker dateTimePicker;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle bundle) {
        datePicker1.setSelectionDivider(new ColorDrawable(0xffff0000));
        datePicker1.setSelectionDividerHeight(2);
        datePicker1.setCalendarViewShown(false);
        //datePicker1.setDayViewShown(false);
        datePicker1.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                datePickerShowText.setText(new StringBuilder()
                        .append("DatePicker  ")
                        .append(year).append("年")
                        .append(monthOfYear + 1).append("月")
                        .append(dayOfMonth).append("日"));
            }
        });

        datePicker2.setSelectionDivider(new ColorDrawable(0xff008B00));
        datePicker2.setSelectionDividerHeight(4);
        datePicker2.setCalendarViewShown(false);
        //datePicker2.setDayViewShown(false);
        datePicker2.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                datePickerShowText.setText(new StringBuilder()
                        .append("DatePicker  ")
                        .append(year).append("年")
                        .append(monthOfYear + 1).append("月")
                        .append(dayOfMonth).append("日"));
            }
        });

        timePicker.setSelectionDivider(new ColorDrawable(0xff000000));
        timePicker.setSelectionDividerHeight(2);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minuteOfHour, int scd) {
                timePickerShowText.setText(new StringBuilder()
                        .append("TimePicker  ")
                        .append(hourOfDay).append("时")
                        .append(minuteOfHour).append("分")
                        .append(scd).append("秒"));
            }
        });

        hourMinutePicker.setIs24HourView(false);
        hourMinutePicker.setSelectionDivider(new ColorDrawable(0xff436EEE));
        hourMinutePicker.setSelectionDividerHeight(4);
        hourMinutePicker.setOnTimeChangedListener(new HourMinutePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(HourMinutePicker view, int hourOfDay, int minute) {
                hourMinutePickerShowText.setText(new StringBuilder()
                        .append("HourMinutePicker  ")
                        .append(hourOfDay).append("时")
                        .append(minute).append("分"));
            }
        });

        ymdhPicker.setSelectionDivider(new ColorDrawable(0xff000000));
        ymdhPicker.setSelectionDividerHeight(2);
        ymdhPicker.setOnDateChangedListener(new YMDHPicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(YMDHPicker view, int year, int monthOfYear, int dayOfMonth, int hourOfDay) {
                ymdhPickerShowText.setText(new StringBuilder()
                        .append("YMDHPicker  ")
                        .append(year).append("年")
                        .append(monthOfYear + 1).append("月")
                        .append(dayOfMonth).append("日")
                        .append(hourOfDay).append("时"));
            }
        });

        dateTimePicker.setSelectionDivider(new ColorDrawable(0xff000000));
        dateTimePicker.setSelectionDividerHeight(2);
        dateTimePicker.setOnChangedListener(new DateTimePicker.OnChangedListener() {
            @Override
            public void onChanged(DateTimePicker view, int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute, int second) {
                dateTimePickerShowText.setText(new StringBuilder()
                        .append("DateTimePicker  ")
                        .append(year).append("年")
                        .append(monthOfYear + 1).append("月")
                        .append(dayOfMonth).append("日")
                        .append(hourOfDay).append("时")
                        .append(minute).append("分")
                        .append(second).append("秒"));
            }
        });
    }
}
