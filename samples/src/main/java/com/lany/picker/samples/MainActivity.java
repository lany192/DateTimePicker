package com.lany.picker.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import com.lany.box.activity.BaseActivity;
import com.lany.picker.DatePicker;
import com.lany.picker.HourMinutePicker;
import com.lany.picker.HourMinuteSecondPicker;
import com.lany.picker.YMDHPicker;

public class MainActivity extends BaseActivity {
    private DatePicker mDatePicker1;
    private DatePicker mDatePicker2;
    private TextView showText;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle bundle) {
        mDatePicker1 = findViewById(R.id.date_picker_1);
        mDatePicker1.setSelectionDivider(new ColorDrawable(0xffff0000));
        mDatePicker1.setSelectionDividerHeight(2);
        mDatePicker1.setCalendarViewShown(false);
        //mDatePicker1.setDayViewShown(false);

        mDatePicker2 = findViewById(R.id.date_picker_2);
        mDatePicker2.setSelectionDivider(new ColorDrawable(0xff008B00));
        mDatePicker2.setSelectionDividerHeight(4);
        mDatePicker2.setCalendarViewShown(false);
        //mDatePicker2.setDayViewShown(false);

        HourMinuteSecondPicker lanyPicker = findViewById(R.id.lanyPicker);
        lanyPicker.setSelectionDivider(new ColorDrawable(0xff000000));
        lanyPicker.setSelectionDividerHeight(2);
        lanyPicker.setOnTimeChangedListener(new HourMinuteSecondPicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(HourMinuteSecondPicker view, int hourOfDay, int minuteOfHour, int scd) {
                showText.setText(new StringBuilder()
                        .append(hourOfDay).append(":")
                        .append(minuteOfHour).append(":")
                        .append(scd));
            }
        });

        showText = findViewById(R.id.lany_picker_show_text);

        HourMinutePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);
        timePicker.setSelectionDivider(new ColorDrawable(0xff436EEE));
        timePicker.setSelectionDividerHeight(4);


        YMDHPicker ymdhPicker = findViewById(R.id.ymdhPicker);
        ymdhPicker.setSelectionDivider(new ColorDrawable(0xff000000));
        ymdhPicker.setSelectionDividerHeight(2);
        ymdhPicker.setOnDateChangedListener(new YMDHPicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(YMDHPicker view, int year, int monthOfYear, int dayOfMonth, int hourOfDay) {
                int mYear = year;
                int mMonth = monthOfYear + 1;
                int mDay = dayOfMonth;
                int mHour = hourOfDay;

                showText.setText(new StringBuilder()
                        .append(mYear).append("年")
                        .append(mMonth).append("月")
                        .append(mDay).append("日")
                        .append(mHour).append("时"));
            }
        });
    }
}
