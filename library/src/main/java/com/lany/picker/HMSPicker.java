package com.lany.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class HMSPicker extends FrameLayout {

    private static final boolean DEFAULT_ENABLED_STATE = true;

    private final NumberPicker mHourSpinner;

    private final NumberPicker mMinuteSpinner;

    private final NumberPicker mSecondSpinner;

    private final EditText mHourSpinnerInput;

    private final EditText mMinuteSpinnerInput;

    private final EditText mSecondSpinnerInput;

    private final TextView mFirstDivider;
    private final TextView mSecondDivider;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    private OnTimeChangedListener mOnTimeChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    public interface OnTimeChangedListener {
        void onTimeChanged(HMSPicker view, int hourOfDay, int minute,
                           int second);
    }

    public HMSPicker(Context context) {
        this(context, null);
    }

    public HMSPicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
    }

    public HMSPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCurrentLocale(Locale.getDefault());
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.lany_picker_holo, this, true);

        // hour
        mHourSpinner = (NumberPicker) findViewById(R.id.hour);
        mHourSpinner
                .setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    public void onValueChange(NumberPicker spinner, int oldVal,
                                              int newVal) {
                        updateInputState();
                        onTimeChanged();
                    }
                });
        mHourSpinnerInput = (EditText) mHourSpinner
                .findViewById(R.id.np__numberpicker_input);
        mHourSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        //divider
        mFirstDivider = (TextView) findViewById(R.id.first_divider);
        if (mFirstDivider != null) {
            mFirstDivider.setText(R.string.time_picker_separator);
        }
        mSecondDivider = (TextView) findViewById(R.id.second_divider);
        if (mSecondDivider != null) {
            mSecondDivider.setText(R.string.time_picker_separator);
        }
        // minute
        mMinuteSpinner = (NumberPicker) findViewById(R.id.minute);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteSpinner
                .setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    public void onValueChange(NumberPicker spinner, int oldVal,
                                              int newVal) {
                        updateInputState();
                        int minValue = mMinuteSpinner.getMinValue();
                        int maxValue = mMinuteSpinner.getMaxValue();
                        if (oldVal == maxValue && newVal == minValue) {
                            int newHour = mHourSpinner.getValue() + 1;
                            mHourSpinner.setValue(newHour);
                        } else if (oldVal == minValue && newVal == maxValue) {
                            int newHour = mHourSpinner.getValue() - 1;
                            mHourSpinner.setValue(newHour);
                        }
                        onTimeChanged();
                    }
                });
        mMinuteSpinnerInput = (EditText) mMinuteSpinner
                .findViewById(R.id.np__numberpicker_input);
        mMinuteSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        View secondView = findViewById(R.id.second);
        mSecondSpinner = (NumberPicker) secondView;
        mSecondSpinner.setMinValue(0);
        mSecondSpinner.setMaxValue(59);
        mMinuteSpinner.setOnLongPressUpdateInterval(100);
        mMinuteSpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondSpinner
                .setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    public void onValueChange(NumberPicker picker, int oldVal,
                                              int newVal) {
                        updateInputState();
                        picker.requestFocus();
                        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
                        onTimeChanged();
                    }
                });
        mSecondSpinnerInput = (EditText) mSecondSpinner
                .findViewById(R.id.np__numberpicker_input);
        mSecondSpinnerInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // update controls to initial state
        updateHourControl();
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);

        setOnTimeChangedListener(new OnTimeChangedListener(){

            @Override
            public void onTimeChanged(HMSPicker view, int hourOfDay, int minute, int second) {

            }
        });

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));

        if (!isEnabled()) {
            setEnabled(false);
        }

        // If not explicitly specified this view is important for accessibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    public void setSelectionDivider(Drawable selectionDivider) {
        mHourSpinner.setSelectionDivider(selectionDivider);
        mMinuteSpinner.setSelectionDivider(selectionDivider);
        mSecondSpinner.setSelectionDivider(selectionDivider);
    }

    public void setSelectionDividerHeight(int selectionDividerHeight) {
        mHourSpinner.setSelectionDividerHeight(selectionDividerHeight);
        mMinuteSpinner.setSelectionDividerHeight(selectionDividerHeight);
        mSecondSpinner.setSelectionDividerHeight(selectionDividerHeight);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mMinuteSpinner.setEnabled(enabled);
        mFirstDivider.setEnabled(enabled);
        mHourSpinner.setEnabled(enabled);
        mSecondDivider.setEnabled(enabled);
        mSecondSpinner.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempCalendar = Calendar.getInstance(locale);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;

        private final int mMinute;

        private final int mSecond;

        private SavedState(Parcelable superState, int hour, int minute,
                           int second) {
            super(superState);
            mHour = hour;
            mMinute = minute;
            mSecond = second;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        public int getSecond() {
            return mSecond;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour(), getCurrentMinute(),
                getCurrentSecond());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        return mHourSpinner.getValue();
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        // why was Integer used in the first place?
        if (currentHour == null || currentHour == getCurrentHour()) {
            return;
        }
        mHourSpinner.setValue(currentHour);
        onTimeChanged();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mMinuteSpinner.getValue();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        mMinuteSpinner.setValue(currentMinute);
        onTimeChanged();
    }

    /**
     * @return The current second.
     */
    public Integer getCurrentSecond() {
        return mSecondSpinner.getValue();
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        if (currentSecond == getCurrentSecond()) {
            return;
        }
        mSecondSpinner.setValue(currentSecond);
        onTimeChanged();
    }

    @Override
    public int getBaseline() {
        return mHourSpinner.getBaseline();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        int flags = DateUtils.FORMAT_SHOW_TIME;
        flags |= DateUtils.FORMAT_24HOUR;
        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        mTempCalendar.set(Calendar.SECOND, getCurrentSecond());
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(),
                mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(HMSPicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(HMSPicker.class.getName());
    }

    private void updateHourControl() {
        mHourSpinner.setMinValue(0);
        mHourSpinner.setMaxValue(23);
        mHourSpinner.setFormatter(NumberPicker.getTwoDigitFormatter());
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(),
                    getCurrentMinute(), getCurrentSecond());
        }
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourSpinnerInput)) {
                mHourSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteSpinnerInput)) {
                mMinuteSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mSecondSpinnerInput)) {
                mSecondSpinnerInput.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }
}
