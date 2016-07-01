package com.lany.picker.lanypicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.lany.picker.R;
import com.lany.picker.lanypicker.LanyPicker.OnTimeChangedListener;

/**
 * hour/minute/second
 */
public class LanyPickerDialog extends AlertDialog implements OnClickListener,
        OnTimeChangedListener {

    public interface OnTimeSetListener {
        void onTimeSet(LanyPicker view, int hourOfDay, int minute, int second);
    }

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";

    private final LanyPicker mLanyPicker;
    private final OnTimeSetListener mCallback;

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private int mInitialSecond;

    public LanyPickerDialog(Context context, OnTimeSetListener callBack,
                            int hourOfDay, int minute, int second) {
        this(
                context,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? R.style.Theme_Dialog_Alert
                        : 0, callBack, hourOfDay, minute, second);
    }

    public LanyPickerDialog(Context context, int theme,
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
        mLanyPicker = (LanyPicker) view.findViewById(R.id.lanyPicker);

        mLanyPicker.setCurrentHour(mInitialHourOfDay);
        mLanyPicker.setCurrentMinute(mInitialMinute);
        mLanyPicker.setCurrentSecond(mInitialSecond);
        mLanyPicker.setOnTimeChangedListener(this);
    }

    public void onClick(DialogInterface dialog, int which) {
        tryNotifyTimeSet();
    }

    public void updateTime(int hourOfDay, int minutOfHour, int secondOfMinute) {
        mLanyPicker.setCurrentHour(hourOfDay);
        mLanyPicker.setCurrentMinute(minutOfHour);
        mLanyPicker.setCurrentSecond(secondOfMinute);
    }

    private void tryNotifyTimeSet() {
        if (mCallback != null) {
            mLanyPicker.clearFocus();
            mCallback.onTimeSet(mLanyPicker, mLanyPicker.getCurrentHour(),
                    mLanyPicker.getCurrentMinute(),
                    mLanyPicker.getCurrentSecond());
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
        state.putInt(HOUR, mLanyPicker.getCurrentHour());
        state.putInt(MINUTE, mLanyPicker.getCurrentMinute());
        state.putInt(SECOND, mLanyPicker.getCurrentSecond());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        int second = savedInstanceState.getInt(SECOND);
        mLanyPicker.setCurrentHour(hour);
        mLanyPicker.setCurrentMinute(minute);
        mLanyPicker.setCurrentSecond(second);
    }

    @Override
    public void onTimeChanged(LanyPicker view, int hourOfDay, int minute,
                              int second) {

    }
}