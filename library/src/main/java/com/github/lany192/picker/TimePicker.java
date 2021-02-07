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

import com.github.lany192.R;

import java.util.Calendar;
import java.util.Locale;

/**
 * 时分秒 Hour/Minute/Second
 */
public class TimePicker extends BasePicker {
    private static final boolean DEFAULT_ENABLED_STATE = true;

    private final NumberPicker mHourNPicker;
    private final NumberPicker mMinuteNPicker;
    private final NumberPicker mSecondNPicker;

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
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCurrentLocale(Locale.getDefault());

        LayoutInflater.from(getContext()).inflate(R.layout.time_picker, this);

        // hour
        mHourNPicker = findViewById(R.id.picker_time_hour);
        mHourNPicker.setOnValueChangedListener((NPicker, oldVal, newVal) -> {
            updateInputState();
            onTimeChanged();
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
        mMinuteNPicker.setOnValueChangedListener((NPicker, oldVal, newVal) -> {
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
        });
        mMinuteNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        mSecondNPicker = findViewById(R.id.picker_time_second);
        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondNPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            updateInputState();
            picker.requestFocus();
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            onTimeChanged();
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


    public void setAccessibilityDescriptionEnabled(boolean enabled) {
        mHourNPicker.setAccessibilityDescriptionEnabled(enabled);
        mMinuteNPicker.setAccessibilityDescriptionEnabled(enabled);
        mSecondNPicker.setAccessibilityDescriptionEnabled(enabled);
    }

    public void setDividerColor(@ColorInt int color) {
        mHourNPicker.setDividerColor(color);
        mMinuteNPicker.setDividerColor(color);
        mSecondNPicker.setDividerColor(color);
    }

    public void setDividerColorResource(@ColorRes int colorId) {
        mHourNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mMinuteNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mSecondNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
    }

    public void setDividerDistance(int distance) {
        mHourNPicker.setDividerDistance(distance);
        mMinuteNPicker.setDividerDistance(distance);
        mSecondNPicker.setDividerDistance(distance);
    }

    public void setDividerDistanceResource(@DimenRes int dimenId) {
        mHourNPicker.setDividerDistanceResource(dimenId);
        mMinuteNPicker.setDividerDistanceResource(dimenId);
        mSecondNPicker.setDividerDistanceResource(dimenId);
    }

    public void setDividerType(@NumberPicker.DividerType int dividerType) {
        mHourNPicker.setDividerType(dividerType);
        mMinuteNPicker.setDividerType(dividerType);
        mSecondNPicker.setDividerType(dividerType);
    }

    public void setDividerThickness(int thickness) {
        mHourNPicker.setDividerThickness(thickness);
        mMinuteNPicker.setDividerThickness(thickness);
        mSecondNPicker.setDividerThickness(thickness);
    }

    public void setDividerThicknessResource(@DimenRes int dimenId) {
        mHourNPicker.setDividerThicknessResource(dimenId);
        mMinuteNPicker.setDividerThicknessResource(dimenId);
        mSecondNPicker.setDividerThicknessResource(dimenId);
    }

    public void setOrder(@NumberPicker.Order int order) {
        mHourNPicker.setOrder(order);
        mMinuteNPicker.setOrder(order);
        mSecondNPicker.setOrder(order);
    }

    public void setOrientation(@NumberPicker.Orientation int orientation) {
        mHourNPicker.setOrientation(orientation);
        mMinuteNPicker.setOrientation(orientation);
        mSecondNPicker.setOrientation(orientation);
    }

    public void setWheelItemCount(int count) {
        mHourNPicker.setWheelItemCount(count);
        mMinuteNPicker.setWheelItemCount(count);
        mSecondNPicker.setWheelItemCount(count);
    }

    public void setFormatter(final String formatter) {
        mHourNPicker.setFormatter(formatter);
        mMinuteNPicker.setFormatter(formatter);
        mSecondNPicker.setFormatter(formatter);
    }

    public void setFormatter(@StringRes int stringId) {
        mHourNPicker.setFormatter(getResources().getString(stringId));
        mMinuteNPicker.setFormatter(getResources().getString(stringId));
        mSecondNPicker.setFormatter(getResources().getString(stringId));
    }

    public void setFadingEdgeEnabled(boolean fadingEdgeEnabled) {
        mHourNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mMinuteNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mSecondNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
    }

    public void setFadingEdgeStrength(float strength) {
        mHourNPicker.setFadingEdgeStrength(strength);
        mMinuteNPicker.setFadingEdgeStrength(strength);
        mSecondNPicker.setFadingEdgeStrength(strength);
    }

    public void setScrollerEnabled(boolean scrollerEnabled) {
        mHourNPicker.setScrollerEnabled(scrollerEnabled);
        mMinuteNPicker.setScrollerEnabled(scrollerEnabled);
        mSecondNPicker.setScrollerEnabled(scrollerEnabled);
    }

    public void setSelectedTextAlign(@NumberPicker.Align int align) {
        mHourNPicker.setSelectedTextAlign(align);
        mMinuteNPicker.setSelectedTextAlign(align);
        mSecondNPicker.setSelectedTextAlign(align);
    }

    public void setSelectedTextColor(@ColorInt int color) {
        mHourNPicker.setSelectedTextColor(color);
        mMinuteNPicker.setSelectedTextColor(color);
        mSecondNPicker.setSelectedTextColor(color);
    }

    public void setSelectedTextColorResource(@ColorRes int colorId) {
        mHourNPicker.setSelectedTextColorResource(colorId);
        mMinuteNPicker.setSelectedTextColorResource(colorId);
        mSecondNPicker.setSelectedTextColorResource(colorId);
    }

    public void setSelectedTextSize(float textSize) {
        mHourNPicker.setSelectedTextSize(textSize);
        mMinuteNPicker.setSelectedTextSize(textSize);
        mSecondNPicker.setSelectedTextSize(textSize);
    }

    public void setSelectedTextSize(@DimenRes int dimenId) {
        mHourNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mMinuteNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mSecondNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
    }

    public void setSelectedTextStrikeThru(boolean strikeThruText) {
        mHourNPicker.setSelectedTextStrikeThru(strikeThruText);
        mMinuteNPicker.setSelectedTextStrikeThru(strikeThruText);
        mSecondNPicker.setSelectedTextStrikeThru(strikeThruText);
    }

    public void setSelectedTextUnderline(boolean underlineText) {
        mHourNPicker.setSelectedTextUnderline(underlineText);
        mMinuteNPicker.setSelectedTextUnderline(underlineText);
        mSecondNPicker.setSelectedTextUnderline(underlineText);
    }

    public void setSelectedTypeface(Typeface typeface) {
        mHourNPicker.setSelectedTypeface(typeface);
        mMinuteNPicker.setSelectedTypeface(typeface);
        mSecondNPicker.setSelectedTypeface(typeface);
    }

    public void setSelectedTypeface(String string, int style) {
        mHourNPicker.setSelectedTypeface(string, style);
        mMinuteNPicker.setSelectedTypeface(string, style);
        mSecondNPicker.setSelectedTypeface(string, style);
    }

    public void setSelectedTypeface(String string) {
        mHourNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mMinuteNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mSecondNPicker.setSelectedTypeface(string, Typeface.NORMAL);
    }

    public void setSelectedTypeface(@StringRes int stringId, int style) {
        mHourNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mMinuteNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mSecondNPicker.setSelectedTypeface(getResources().getString(stringId), style);
    }

    public void setSelectedTypeface(@StringRes int stringId) {
        mHourNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mMinuteNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mSecondNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
    }

    public void setTextAlign(@NumberPicker.Align int align) {
        mHourNPicker.setTextAlign(align);
        mMinuteNPicker.setTextAlign(align);
        mSecondNPicker.setTextAlign(align);
    }

    public void setTextColor(@ColorInt int color) {
        mHourNPicker.setTextColor(color);
        mMinuteNPicker.setTextColor(color);
        mSecondNPicker.setTextColor(color);
    }

    public void setTextColorResource(@ColorRes int colorId) {
        mHourNPicker.setTextColorResource(colorId);
        mMinuteNPicker.setTextColorResource(colorId);
        mSecondNPicker.setTextColorResource(colorId);
    }

    public void setTextSize(float textSize) {
        mHourNPicker.setTextSize(textSize);
        mMinuteNPicker.setTextSize(textSize);
        mSecondNPicker.setTextSize(textSize);
    }

    public void setTextSize(@DimenRes int dimenId) {
        mHourNPicker.setTextSize(dimenId);
        mMinuteNPicker.setTextSize(dimenId);
        mSecondNPicker.setTextSize(dimenId);
    }

    public void setTextStrikeThru(boolean strikeThruText) {
        mHourNPicker.setTextStrikeThru(strikeThruText);
        mMinuteNPicker.setTextStrikeThru(strikeThruText);
        mSecondNPicker.setTextStrikeThru(strikeThruText);
    }

    public void setTextUnderline(boolean underlineText) {
        mHourNPicker.setTextUnderline(underlineText);
        mMinuteNPicker.setTextUnderline(underlineText);
        mSecondNPicker.setTextUnderline(underlineText);
    }

    public void setTypeface(Typeface typeface) {
        mHourNPicker.setTypeface(typeface);
        mMinuteNPicker.setTypeface(typeface);
        mSecondNPicker.setTypeface(typeface);
    }

    public void setTypeface(String string, int style) {
        mHourNPicker.setTypeface(string, style);
        mMinuteNPicker.setTypeface(string, style);
        mSecondNPicker.setTypeface(string, style);
    }

    public void setTypeface(String string) {
        mHourNPicker.setTypeface(string);
        mMinuteNPicker.setTypeface(string);
        mSecondNPicker.setTypeface(string);
    }

    public void setTypeface(@StringRes int stringId, int style) {
        mHourNPicker.setTypeface(stringId, style);
        mMinuteNPicker.setTypeface(stringId, style);
        mSecondNPicker.setTypeface(stringId, style);
    }

    public void setTypeface(@StringRes int stringId) {
        mHourNPicker.setTypeface(stringId);
        mMinuteNPicker.setTypeface(stringId);
        mSecondNPicker.setTypeface(stringId);
    }

    public void setLineSpacingMultiplier(float multiplier) {
        mHourNPicker.setLineSpacingMultiplier(multiplier);
        mMinuteNPicker.setLineSpacingMultiplier(multiplier);
        mSecondNPicker.setLineSpacingMultiplier(multiplier);
    }

    public void setMaxFlingVelocityCoefficient(int coefficient) {
        mHourNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mMinuteNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mSecondNPicker.setMaxFlingVelocityCoefficient(coefficient);
    }

    public void setImeOptions(int imeOptions) {
        mHourNPicker.setImeOptions(imeOptions);
        mMinuteNPicker.setImeOptions(imeOptions);
        mSecondNPicker.setImeOptions(imeOptions);
    }

    public void setItemSpacing(int itemSpacing) {
        mHourNPicker.setItemSpacing(itemSpacing);
        mMinuteNPicker.setItemSpacing(itemSpacing);
        mSecondNPicker.setItemSpacing(itemSpacing);
    }
}
