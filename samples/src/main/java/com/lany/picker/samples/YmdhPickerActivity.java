package com.lany.picker.samples;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.lany.picker.ymdhpicker.YmdhPicker;

public class YmdhPickerActivity extends AppCompatActivity {
    private TextView showText;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ymdh_picker);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showText = (TextView) findViewById(R.id.lany_picker_show_text);

        YmdhPicker ymdhPicker = (YmdhPicker) findViewById(R.id.ymdhPicker);
        ymdhPicker.setSelectionDivider(new ColorDrawable(0xff000000));
        ymdhPicker.setSelectionDividerHeight(2);
        ymdhPicker.setOnDateChangedListener(new YmdhPicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(YmdhPicker view, int year, int monthOfYear, int dayOfMonth, int hourOfDay) {
                mYear = year;
                mMonth = monthOfYear + 1;
                mDay = dayOfMonth;
                mHour = hourOfDay;
                updateDisplay();
            }
        });
    }

    private void updateDisplay() {
        showText.setText(new StringBuilder()
                .append(mYear).append("年")
                .append(mMonth).append("月")
                .append(mDay).append("日")
                .append(mHour).append("时"));
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
