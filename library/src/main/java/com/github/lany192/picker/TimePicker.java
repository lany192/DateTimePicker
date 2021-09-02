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
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Locale;

/**
 * 时分秒 Hour/Minute/Second
 */
public class TimePicker extends BasePicker {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final boolean DEFAULT_AUTO_SCROLL_STATE = true;

    private final NumberPicker mHourNPicker;
    private final NumberPicker mMinuteNPicker;
    private final NumberPicker mSecondNPicker;

    private final TextView mFirstDivider;
    private final TextView mSecondDivider;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    private boolean mIsAutoScroll = DEFAULT_AUTO_SCROLL_STATE;

    private OnChangedListener mOnChangedListener;

    private Calendar mTempCalendar;

    private Locale mCurrentLocale;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCurrentLocale(Locale.getDefault());

        LayoutInflater.from(getContext()).inflate(R.layout.time_picker, this);

        // hour
        mHourNPicker = findViewById(R.id.picker_time_hour);
        mHourNPicker.setOnChangedListener((NPicker, oldVal, newVal) -> {
            updateInputState();
            onChanged();
        });
        mHourNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
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
        mMinuteNPicker.setOnChangedListener((NPicker, oldVal, newVal) -> {
            updateInputState();
            int minValue = mMinuteNPicker.getMinValue();
            int maxValue = mMinuteNPicker.getMaxValue();
            if (oldVal == maxValue && newVal == minValue && mIsAutoScroll) {
                int newHour = mHourNPicker.getValue() + 1;
                mHourNPicker.setValue(newHour);
            } else if (oldVal == minValue && newVal == maxValue && mIsAutoScroll) {
                int newHour = mHourNPicker.getValue() - 1;
                mHourNPicker.setValue(newHour);
            }
            onChanged();
        });
        mMinuteNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        mSecondNPicker = findViewById(R.id.picker_time_second);
        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondNPicker.setOnChangedListener((picker, oldVal, newVal) -> {
            updateInputState();
            picker.requestFocus();
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            onChanged();
        });
        mSecondNPicker.setImeOptions(EditorInfo.IME_ACTION_DONE);

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

    /**
     * Sets the automatic scrolling of items in the picker.
     */
    public void setIsAutoScrollState(Boolean isAutoScrollState) {
        if (mIsAutoScroll == isAutoScrollState) {
            return;
        }
        mIsAutoScroll = isAutoScrollState;
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

    public void setOnChangedListener(OnChangedListener listener) {
        mOnChangedListener = listener;
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
        onChanged();
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
        onChanged();
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
        onChanged();
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

    private void onChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnChangedListener != null) {
            mOnChangedListener.onChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSecond());
        }
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mHourNPicker)) {
                mHourNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMinuteNPicker)) {
                mMinuteNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mSecondNPicker)) {
                mSecondNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    public interface OnChangedListener {
        void onChanged(TimePicker picker, int hourOfDay, int minute, int second);
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


    public void setAccessibilityDescriptionEnabled(boolean enabled) {
        super.setAccessibilityDescriptionEnabled(enabled, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerColor(@ColorInt int color) {
        super.setDividerColor(color, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerColorResource(@ColorRes int colorId) {
        super.setDividerColor(ContextCompat.getColor(getContext(), colorId), mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerDistance(int distance) {
        super.setDividerDistance(distance, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerDistanceResource(@DimenRes int dimenId) {
        super.setDividerDistanceResource(dimenId, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerType(@NumberPicker.DividerType int dividerType) {
        super.setDividerType(dividerType, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerThickness(int thickness) {
        super.setDividerThickness(thickness, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerThicknessResource(@DimenRes int dimenId) {
        super.setDividerThicknessResource(dimenId, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setOrder(@NumberPicker.Order int order) {
        super.setOrder(order, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setOrientation(@NumberPicker.Orientation int orientation) {
        super.setOrientation(orientation, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setWheelItemCount(int count) {
        super.setWheelItemCount(count, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setFormatter(String hourFormatter, String minuteFormatter, String secondFormatter) {
        mHourNPicker.setFormatter(hourFormatter);
        mMinuteNPicker.setFormatter(minuteFormatter);
        mSecondNPicker.setFormatter(secondFormatter);
    }

    public void setFormatter(@StringRes int hourFormatterId, @StringRes int minuteFormatterId, @StringRes int secondFormatterId) {
        mHourNPicker.setFormatter(getResources().getString(hourFormatterId));
        mMinuteNPicker.setFormatter(getResources().getString(minuteFormatterId));
        mSecondNPicker.setFormatter(getResources().getString(secondFormatterId));
    }

    public void setFadingEdgeEnabled(boolean fadingEdgeEnabled) {
        super.setFadingEdgeEnabled(fadingEdgeEnabled, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setFadingEdgeStrength(float strength) {
        super.setFadingEdgeStrength(strength, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setScrollerEnabled(boolean scrollerEnabled) {
        super.setScrollerEnabled(scrollerEnabled, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextAlign(@NumberPicker.Align int align) {
        super.setSelectedTextAlign(align, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextColor(@ColorInt int color) {
        super.setSelectedTextColor(color, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextColorResource(@ColorRes int colorId) {
        super.setSelectedTextColorResource(colorId, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextSize(float textSize) {
        super.setSelectedTextSize(textSize, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextSize(@DimenRes int dimenId) {
        super.setSelectedTextSize(getResources().getDimension(dimenId), mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextStrikeThru(boolean strikeThruText) {
        super.setSelectedTextStrikeThru(strikeThruText, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextUnderline(boolean underlineText) {
        super.setSelectedTextUnderline(underlineText, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(Typeface typeface) {
        super.setSelectedTypeface(typeface, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(String string, int style) {
        super.setSelectedTypeface(string, style, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(String string) {
        super.setSelectedTypeface(string, Typeface.NORMAL, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(@StringRes int stringId, int style) {
        super.setSelectedTypeface(getResources().getString(stringId), style, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(@StringRes int stringId) {
        super.setSelectedTypeface(stringId, Typeface.NORMAL, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextAlign(@NumberPicker.Align int align) {
        super.setTextAlign(align, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextColor(@ColorInt int color) {
        super.setTextColor(color, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextColorResource(@ColorRes int colorId) {
        super.setTextColorResource(colorId, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextSize(float textSize) {
        super.setTextSize(textSize, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextSize(@DimenRes int dimenId) {
        super.setTextSize(dimenId, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextStrikeThru(boolean strikeThruText) {
        super.setTextStrikeThru(strikeThruText, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextUnderline(boolean underlineText) {
        super.setTextUnderline(underlineText, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(Typeface typeface) {
        super.setTypeface(typeface, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(String string, int style) {
        super.setTypeface(string, style, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(String string) {
        super.setTypeface(string, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(@StringRes int stringId, int style) {
        super.setTypeface(stringId, style, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(@StringRes int stringId) {
        super.setTypeface(stringId, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setLineSpacingMultiplier(float multiplier) {
        super.setLineSpacingMultiplier(multiplier, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setMaxFlingVelocityCoefficient(int coefficient) {
        super.setMaxFlingVelocityCoefficient(coefficient, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setImeOptions(int imeOptions) {
        super.setImeOptions(imeOptions, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setItemSpacing(int itemSpacing) {
        super.setItemSpacing(itemSpacing, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }
}
