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
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lany.numberpicker.NumberPicker;

import java.util.Calendar;
import java.util.Locale;

/**
 * Hour/Minute/Second
 */
public class TimePicker extends FrameLayout {
    private static final boolean DEFAULT_ENABLED_STATE = true;

    private final NumberPicker mHourNPicker;

    private final NumberPicker mMinuteNPicker;

    private final NumberPicker mSecondNPicker;

    private final EditText mHourEditText;

    private final EditText mMinuteEditText;

    private final EditText mSecondEditText;

    private final TextView mFirstDivider;
    private final TextView mSecondDivider;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    private OnTimeChangedListener mOnTimeChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCurrentLocale(Locale.getDefault());

        LayoutInflater.from(getContext()).inflate(R.layout.picker_time, this);

        // hour
        mHourNPicker = findViewById(R.id.picker_time_hour);
        mHourNPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker NPicker, int oldVal, int newVal) {
                updateInputState();
                onTimeChanged();
            }
        });
        mHourEditText = mHourNPicker.findViewById(R.id.number_picker_edit_text);
        mHourEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        //divider
        mFirstDivider = findViewById(R.id.picker_time_first_divider);
        if (mFirstDivider != null) {
            mFirstDivider.setText(R.string.time_picker_separator);
        }
        mSecondDivider = findViewById(R.id.picker_time_second_divider);
        if (mSecondDivider != null) {
            mSecondDivider.setText(R.string.time_picker_separator);
        }
        // minute
        mMinuteNPicker = findViewById(R.id.picker_time_minute);
        mMinuteNPicker.setMinValue(0);
        mMinuteNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteNPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker NPicker, int oldVal,
                                      int newVal) {
                updateInputState();
                int minValue = mMinuteNPicker.getMinValue();
                int maxValue = mMinuteNPicker.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourNPicker.getValue() + 1;
                    mHourNPicker.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourNPicker.getValue() - 1;
                    mHourNPicker.setValue(newHour);
                }
                onTimeChanged();
            }
        });
        mMinuteEditText = mMinuteNPicker.findViewById(R.id.number_picker_edit_text);
        mMinuteEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        mSecondNPicker = findViewById(R.id.picker_time_second);
        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondNPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateInputState();
                picker.requestFocus();
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
                onTimeChanged();
            }
        });
        mSecondEditText = mSecondNPicker.findViewById(R.id.number_picker_edit_text);
        mSecondEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // update controls to initial state
        updateHourControl();
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);

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
        mHourNPicker.setSelectionDivider(selectionDivider);
        mMinuteNPicker.setSelectionDivider(selectionDivider);
        mSecondNPicker.setSelectionDivider(selectionDivider);
    }

    public void setSelectionDividerHeight(int selectionDividerHeight) {
        mHourNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mMinuteNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mSecondNPicker.setSelectionDividerHeight(selectionDividerHeight);
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        mMinuteNPicker.setEnabled(enabled);
        mFirstDivider.setEnabled(enabled);
        mHourNPicker.setEnabled(enabled);
        mSecondDivider.setEnabled(enabled);
        mSecondNPicker.setEnabled(enabled);
        mIsEnabled = enabled;
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

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }

    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        mOnTimeChangedListener = listener;
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        return mHourNPicker.getValue();
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        // why was Integer used in the first place?
        if (currentHour == null || currentHour == getCurrentHour()) {
            return;
        }
        mHourNPicker.setValue(currentHour);
        onTimeChanged();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mMinuteNPicker.getValue();
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute == getCurrentMinute()) {
            return;
        }
        mMinuteNPicker.setValue(currentMinute);
        onTimeChanged();
    }

    /**
     * @return The current second.
     */
    public Integer getCurrentSecond() {
        return mSecondNPicker.getValue();
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        if (currentSecond == getCurrentSecond()) {
            return;
        }
        mSecondNPicker.setValue(currentSecond);
        onTimeChanged();
    }

    @Override
    public int getBaseline() {
        return mHourNPicker.getBaseline();
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
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(), mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TimePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TimePicker.class.getName());
    }

    private void updateHourControl() {
        mHourNPicker.setMinValue(0);
        mHourNPicker.setMaxValue(23);
        mHourNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
        }
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourEditText)) {
                mHourEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteEditText)) {
                mMinuteEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mSecondEditText)) {
                mSecondEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker view, int hourOfDay, int minute,
                           int second);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;

        private final int mMinute;

        private final int mSecond;

        private SavedState(Parcelable superState, int hour, int minute, int second) {
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
}
