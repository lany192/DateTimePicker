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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lany.numberpicker.NumberPicker;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * 小时和分钟
 */
public class HourMinutePicker extends FrameLayout {
    private static final boolean DEFAULT_ENABLED_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;
    // ui components
    private final NumberPicker mHourNPicker;
    private final NumberPicker mMinuteNPicker;
    private final NumberPicker mAmPmNPicker;
    private final EditText mHourEditText;
    private final EditText mMinuteEditText;
    private final EditText mAmPmEditText;
    private final TextView mDivider;
    // Note that the legacy implementation of the TimePicker is
    // using a button for toggling between AM/PM while the new
    // version uses a NumberPicker NPicker. Therefore the code
    // accommodates these two cases to be backwards compatible.
    private final Button mAmPmButton;
    private final String[] mAmPmStrings;
    // state
    private boolean mIs24HourView = true;
    private boolean mIsAm;
    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    public HourMinutePicker(Context context) {
        this(context, null);
    }

    public HourMinutePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
    }

    public HourMinutePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        LayoutInflater.from(getContext()).inflate(R.layout.picker_hour_minute, this);

        // hour
        mHourNPicker = findViewById(R.id.hour);
        mHourNPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker NPicker, int oldVal, int newVal) {
                updateInputState();
                if (!is24HourView()) {
                    if ((oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY)
                            || (oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1)) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                }
                onTimeChanged();
            }
        });
        mHourEditText = mHourNPicker.findViewById(R.id.number_picker_edit_text);
        mHourEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // divider (only for the new widget style)
        mDivider = findViewById(R.id.divider);
        if (mDivider != null) {
            mDivider.setText(R.string.time_picker_separator);
        }

        // minute
        mMinuteNPicker = findViewById(R.id.minute);
        mMinuteNPicker.setMinValue(0);
        mMinuteNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteNPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker NPicker, int oldVal, int newVal) {
                updateInputState();
                int minValue = mMinuteNPicker.getMinValue();
                int maxValue = mMinuteNPicker.getMaxValue();
                if (oldVal == maxValue && newVal == minValue) {
                    int newHour = mHourNPicker.getValue() + 1;
                    if (!is24HourView() && newHour == HOURS_IN_HALF_DAY) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourNPicker.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    int newHour = mHourNPicker.getValue() - 1;
                    if (!is24HourView()
                            && newHour == HOURS_IN_HALF_DAY - 1) {
                        mIsAm = !mIsAm;
                        updateAmPmControl();
                    }
                    mHourNPicker.setValue(newHour);
                }
                onTimeChanged();
            }
        });
        mMinuteEditText = mMinuteNPicker.findViewById(R.id.number_picker_edit_text);
        mMinuteEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        /* Get the localized am/pm strings and use them in the NPicker */
        mAmPmStrings = new DateFormatSymbols().getAmPmStrings();

        // am/pm
        View amPmView = findViewById(R.id.amPm);
        if (amPmView instanceof Button) {
            mAmPmNPicker = null;
            mAmPmEditText = null;
            mAmPmButton = (Button) amPmView;
            mAmPmButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View button) {
                    button.requestFocus();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged();
                }
            });
        } else {
            mAmPmButton = null;
            mAmPmNPicker = (NumberPicker) amPmView;
            mAmPmNPicker.setMinValue(0);
            mAmPmNPicker.setMaxValue(1);
            mAmPmNPicker.setDisplayedValues(mAmPmStrings);
            mAmPmNPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                public void onValueChange(NumberPicker picker,
                                          int oldVal, int newVal) {
                    updateInputState();
                    picker.requestFocus();
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                    onTimeChanged();
                }
            });
            mAmPmEditText = mAmPmNPicker.findViewById(R.id.number_picker_edit_text);
            mAmPmEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        // update controls to initial state
        updateHourControl();
        updateAmPmControl();

        setOnTimeChangedListener(new OnTimeChangedListener() {

            @Override
            public void onTimeChanged(HourMinutePicker view, int hourOfDay, int minute) {

            }
        });

        // set to current time
        setCurrentHour(mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(mTempCalendar.get(Calendar.MINUTE));

        if (!isEnabled()) {
            setEnabled(false);
        }

        // set the content descriptions
        setContentDescriptions();

        // If not explicitly specified this view is important for accessibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    public void setSelectionDivider(Drawable selectionDivider) {
        mHourNPicker.setSelectionDivider(selectionDivider);
        mMinuteNPicker.setSelectionDivider(selectionDivider);
        mAmPmNPicker.setSelectionDivider(selectionDivider);
    }

    public void setSelectionDividerHeight(int selectionDividerHeight) {
        mHourNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mMinuteNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mAmPmNPicker.setSelectionDividerHeight(selectionDividerHeight);
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
        if (mDivider != null) {
            mDivider.setEnabled(enabled);
        }
        mHourNPicker.setEnabled(enabled);
        if (mAmPmNPicker != null) {
            mAmPmNPicker.setEnabled(enabled);
        } else {
            mAmPmButton.setEnabled(enabled);
        }
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
        return new SavedState(superState, getCurrentHour(), getCurrentMinute());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour in the range (0-23).
     */
    public Integer getCurrentHour() {
        int currentHour = mHourNPicker.getValue();
        if (is24HourView()) {
            return currentHour;
        } else if (mIsAm) {
            return currentHour % HOURS_IN_HALF_DAY;
        } else {
            return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
        }
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        // why was Integer used in the first place?
        if (currentHour == null || currentHour == getCurrentHour()) {
            return;
        }
        if (!is24HourView()) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour >= HOURS_IN_HALF_DAY) {
                mIsAm = false;
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour = currentHour - HOURS_IN_HALF_DAY;
                }
            } else {
                mIsAm = true;
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY;
                }
            }
            updateAmPmControl();
        }
        mHourNPicker.setValue(currentHour);
        onTimeChanged();
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView == is24HourView) {
            return;
        }
        mIs24HourView = is24HourView;
        // cache the current hour since NPicker range changes
        int currentHour = getCurrentHour();
        updateHourControl();
        // set value after NPicker range is updated
        setCurrentHour(currentHour);
        updateAmPmControl();
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
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
        if (mIs24HourView) {
            flags |= DateUtils.FORMAT_24HOUR;
        } else {
            flags |= DateUtils.FORMAT_12HOUR;
        }
        mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(),
                mTempCalendar.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(HourMinutePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(HourMinutePicker.class.getName());
    }

    private void updateHourControl() {
        if (is24HourView()) {
            mHourNPicker.setMinValue(0);
            mHourNPicker.setMaxValue(23);
            mHourNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        } else {
            mHourNPicker.setMinValue(1);
            mHourNPicker.setMaxValue(12);
            mHourNPicker.setFormatter(null);
        }
    }

    private void updateAmPmControl() {
        if (is24HourView()) {
            if (mAmPmNPicker != null) {
                mAmPmNPicker.setVisibility(View.GONE);
            } else {
                mAmPmButton.setVisibility(View.GONE);
            }
        } else {
            int index = mIsAm ? Calendar.AM : Calendar.PM;
            if (mAmPmNPicker != null) {
                mAmPmNPicker.setValue(index);
                mAmPmNPicker.setVisibility(View.VISIBLE);
            } else {
                mAmPmButton.setText(mAmPmStrings[index]);
                mAmPmButton.setVisibility(View.VISIBLE);
            }
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnTimeChangedListener != null) {
            mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(),
                    getCurrentMinute());
        }
    }

    private void setContentDescriptions() {
        if (true)
            return; // This is never reached anyway, backport doesn't have
        // increment/decrement buttons
        // Minute
        trySetContentDescription(mMinuteNPicker, R.id.np__increment,
                R.string.time_picker_increment_minute_button);
        trySetContentDescription(mMinuteNPicker, R.id.np__decrement,
                R.string.time_picker_decrement_minute_button);
        // Hour
        trySetContentDescription(mHourNPicker, R.id.np__increment,
                R.string.time_picker_increment_hour_button);
        trySetContentDescription(mHourNPicker, R.id.np__decrement,
                R.string.time_picker_decrement_hour_button);
        // AM/PM
        if (mAmPmNPicker != null) {
            trySetContentDescription(mAmPmNPicker, R.id.np__increment,
                    R.string.time_picker_increment_set_pm_button);
            trySetContentDescription(mAmPmNPicker, R.id.np__decrement,
                    R.string.time_picker_decrement_set_am_button);
        }
    }

    private void trySetContentDescription(View root, int viewId,
                                          int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(getContext().getString(contDescResId));
        }
    }

    private void updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        // InputMethodManager inputMethodManager =
        // InputMethodManager.peekInstance();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourEditText)) {
                mHourEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteEditText)) {
                mMinuteEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mAmPmEditText)) {
                mAmPmEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view      The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute    The current minute.
         */
        void onTimeChanged(HourMinutePicker view, int hourOfDay, int minute);
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings({"unused", "hiding"})
        public static final Creator<SavedState> CREATOR = new Creator<HourMinutePicker.SavedState>() {
            public HourMinutePicker.SavedState createFromParcel(Parcel in) {
                return new HourMinutePicker.SavedState(in);
            }

            public HourMinutePicker.SavedState[] newArray(int size) {
                return new HourMinutePicker.SavedState[size];
            }
        };
        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }
    }
}
