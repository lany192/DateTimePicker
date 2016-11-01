package com.lany.picker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.lany.picker.HMSPicker.OnTimeChangedListener;

/**
 * hour/minute/second
 */
public class HMSPickerDialog extends AlertDialog implements OnClickListener,
        OnTimeChangedListener {

    public interface OnTimeSetListener {
        void onTimeSet(HMSPicker view, int hourOfDay, int minute, int second);
    }

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    private final HMSPicker mHMSPicker;
    private final OnTimeSetListener mCallback;

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private int mInitialSecond;

    public HMSPickerDialog(Context context, OnTimeSetListener callBack,
                           int hourOfDay, int minute, int second) {
        this(
                context,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? R.style.Theme_Dialog_Alert
                        : 0, callBack, hourOfDay, minute, second);
    }

    public HMSPickerDialog(Context context, int theme,
                           OnTimeSetListener callBack, int hourOfDay, int minute, int second) {
        super(context, theme);
        mCallback = callBack;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mInitialSecond = second;

        setIcon(0);
        setTitle(R.string.time_picker_dialog_title);

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE,
                themeContext.getText(R.string.date_time_done), this);

        LayoutInflater inflater = (LayoutInflater) themeContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.lany_picker_dialog, null);
        setView(view);
        mHMSPicker = (HMSPicker) view.findViewById(R.id.hMSPicker);

        mHMSPicker.setCurrentHour(mInitialHourOfDay);
        mHMSPicker.setCurrentMinute(mInitialMinute);
        mHMSPicker.setCurrentSecond(mInitialSecond);
        mHMSPicker.setOnTimeChangedListener(this);
    }

    public void onClick(DialogInterface dialog, int which) {
        tryNotifyTimeSet();
    }

    public void updateTime(int hourOfDay, int minutOfHour, int secondOfMinute) {
        mHMSPicker.setCurrentHour(hourOfDay);
        mHMSPicker.setCurrentMinute(minutOfHour);
        mHMSPicker.setCurrentSecond(secondOfMinute);
    }

    private void tryNotifyTimeSet() {
        if (mCallback != null) {
            mHMSPicker.clearFocus();
            mCallback.onTimeSet(mHMSPicker, mHMSPicker.getCurrentHour(),
                    mHMSPicker.getCurrentMinute(),
                    mHMSPicker.getCurrentSecond());
        }
    }

    @Override
    protected void onStop() {
        tryNotifyTimeSet();
        super.onStop();
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mHMSPicker.getCurrentHour());
        state.putInt(MINUTE, mHMSPicker.getCurrentMinute());
        state.putInt(SECOND, mHMSPicker.getCurrentSecond());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        int second = savedInstanceState.getInt(SECOND);
        mHMSPicker.setCurrentHour(hour);
        mHMSPicker.setCurrentMinute(minute);
        mHMSPicker.setCurrentSecond(second);
    }

    @Override
    public void onTimeChanged(HMSPicker view, int hourOfDay, int minute, int second) {

    }
}