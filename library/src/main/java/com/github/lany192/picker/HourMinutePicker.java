package com.github.lany192.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
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
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.github.lany192.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * 小时和分钟
 */
public class HourMinutePicker extends BasePicker {
    private static final boolean DEFAULT_ENABLED_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;
    // ui components
    private final NumberPicker mHourNPicker;
    private final NumberPicker mMinuteNPicker;
    private final NumberPicker mAmPmNPicker;

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
        this(context, attrs, 0);
    }

    public HourMinutePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        LayoutInflater.from(getContext()).inflate(R.layout.hour_minute_picker, this);

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
        mHourNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);

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
        mMinuteNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        /* Get the localized am/pm strings and use them in the NPicker */
        mAmPmStrings = new DateFormatSymbols().getAmPmStrings();

        // am/pm
        View amPmView = findViewById(R.id.amPm);
        if (amPmView instanceof Button) {
            mAmPmNPicker = null;
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
            mAmPmNPicker.setImeOptions(EditorInfo.IME_ACTION_DONE);
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
            mHourNPicker.setFormatter("");
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
            if (inputMethodManager.isActive(mHourNPicker)) {
                mHourNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteNPicker)) {
                mMinuteNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mAmPmNPicker)) {
                mAmPmNPicker.clearFocus();
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

    public void setAccessibilityDescriptionEnabled(boolean enabled) {
        super.setAccessibilityDescriptionEnabled(enabled, mHourNPicker, mMinuteNPicker);
    }

    public void setDividerColor(@ColorInt int color) {
        super.setDividerColor(color, mHourNPicker, mMinuteNPicker);
    }

    public void setDividerColorResource(@ColorRes int colorId) {
        super.setDividerColor(ContextCompat.getColor(getContext(), colorId), mHourNPicker, mMinuteNPicker);
    }

    public void setDividerDistance(int distance) {
        super.setDividerDistance(distance, mHourNPicker, mMinuteNPicker);
    }

    public void setDividerDistanceResource(@DimenRes int dimenId) {
        super.setDividerDistanceResource(dimenId, mHourNPicker, mMinuteNPicker);
    }

    public void setDividerType(@NumberPicker.DividerType int dividerType) {
        super.setDividerType(dividerType, mHourNPicker, mMinuteNPicker);
    }

    public void setDividerThickness(int thickness) {
        super.setDividerThickness(thickness, mHourNPicker, mMinuteNPicker);
    }

    public void setDividerThicknessResource(@DimenRes int dimenId) {
        super.setDividerThicknessResource(dimenId, mHourNPicker, mMinuteNPicker);
    }

    public void setOrder(@NumberPicker.Order int order) {
        super.setOrder(order, mHourNPicker, mMinuteNPicker);
    }

    public void setOrientation(@NumberPicker.Orientation int orientation) {
        super.setOrientation(orientation, mHourNPicker, mMinuteNPicker);
    }

    public void setWheelItemCount(int count) {
        super.setWheelItemCount(count, mHourNPicker, mMinuteNPicker);
    }

    public void setFormatter(String hourFormatter, String minuteFormatter) {
        mHourNPicker.setFormatter(hourFormatter);
        mMinuteNPicker.setFormatter(minuteFormatter);
    }

    public void setFormatter(@StringRes int hourFormatterId, @StringRes int minuteFormatterId) {
        mHourNPicker.setFormatter(getResources().getString(hourFormatterId));
        mMinuteNPicker.setFormatter(getResources().getString(minuteFormatterId));
    }

    public void setFadingEdgeEnabled(boolean fadingEdgeEnabled) {
        super.setFadingEdgeEnabled(fadingEdgeEnabled, mHourNPicker, mMinuteNPicker);
    }

    public void setFadingEdgeStrength(float strength) {
        super.setFadingEdgeStrength(strength, mHourNPicker, mMinuteNPicker);
    }

    public void setScrollerEnabled(boolean scrollerEnabled) {
        super.setScrollerEnabled(scrollerEnabled, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextAlign(@NumberPicker.Align int align) {
        super.setSelectedTextAlign(align, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextColor(@ColorInt int color) {
        super.setSelectedTextColor(color, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextColorResource(@ColorRes int colorId) {
        super.setSelectedTextColorResource(colorId, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextSize(float textSize) {
        super.setSelectedTextSize(textSize, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextSize(@DimenRes int dimenId) {
        super.setSelectedTextSize(getResources().getDimension(dimenId), mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextStrikeThru(boolean strikeThruText) {
        super.setSelectedTextStrikeThru(strikeThruText, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTextUnderline(boolean underlineText) {
        super.setSelectedTextUnderline(underlineText, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTypeface(Typeface typeface) {
        super.setSelectedTypeface(typeface, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTypeface(String string, int style) {
        super.setSelectedTypeface(string, style, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTypeface(String string) {
        super.setSelectedTypeface(string, Typeface.NORMAL, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTypeface(@StringRes int stringId, int style) {
        super.setSelectedTypeface(getResources().getString(stringId), style, mHourNPicker, mMinuteNPicker);
    }

    public void setSelectedTypeface(@StringRes int stringId) {
        super.setSelectedTypeface(stringId, Typeface.NORMAL, mHourNPicker, mMinuteNPicker);
    }

    public void setTextAlign(@NumberPicker.Align int align) {
        super.setTextAlign(align, mHourNPicker, mMinuteNPicker);
    }

    public void setTextColor(@ColorInt int color) {
        super.setTextColor(color, mHourNPicker, mMinuteNPicker);
    }

    public void setTextColorResource(@ColorRes int colorId) {
        super.setTextColorResource(colorId, mHourNPicker, mMinuteNPicker);
    }

    public void setTextSize(float textSize) {
        super.setTextSize(textSize, mHourNPicker, mMinuteNPicker);
    }

    public void setTextSize(@DimenRes int dimenId) {
        super.setTextSize(dimenId, mHourNPicker, mMinuteNPicker);
    }

    public void setTextStrikeThru(boolean strikeThruText) {
        super.setTextStrikeThru(strikeThruText, mHourNPicker, mMinuteNPicker);
    }

    public void setTextUnderline(boolean underlineText) {
        super.setTextUnderline(underlineText, mHourNPicker, mMinuteNPicker);
    }

    public void setTypeface(Typeface typeface) {
        super.setTypeface(typeface, mHourNPicker, mMinuteNPicker);
    }

    public void setTypeface(String string, int style) {
        super.setTypeface(string, style, mHourNPicker, mMinuteNPicker);
    }

    public void setTypeface(String string) {
        super.setTypeface(string, mHourNPicker, mMinuteNPicker);
    }

    public void setTypeface(@StringRes int stringId, int style) {
        super.setTypeface(stringId, style, mHourNPicker, mMinuteNPicker);
    }

    public void setTypeface(@StringRes int stringId) {
        super.setTypeface(stringId, mHourNPicker, mMinuteNPicker);
    }

    public void setLineSpacingMultiplier(float multiplier) {
        super.setLineSpacingMultiplier(multiplier, mHourNPicker, mMinuteNPicker);
    }

    public void setMaxFlingVelocityCoefficient(int coefficient) {
        super.setMaxFlingVelocityCoefficient(coefficient, mHourNPicker, mMinuteNPicker);
    }

    public void setImeOptions(int imeOptions) {
        super.setImeOptions(imeOptions, mHourNPicker, mMinuteNPicker);
    }

    public void setItemSpacing(int itemSpacing) {
        super.setItemSpacing(itemSpacing, mHourNPicker, mMinuteNPicker);
    }
}
