/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lany.picker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.lany.picker.HMPicker.OnTimeChangedListener;

/**
 * A dialog that prompts the user for the time of day using a {@link HMPicker}.
 * <p/>
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/pickers.html">Pickers</a>
 * guide.</p>
 */
public class HMPickerDialog extends AlertDialog
        implements OnClickListener, OnTimeChangedListener {

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /**
         * @param view      The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute    The minute that was set.
         */
        void onTimeSet(HMPicker view, int hourOfDay, int minute);
    }

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    private final HMPicker mHMPicker;
    private final OnTimeSetListener mCallback;

    int mInitialHourOfDay;
    int mInitialMinute;
    boolean mIs24HourView;

    /**
     * @param context      Parent.
     * @param callBack     How parent is notified.
     * @param hourOfDay    The initial hour.
     * @param minute       The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public HMPickerDialog(Context context,
                          OnTimeSetListener callBack,
                          int hourOfDay, int minute, boolean is24HourView) {
        this(context, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? R.style.Theme_Dialog_Alert : 0, callBack, hourOfDay, minute, is24HourView);
    }

    /**
     * @param context      Parent.
     * @param theme        the theme to apply to this dialog
     * @param callBack     How parent is notified.
     * @param hourOfDay    The initial hour.
     * @param minute       The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public HMPickerDialog(Context context,
                          int theme,
                          OnTimeSetListener callBack,
                          int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        mCallback = callBack;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourView = is24HourView;

        setIcon(0);
        setTitle(R.string.time_picker_dialog_title);

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE, themeContext.getText(R.string.date_time_done), this);

        LayoutInflater inflater =
                (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.time_picker_dialog, null);
        setView(view);
        mHMPicker = (HMPicker) view.findViewById(R.id.hmPicker);

        // initialize state
        mHMPicker.setIs24HourView(mIs24HourView);
        mHMPicker.setCurrentHour(mInitialHourOfDay);
        mHMPicker.setCurrentMinute(mInitialMinute);
        mHMPicker.setOnTimeChangedListener(this);
    }

    public void onClick(DialogInterface dialog, int which) {
        tryNotifyTimeSet();
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        mHMPicker.setCurrentHour(hourOfDay);
        mHMPicker.setCurrentMinute(minutOfHour);
    }

    public void onTimeChanged(HMPicker view, int hourOfDay, int minute) {
        /* do nothing */
    }

    private void tryNotifyTimeSet() {
        if (mCallback != null) {
            mHMPicker.clearFocus();
            mCallback.onTimeSet(mHMPicker, mHMPicker.getCurrentHour(),
                    mHMPicker.getCurrentMinute());
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
        state.putInt(HOUR, mHMPicker.getCurrentHour());
        state.putInt(MINUTE, mHMPicker.getCurrentMinute());
        state.putBoolean(IS_24_HOUR, mHMPicker.is24HourView());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        mHMPicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
        mHMPicker.setCurrentHour(hour);
        mHMPicker.setCurrentMinute(minute);
    }
}
