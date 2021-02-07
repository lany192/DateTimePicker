package com.github.lany192.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.github.lany192.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

/**
 * 年月日时分秒
 */
public class DateTimePicker extends BasePicker {
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;

    private NumberPicker mSecondNPicker;
    private NumberPicker mMinuteNPicker;
    private NumberPicker mHourNPicker;
    private NumberPicker mDayNPicker;
    private NumberPicker mMonthNPicker;
    private NumberPicker mYearNPicker;

    private Locale mCurrentLocale;
    private OnChangedListener mOnChangedListener;
    private String[] mShortMonths;
    private int mNumberOfMonths;

    private Calendar mTempDate;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private Calendar mCurrentDate;

    private boolean mIsEnabled = true;

    public DateTimePicker(Context context) {
        super(context);
        init(null);
    }

    public DateTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DateTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.date_time_picker, this);
        setCurrentLocale(Locale.getDefault());
        int startYear = DEFAULT_START_YEAR;
        int endYear = DEFAULT_END_YEAR;
        String minDate = "";
        String maxDate = "";
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DateTimePicker);
            startYear = typedArray.getInt(R.styleable.DateTimePicker_picker_startYear, DEFAULT_START_YEAR);
            endYear = typedArray.getInt(R.styleable.DateTimePicker_picker_endYear, DEFAULT_END_YEAR);
            minDate = typedArray.getString(R.styleable.DateTimePicker_picker_minDate);
            maxDate = typedArray.getString(R.styleable.DateTimePicker_picker_maxDate);


            int solidColor = typedArray.getColor(R.styleable.DateTimePicker_picker_solidColor, 0);
            Drawable selectionDivider = typedArray.getDrawable(R.styleable.DateTimePicker_picker_selectionDivider);
            int selectionDividerHeight = typedArray.getDimensionPixelSize(R.styleable.DateTimePicker_picker_selectionDividerHeight, dp2px(2));
            int selectionDividersDistance = typedArray.getDimensionPixelSize(R.styleable.DateTimePicker_picker_selectionDividersDistance, dp2px(2));
            int minHeight = typedArray.getDimensionPixelSize(R.styleable.DateTimePicker_picker_internalMinHeight, SIZE_UNSPECIFIED);
            int maxHeight = typedArray.getDimensionPixelSize(R.styleable.DateTimePicker_picker_internalMaxHeight, SIZE_UNSPECIFIED);
            if (minHeight != SIZE_UNSPECIFIED && maxHeight != SIZE_UNSPECIFIED && minHeight > maxHeight) {
                throw new IllegalArgumentException("minHeight > maxHeight");
            }
            int mMinWidth = typedArray.getDimensionPixelSize(R.styleable.DateTimePicker_picker_internalMinWidth, SIZE_UNSPECIFIED);
            int mMaxWidth = typedArray.getDimensionPixelSize(R.styleable.DateTimePicker_picker_internalMaxWidth, SIZE_UNSPECIFIED);
            if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED && mMinWidth > mMaxWidth) {
                throw new IllegalArgumentException("minWidth > maxWidth");
            }
            int selectionTextSize = (int) typedArray.getDimension(R.styleable.DateTimePicker_picker_selectionTextSize, SIZE_UNSPECIFIED);
            int selectionTextColor = typedArray.getColor(R.styleable.DateTimePicker_picker_selectionTextColor, Color.BLACK);

            typedArray.recycle();
        }

        NumberPicker.OnValueChangeListener onChangeListener = new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
                updateInputState();
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                // take care of wrapping of days and months to update greater
                // fields
                if (picker == mDayNPicker) {
                    int maxDayOfMonth = mTempDate
                            .getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (oldValue == maxDayOfMonth && newValue == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (oldValue == 1 && newValue == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newValue - oldValue);
                    }
                } else if (picker == mMonthNPicker) {
                    if (oldValue == 11 && newValue == 0) {
                        mTempDate.add(Calendar.MONTH, 1);
                    } else if (oldValue == 0 && newValue == 11) {
                        mTempDate.add(Calendar.MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.MONTH, newValue - oldValue);
                    }
                } else if (picker == mYearNPicker) {
                    mTempDate.set(Calendar.YEAR, newValue);
                } else if (picker == mHourNPicker) {
                    mTempDate.set(Calendar.HOUR_OF_DAY, newValue);
                } else if (picker == mMinuteNPicker) {
                    mTempDate.set(Calendar.MINUTE, newValue);
                } else if (picker == mSecondNPicker) {
                    mTempDate.set(Calendar.SECOND, newValue);
                } else {
                    throw new IllegalArgumentException();
                }
                // now set the date to the adjusted one
                setDate(mTempDate.get(Calendar.YEAR),
                        mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH),
                        mTempDate.get(Calendar.HOUR_OF_DAY),
                        mTempDate.get(Calendar.MINUTE),
                        mTempDate.get(Calendar.SECOND));
                updateNPickers();
                notifyDateChanged();
            }
        };
        // year
        mYearNPicker = findViewById(R.id.year);
        mYearNPicker.setOnLongPressUpdateInterval(100);
        mYearNPicker.setOnChangedListener(onChangeListener);
        mYearNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // month
        mMonthNPicker = findViewById(R.id.month);
        mMonthNPicker.setMinValue(0);
        mMonthNPicker.setMaxValue(mNumberOfMonths - 1);
        mMonthNPicker.setDisplayedValues(mShortMonths);
        mMonthNPicker.setOnLongPressUpdateInterval(200);
        mMonthNPicker.setOnChangedListener(onChangeListener);
        mMonthNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // day
        mDayNPicker = findViewById(R.id.day);
        mDayNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mDayNPicker.setOnLongPressUpdateInterval(100);
        mDayNPicker.setOnChangedListener(onChangeListener);
        mDayNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // hour
        mHourNPicker = findViewById(R.id.hour);
        mHourNPicker.setOnLongPressUpdateInterval(100);
        mHourNPicker.setOnChangedListener(onChangeListener);
        mHourNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // minute
        mMinuteNPicker = findViewById(R.id.minute);
        mMinuteNPicker.setMinValue(0);
        mMinuteNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteNPicker.setOnChangedListener(onChangeListener);
        mMinuteNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        mSecondNPicker = findViewById(R.id.second);
        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondNPicker.setOnChangedListener(onChangeListener);
        mSecondNPicker.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // set the min date giving priority of the minDate over startYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(minDate)) {
            if (!parseDate(minDate, mTempDate)) {
                mTempDate.set(startYear, 0, 1);
            }
        } else {
            mTempDate.set(startYear, 0, 1);
        }
        setMinDate(mTempDate.getTimeInMillis());

        // set the max date giving priority of the maxDate over endYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(maxDate)) {
            if (!parseDate(maxDate, mTempDate)) {
                mTempDate.set(endYear, 11, 31);
            }
        } else {
            mTempDate.set(endYear, 11, 31);
        }
        setMaxDate(mTempDate.getTimeInMillis());

        // initialize to current date
        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate.get(Calendar.YEAR),
                mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH),
                mCurrentDate.get(Calendar.HOUR_OF_DAY),
                mCurrentDate.get(Calendar.MINUTE),
                mCurrentDate.get(Calendar.SECOND));

        // re-order the number NPickers to match the current date format
        reorderNPickers();

        // If not explicitly specified this view is important for accessibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    public void setOnChangedListener(OnChangedListener listener) {
        mOnChangedListener = listener;
    }

    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate
                .get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        }
        updateNPickers();
    }

    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate
                .get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMaxDate.setTimeInMillis(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
        updateNPickers();
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
        mHourNPicker.setEnabled(enabled);
        mDayNPicker.setEnabled(enabled);
        mMonthNPicker.setEnabled(enabled);
        mYearNPicker.setEnabled(enabled);
        mMinuteNPicker.setEnabled(enabled);
        mSecondNPicker.setEnabled(enabled);
        mIsEnabled = enabled;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(), mCurrentDate.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(DateTimePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(DateTimePicker.class.getName());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);

        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1;
        mShortMonths = new String[mNumberOfMonths];
        for (int i = 0; i < mNumberOfMonths; i++) {
            mShortMonths[i] = DateUtils.getMonthString(Calendar.JANUARY + i, DateUtils.LENGTH_MEDIUM);
        }
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    private void reorderNPickers() {
        char[] order;
        try {
            order = DateFormat.getDateFormatOrder(getContext());
        } catch (IllegalArgumentException expected) {
            order = new char[0];
        }
        final int NPickerCount = order.length;
        for (int i = 0; i < NPickerCount; i++) {
            switch (order[i]) {
                case 's':
                    setImeOptions(mSecondNPicker, NPickerCount, i);
                    break;
                case 'm':
                    setImeOptions(mMinuteNPicker, NPickerCount, i);
                    break;
                case 'h':
                    setImeOptions(mHourNPicker, NPickerCount, i);
                    break;
                case 'd':
                    setImeOptions(mDayNPicker, NPickerCount, i);
                    break;
                case 'M':
                    setImeOptions(mMonthNPicker, NPickerCount, i);
                    break;
                case 'y':
                    setImeOptions(mYearNPicker, NPickerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public void updateDate(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        if (!isNewDate(year, month, dayOfMonth, hourOfDay, minute, second)) {
            return;
        }
        setDate(year, month, dayOfMonth, hourOfDay, minute, second);
        updateNPickers();
        notifyDateChanged();
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth(), getHourOfDay(), getMinute(), getSecond());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay, ss.mHour, ss.mMinute, ss.mSecond);
        updateNPickers();
    }

    public void init(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute, int second) {
        setDate(year, monthOfYear, dayOfMonth, hourOfDay, minute, second);
        updateNPickers();
    }

    private boolean parseDate(String date, Calendar outDate) {
        try {
            SimpleDateFormat mDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            outDate.setTime(Objects.requireNonNull(mDateFormat.parse(date)));
            return true;
        } catch (ParseException e) {
            Log.w(TAG, "Date: " + date + " not in format: MM/dd/yyyy");
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        return (mCurrentDate.get(Calendar.YEAR) != year
                || mCurrentDate.get(Calendar.MONTH) != dayOfMonth
                || mCurrentDate.get(Calendar.DAY_OF_MONTH) != month
                || mCurrentDate.get(Calendar.HOUR_OF_DAY) != hourOfDay
                || mCurrentDate.get(Calendar.MINUTE) != minute
                || mCurrentDate.get(Calendar.SECOND) != second);
    }

    private void setDate(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        mCurrentDate.set(year, month, dayOfMonth, hourOfDay, minute, second);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }

    private void updateNPickers() {
        if (mCurrentDate.equals(mMinDate)) {
            mDayNPicker.setMinValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mDayNPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDayNPicker.setWrapSelectorWheel(false);
            mMonthNPicker.setDisplayedValues(null);
            mMonthNPicker.setMinValue(mCurrentDate.get(Calendar.MONTH));
            mMonthNPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.MONTH));
            mMonthNPicker.setWrapSelectorWheel(false);
        } else if (mCurrentDate.equals(mMaxDate)) {
            mDayNPicker.setMinValue(mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            mDayNPicker.setMaxValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mDayNPicker.setWrapSelectorWheel(false);
            mMonthNPicker.setDisplayedValues(null);
            mMonthNPicker.setMinValue(mCurrentDate.getActualMinimum(Calendar.MONTH));
            mMonthNPicker.setMaxValue(mCurrentDate.get(Calendar.MONTH));
            mMonthNPicker.setWrapSelectorWheel(false);
        } else {
            mDayNPicker.setMinValue(1);
            mDayNPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDayNPicker.setWrapSelectorWheel(true);
            mMonthNPicker.setDisplayedValues(null);
            mMonthNPicker.setMinValue(0);
            mMonthNPicker.setMaxValue(11);
            mMonthNPicker.setWrapSelectorWheel(true);
        }

        // make sure the month names are a zero based array
        // with the months in the month NPicker
        String[] displayedValues = Arrays.copyOfRange(mShortMonths, mMonthNPicker.getMinValue(), mMonthNPicker.getMaxValue() + 1);
        mMonthNPicker.setDisplayedValues(displayedValues);

        // year NPicker range does not change based on the current date
        mYearNPicker.setMinValue(mMinDate.get(Calendar.YEAR));
        mYearNPicker.setMaxValue(mMaxDate.get(Calendar.YEAR));
        mYearNPicker.setWrapSelectorWheel(false);

        mHourNPicker.setMinValue(0);
        mHourNPicker.setMaxValue(23);
        mHourNPicker.setWrapSelectorWheel(true);

        mMinuteNPicker.setMinValue(0);
        mMinuteNPicker.setMaxValue(59);
        mMinuteNPicker.setWrapSelectorWheel(true);

        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mSecondNPicker.setWrapSelectorWheel(true);

        // set the NPicker values
        mYearNPicker.setValue(mCurrentDate.get(Calendar.YEAR));
        mMonthNPicker.setValue(mCurrentDate.get(Calendar.MONTH));
        mDayNPicker.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
        mHourNPicker.setValue(mCurrentDate.get(Calendar.HOUR_OF_DAY));
        mMinuteNPicker.setValue(mCurrentDate.get(Calendar.MINUTE));
        mSecondNPicker.setValue(mCurrentDate.get(Calendar.SECOND));
    }

    /**
     * @return The selected year.
     */
    public int getYear() {
        return mCurrentDate.get(Calendar.YEAR);
    }

    /**
     * @return The selected month.
     */
    public int getMonth() {
        return mCurrentDate.get(Calendar.MONTH);
    }

    /**
     * @return The selected day of month.
     */
    public int getDayOfMonth() {
        return mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * @return The selected day of month.
     */
    public int getHourOfDay() {
        return mCurrentDate.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return mCurrentDate.get(Calendar.MINUTE);
    }

    public int getSecond() {
        return mCurrentDate.get(Calendar.SECOND);
    }

    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnChangedListener != null) {
            mOnChangedListener.onChanged(this, getYear(), getMonth(), getDayOfMonth(), getHourOfDay(), getMinute(), getSecond());
        }
    }

    /**
     * Sets the IME options for a NPicker based on its ordering.
     *
     * @param numberPicker The NPicker.
     * @param pickerCount  The total NPicker count.
     * @param pickerIndex  The index of the given NPicker.
     */
    private void setImeOptions(NumberPicker numberPicker, int pickerCount, int pickerIndex) {
        final int imeOptions;
        if (pickerIndex < pickerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        numberPicker.setImeOptions(imeOptions);
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mYearNPicker)) {
                mYearNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMonthNPicker)) {
                mMonthNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mDayNPicker)) {
                mDayNPicker.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mHourNPicker)) {
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
        void onChanged(DateTimePicker picker, int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute, int second);
    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Creator<SavedState> CREATOR = new Creator<DateTimePicker.SavedState>() {

            public DateTimePicker.SavedState createFromParcel(Parcel in) {
                return new DateTimePicker.SavedState(in);
            }

            public DateTimePicker.SavedState[] newArray(int size) {
                return new DateTimePicker.SavedState[size];
            }
        };
        private final int mYear;
        private final int mMonth;
        private final int mDay;
        private final int mHour;
        private final int mMinute;
        private final int mSecond;

        /**
         * Constructor called from {@link DateTimePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day, int hour, int minute, int second) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
            mHour = hour;
            mMinute = minute;
            mSecond = second;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
            mHour = in.readInt();
            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }
    }

    public void setAccessibilityDescriptionEnabled(boolean enabled) {
        super.setAccessibilityDescriptionEnabled(enabled, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerColor(@ColorInt int color) {
        super.setDividerColor(color, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerColorResource(@ColorRes int colorId) {
        super.setDividerColor(ContextCompat.getColor(getContext(), colorId), mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerDistance(int distance) {
        super.setDividerDistance(distance, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerDistanceResource(@DimenRes int dimenId) {
        super.setDividerDistanceResource(dimenId, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerType(@NumberPicker.DividerType int dividerType) {
        super.setDividerType(dividerType, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerThickness(int thickness) {
        super.setDividerThickness(thickness, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setDividerThicknessResource(@DimenRes int dimenId) {
        super.setDividerThicknessResource(dimenId, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setOrder(@NumberPicker.Order int order) {
        super.setOrder(order, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setOrientation(@NumberPicker.Orientation int orientation) {
        super.setOrientation(orientation, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setWheelItemCount(int count) {
        super.setWheelItemCount(count, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setFormatter(String yearFormatter, String monthFormatter, String dayFormatter,
                             String hourFormatter, String minuteFormatter, String secondFormatter) {
        mYearNPicker.setFormatter(yearFormatter);
        mMonthNPicker.setFormatter(monthFormatter);
        mDayNPicker.setFormatter(dayFormatter);
        mHourNPicker.setFormatter(hourFormatter);
        mMinuteNPicker.setFormatter(minuteFormatter);
        mSecondNPicker.setFormatter(secondFormatter);
    }

    public void setFormatter(@StringRes int yearFormatterId, @StringRes int monthFormatterId, @StringRes int dayFormatterId,
                             @StringRes int hourFormatterId, @StringRes int minuteFormatterId, @StringRes int secondFormatterId) {
        mYearNPicker.setFormatter(getResources().getString(yearFormatterId));
        mMonthNPicker.setFormatter(getResources().getString(monthFormatterId));
        mDayNPicker.setFormatter(getResources().getString(dayFormatterId));
        mHourNPicker.setFormatter(getResources().getString(hourFormatterId));
        mMinuteNPicker.setFormatter(getResources().getString(minuteFormatterId));
        mSecondNPicker.setFormatter(getResources().getString(secondFormatterId));
    }

    public void setFadingEdgeEnabled(boolean fadingEdgeEnabled) {
        super.setFadingEdgeEnabled(fadingEdgeEnabled, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setFadingEdgeStrength(float strength) {
        super.setFadingEdgeStrength(strength, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setScrollerEnabled(boolean scrollerEnabled) {
        super.setScrollerEnabled(scrollerEnabled, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextAlign(@NumberPicker.Align int align) {
        super.setSelectedTextAlign(align, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextColor(@ColorInt int color) {
        super.setSelectedTextColor(color, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextColorResource(@ColorRes int colorId) {
        super.setSelectedTextColorResource(colorId, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextSize(float textSize) {
        super.setSelectedTextSize(textSize, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextSize(@DimenRes int dimenId) {
        super.setSelectedTextSize(getResources().getDimension(dimenId), mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextStrikeThru(boolean strikeThruText) {
        super.setSelectedTextStrikeThru(strikeThruText, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTextUnderline(boolean underlineText) {
        super.setSelectedTextUnderline(underlineText, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(Typeface typeface) {
        super.setSelectedTypeface(typeface, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(String string, int style) {
        super.setSelectedTypeface(string, style, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(String string) {
        super.setSelectedTypeface(string, Typeface.NORMAL, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(@StringRes int stringId, int style) {
        super.setSelectedTypeface(getResources().getString(stringId), style, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setSelectedTypeface(@StringRes int stringId) {
        super.setSelectedTypeface(stringId, Typeface.NORMAL, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextAlign(@NumberPicker.Align int align) {
        super.setTextAlign(align, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextColor(@ColorInt int color) {
        super.setTextColor(color, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextColorResource(@ColorRes int colorId) {
        super.setTextColorResource(colorId, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextSize(float textSize) {
        super.setTextSize(textSize, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextSize(@DimenRes int dimenId) {
        super.setTextSize(dimenId, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextStrikeThru(boolean strikeThruText) {
        super.setTextStrikeThru(strikeThruText, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTextUnderline(boolean underlineText) {
        super.setTextUnderline(underlineText, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(Typeface typeface) {
        super.setTypeface(typeface, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(String string, int style) {
        super.setTypeface(string, style, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(String string) {
        super.setTypeface(string, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(@StringRes int stringId, int style) {
        super.setTypeface(stringId, style, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setTypeface(@StringRes int stringId) {
        super.setTypeface(stringId, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setLineSpacingMultiplier(float multiplier) {
        super.setLineSpacingMultiplier(multiplier, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setMaxFlingVelocityCoefficient(int coefficient) {
        super.setMaxFlingVelocityCoefficient(coefficient, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setImeOptions(int imeOptions) {
        super.setImeOptions(imeOptions, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }

    public void setItemSpacing(int itemSpacing) {
        super.setItemSpacing(itemSpacing, mYearNPicker, mMonthNPicker, mDayNPicker, mHourNPicker, mMinuteNPicker, mSecondNPicker);
    }
}