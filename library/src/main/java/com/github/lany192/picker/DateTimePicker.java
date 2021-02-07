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
        mYearNPicker.setOnValueChangedListener(onChangeListener);
        mYearNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // month
        mMonthNPicker = findViewById(R.id.month);
        mMonthNPicker.setMinValue(0);
        mMonthNPicker.setMaxValue(mNumberOfMonths - 1);
        mMonthNPicker.setDisplayedValues(mShortMonths);
        mMonthNPicker.setOnLongPressUpdateInterval(200);
        mMonthNPicker.setOnValueChangedListener(onChangeListener);
        mMonthNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // day
        mDayNPicker = findViewById(R.id.day);
        mDayNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mDayNPicker.setOnLongPressUpdateInterval(100);
        mDayNPicker.setOnValueChangedListener(onChangeListener);
        mDayNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // hour
        mHourNPicker = findViewById(R.id.hour);
        mHourNPicker.setOnLongPressUpdateInterval(100);
        mHourNPicker.setOnValueChangedListener(onChangeListener);
        mHourNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // minute
        mMinuteNPicker = findViewById(R.id.minute);
        mMinuteNPicker.setMinValue(0);
        mMinuteNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteNPicker.setOnValueChangedListener(onChangeListener);
        mMinuteNPicker.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        mSecondNPicker = findViewById(R.id.second);
        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondNPicker.setOnValueChangedListener(onChangeListener);
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
        mYearNPicker.setAccessibilityDescriptionEnabled(enabled);
        mMonthNPicker.setAccessibilityDescriptionEnabled(enabled);
        mDayNPicker.setAccessibilityDescriptionEnabled(enabled);
        mHourNPicker.setAccessibilityDescriptionEnabled(enabled);
        mMinuteNPicker.setAccessibilityDescriptionEnabled(enabled);
        mSecondNPicker.setAccessibilityDescriptionEnabled(enabled);
    }

    public void setDividerColor(@ColorInt int color) {
        mDayNPicker.setDividerColor(color);
        mMonthNPicker.setDividerColor(color);
        mYearNPicker.setDividerColor(color);
        mHourNPicker.setDividerColor(color);
        mMinuteNPicker.setDividerColor(color);
        mSecondNPicker.setDividerColor(color);
    }

    public void setDividerColorResource(@ColorRes int colorId) {
        mYearNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mMonthNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mDayNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mHourNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mMinuteNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        mSecondNPicker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
    }

    public void setDividerDistance(int distance) {
        mYearNPicker.setDividerDistance(distance);
        mMonthNPicker.setDividerDistance(distance);
        mDayNPicker.setDividerDistance(distance);
        mHourNPicker.setDividerDistance(distance);
        mMinuteNPicker.setDividerDistance(distance);
        mSecondNPicker.setDividerDistance(distance);
    }

    public void setDividerDistanceResource(@DimenRes int dimenId) {
        mYearNPicker.setDividerDistanceResource(dimenId);
        mMonthNPicker.setDividerDistanceResource(dimenId);
        mDayNPicker.setDividerDistanceResource(dimenId);
        mHourNPicker.setDividerDistanceResource(dimenId);
        mMinuteNPicker.setDividerDistanceResource(dimenId);
        mSecondNPicker.setDividerDistanceResource(dimenId);
    }

    public void setDividerType(@NumberPicker.DividerType int dividerType) {
        mYearNPicker.setDividerType(dividerType);
        mMonthNPicker.setDividerType(dividerType);
        mDayNPicker.setDividerType(dividerType);
        mHourNPicker.setDividerType(dividerType);
        mMinuteNPicker.setDividerType(dividerType);
        mSecondNPicker.setDividerType(dividerType);
    }

    public void setDividerThickness(int thickness) {
        mYearNPicker.setDividerThickness(thickness);
        mMonthNPicker.setDividerThickness(thickness);
        mDayNPicker.setDividerThickness(thickness);
        mHourNPicker.setDividerThickness(thickness);
        mMinuteNPicker.setDividerThickness(thickness);
        mSecondNPicker.setDividerThickness(thickness);
    }

    public void setDividerThicknessResource(@DimenRes int dimenId) {
        mYearNPicker.setDividerThicknessResource(dimenId);
        mMonthNPicker.setDividerThicknessResource(dimenId);
        mDayNPicker.setDividerThicknessResource(dimenId);
        mHourNPicker.setDividerThicknessResource(dimenId);
        mMinuteNPicker.setDividerThicknessResource(dimenId);
        mSecondNPicker.setDividerThicknessResource(dimenId);
    }

    public void setOrder(@NumberPicker.Order int order) {
        mYearNPicker.setOrder(order);
        mMonthNPicker.setOrder(order);
        mDayNPicker.setOrder(order);
        mHourNPicker.setOrder(order);
        mMinuteNPicker.setOrder(order);
        mSecondNPicker.setOrder(order);
    }

    public void setOrientation(@NumberPicker.Orientation int orientation) {
        mYearNPicker.setOrientation(orientation);
        mMonthNPicker.setOrientation(orientation);
        mDayNPicker.setOrientation(orientation);
        mHourNPicker.setOrientation(orientation);
        mMinuteNPicker.setOrientation(orientation);
        mSecondNPicker.setOrientation(orientation);
    }

    public void setWheelItemCount(int count) {
        mYearNPicker.setWheelItemCount(count);
        mMonthNPicker.setWheelItemCount(count);
        mDayNPicker.setWheelItemCount(count);
        mHourNPicker.setWheelItemCount(count);
        mMinuteNPicker.setWheelItemCount(count);
        mSecondNPicker.setWheelItemCount(count);
    }

    public void setFormatter(final String formatter) {
        mYearNPicker.setFormatter(formatter);
        mMonthNPicker.setFormatter(formatter);
        mDayNPicker.setFormatter(formatter);
        mHourNPicker.setFormatter(formatter);
        mMinuteNPicker.setFormatter(formatter);
        mSecondNPicker.setFormatter(formatter);
    }

    public void setFormatter(@StringRes int stringId) {
        mYearNPicker.setFormatter(getResources().getString(stringId));
        mMonthNPicker.setFormatter(getResources().getString(stringId));
        mDayNPicker.setFormatter(getResources().getString(stringId));
        mHourNPicker.setFormatter(getResources().getString(stringId));
        mMinuteNPicker.setFormatter(getResources().getString(stringId));
        mSecondNPicker.setFormatter(getResources().getString(stringId));
    }

    public void setFadingEdgeEnabled(boolean fadingEdgeEnabled) {
        mYearNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mMonthNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mDayNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mHourNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mMinuteNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
        mSecondNPicker.setFadingEdgeEnabled(fadingEdgeEnabled);
    }

    public void setFadingEdgeStrength(float strength) {
        mYearNPicker.setFadingEdgeStrength(strength);
        mMonthNPicker.setFadingEdgeStrength(strength);
        mDayNPicker.setFadingEdgeStrength(strength);
        mHourNPicker.setFadingEdgeStrength(strength);
        mMinuteNPicker.setFadingEdgeStrength(strength);
        mSecondNPicker.setFadingEdgeStrength(strength);
    }

    public void setScrollerEnabled(boolean scrollerEnabled) {
        mYearNPicker.setScrollerEnabled(scrollerEnabled);
        mMonthNPicker.setScrollerEnabled(scrollerEnabled);
        mDayNPicker.setScrollerEnabled(scrollerEnabled);
        mHourNPicker.setScrollerEnabled(scrollerEnabled);
        mMinuteNPicker.setScrollerEnabled(scrollerEnabled);
        mSecondNPicker.setScrollerEnabled(scrollerEnabled);
    }

    public void setSelectedTextAlign(@NumberPicker.Align int align) {
        mYearNPicker.setSelectedTextAlign(align);
        mMonthNPicker.setSelectedTextAlign(align);
        mDayNPicker.setSelectedTextAlign(align);
        mHourNPicker.setSelectedTextAlign(align);
        mMinuteNPicker.setSelectedTextAlign(align);
        mSecondNPicker.setSelectedTextAlign(align);
    }

    public void setSelectedTextColor(@ColorInt int color) {
        mYearNPicker.setSelectedTextColor(color);
        mMonthNPicker.setSelectedTextColor(color);
        mDayNPicker.setSelectedTextColor(color);
        mHourNPicker.setSelectedTextColor(color);
        mMinuteNPicker.setSelectedTextColor(color);
        mSecondNPicker.setSelectedTextColor(color);
    }

    public void setSelectedTextColorResource(@ColorRes int colorId) {
        mYearNPicker.setSelectedTextColorResource(colorId);
        mMonthNPicker.setSelectedTextColorResource(colorId);
        mDayNPicker.setSelectedTextColorResource(colorId);
        mHourNPicker.setSelectedTextColorResource(colorId);
        mMinuteNPicker.setSelectedTextColorResource(colorId);
        mSecondNPicker.setSelectedTextColorResource(colorId);
    }

    public void setSelectedTextSize(float textSize) {
        mYearNPicker.setSelectedTextSize(textSize);
        mMonthNPicker.setSelectedTextSize(textSize);
        mDayNPicker.setSelectedTextSize(textSize);
        mHourNPicker.setSelectedTextSize(textSize);
        mMinuteNPicker.setSelectedTextSize(textSize);
        mSecondNPicker.setSelectedTextSize(textSize);
    }

    public void setSelectedTextSize(@DimenRes int dimenId) {
        mYearNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mMonthNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mDayNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mHourNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mMinuteNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
        mSecondNPicker.setSelectedTextSize(getResources().getDimension(dimenId));
    }

    public void setSelectedTextStrikeThru(boolean strikeThruText) {
        mYearNPicker.setSelectedTextStrikeThru(strikeThruText);
        mMonthNPicker.setSelectedTextStrikeThru(strikeThruText);
        mDayNPicker.setSelectedTextStrikeThru(strikeThruText);
        mHourNPicker.setSelectedTextStrikeThru(strikeThruText);
        mMinuteNPicker.setSelectedTextStrikeThru(strikeThruText);
        mSecondNPicker.setSelectedTextStrikeThru(strikeThruText);
    }

    public void setSelectedTextUnderline(boolean underlineText) {
        mYearNPicker.setSelectedTextUnderline(underlineText);
        mMonthNPicker.setSelectedTextUnderline(underlineText);
        mDayNPicker.setSelectedTextUnderline(underlineText);
        mHourNPicker.setSelectedTextUnderline(underlineText);
        mMinuteNPicker.setSelectedTextUnderline(underlineText);
        mSecondNPicker.setSelectedTextUnderline(underlineText);
    }

    public void setSelectedTypeface(Typeface typeface) {
        mYearNPicker.setSelectedTypeface(typeface);
        mMonthNPicker.setSelectedTypeface(typeface);
        mDayNPicker.setSelectedTypeface(typeface);
        mHourNPicker.setSelectedTypeface(typeface);
        mMinuteNPicker.setSelectedTypeface(typeface);
        mSecondNPicker.setSelectedTypeface(typeface);
    }

    public void setSelectedTypeface(String string, int style) {
        mYearNPicker.setSelectedTypeface(string, style);
        mMonthNPicker.setSelectedTypeface(string, style);
        mDayNPicker.setSelectedTypeface(string, style);
        mHourNPicker.setSelectedTypeface(string, style);
        mMinuteNPicker.setSelectedTypeface(string, style);
        mSecondNPicker.setSelectedTypeface(string, style);
    }

    public void setSelectedTypeface(String string) {
        mYearNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mMonthNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mDayNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mHourNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mMinuteNPicker.setSelectedTypeface(string, Typeface.NORMAL);
        mSecondNPicker.setSelectedTypeface(string, Typeface.NORMAL);
    }

    public void setSelectedTypeface(@StringRes int stringId, int style) {
        mYearNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mMonthNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mDayNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mHourNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mMinuteNPicker.setSelectedTypeface(getResources().getString(stringId), style);
        mSecondNPicker.setSelectedTypeface(getResources().getString(stringId), style);
    }

    public void setSelectedTypeface(@StringRes int stringId) {
        mYearNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mMonthNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mDayNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mHourNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mMinuteNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
        mSecondNPicker.setSelectedTypeface(stringId, Typeface.NORMAL);
    }

    public void setTextAlign(@NumberPicker.Align int align) {
        mYearNPicker.setTextAlign(align);
        mMonthNPicker.setTextAlign(align);
        mDayNPicker.setTextAlign(align);
        mHourNPicker.setTextAlign(align);
        mMinuteNPicker.setTextAlign(align);
        mSecondNPicker.setTextAlign(align);
    }

    public void setTextColor(@ColorInt int color) {
        mYearNPicker.setTextColor(color);
        mMonthNPicker.setTextColor(color);
        mDayNPicker.setTextColor(color);
        mHourNPicker.setTextColor(color);
        mMinuteNPicker.setTextColor(color);
        mSecondNPicker.setTextColor(color);
    }

    public void setTextColorResource(@ColorRes int colorId) {
        mYearNPicker.setTextColorResource(colorId);
        mMonthNPicker.setTextColorResource(colorId);
        mDayNPicker.setTextColorResource(colorId);
        mHourNPicker.setTextColorResource(colorId);
        mMinuteNPicker.setTextColorResource(colorId);
        mSecondNPicker.setTextColorResource(colorId);
    }

    public void setTextSize(float textSize) {
        mYearNPicker.setTextSize(textSize);
        mMonthNPicker.setTextSize(textSize);
        mDayNPicker.setTextSize(textSize);
        mHourNPicker.setTextSize(textSize);
        mMinuteNPicker.setTextSize(textSize);
        mSecondNPicker.setTextSize(textSize);
    }

    public void setTextSize(@DimenRes int dimenId) {
        mYearNPicker.setTextSize(dimenId);
        mMonthNPicker.setTextSize(dimenId);
        mDayNPicker.setTextSize(dimenId);
        mHourNPicker.setTextSize(dimenId);
        mMinuteNPicker.setTextSize(dimenId);
        mSecondNPicker.setTextSize(dimenId);
    }

    public void setTextStrikeThru(boolean strikeThruText) {
        mYearNPicker.setTextStrikeThru(strikeThruText);
        mMonthNPicker.setTextStrikeThru(strikeThruText);
        mDayNPicker.setTextStrikeThru(strikeThruText);
        mHourNPicker.setTextStrikeThru(strikeThruText);
        mMinuteNPicker.setTextStrikeThru(strikeThruText);
        mSecondNPicker.setTextStrikeThru(strikeThruText);
    }

    public void setTextUnderline(boolean underlineText) {
        mYearNPicker.setTextUnderline(underlineText);
        mMonthNPicker.setTextUnderline(underlineText);
        mDayNPicker.setTextUnderline(underlineText);
        mHourNPicker.setTextUnderline(underlineText);
        mMinuteNPicker.setTextUnderline(underlineText);
        mSecondNPicker.setTextUnderline(underlineText);
    }

    public void setTypeface(Typeface typeface) {
        mYearNPicker.setTypeface(typeface);
        mMonthNPicker.setTypeface(typeface);
        mDayNPicker.setTypeface(typeface);
        mHourNPicker.setTypeface(typeface);
        mMinuteNPicker.setTypeface(typeface);
        mSecondNPicker.setTypeface(typeface);
    }

    public void setTypeface(String string, int style) {
        mYearNPicker.setTypeface(string, style);
        mMonthNPicker.setTypeface(string, style);
        mDayNPicker.setTypeface(string, style);
        mHourNPicker.setTypeface(string, style);
        mMinuteNPicker.setTypeface(string, style);
        mSecondNPicker.setTypeface(string, style);
    }

    public void setTypeface(String string) {
        mYearNPicker.setTypeface(string);
        mMonthNPicker.setTypeface(string);
        mDayNPicker.setTypeface(string);
        mHourNPicker.setTypeface(string);
        mMinuteNPicker.setTypeface(string);
        mSecondNPicker.setTypeface(string);
    }

    public void setTypeface(@StringRes int stringId, int style) {
        mYearNPicker.setTypeface(stringId, style);
        mMonthNPicker.setTypeface(stringId, style);
        mDayNPicker.setTypeface(stringId, style);
        mHourNPicker.setTypeface(stringId, style);
        mMinuteNPicker.setTypeface(stringId, style);
        mSecondNPicker.setTypeface(stringId, style);
    }

    public void setTypeface(@StringRes int stringId) {
        mYearNPicker.setTypeface(stringId);
        mMonthNPicker.setTypeface(stringId);
        mDayNPicker.setTypeface(stringId);
        mHourNPicker.setTypeface(stringId);
        mMinuteNPicker.setTypeface(stringId);
        mSecondNPicker.setTypeface(stringId);
    }

    public void setLineSpacingMultiplier(float multiplier) {
        mYearNPicker.setLineSpacingMultiplier(multiplier);
        mMonthNPicker.setLineSpacingMultiplier(multiplier);
        mDayNPicker.setLineSpacingMultiplier(multiplier);
        mHourNPicker.setLineSpacingMultiplier(multiplier);
        mMinuteNPicker.setLineSpacingMultiplier(multiplier);
        mSecondNPicker.setLineSpacingMultiplier(multiplier);
    }

    public void setMaxFlingVelocityCoefficient(int coefficient) {
        mYearNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mMonthNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mDayNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mHourNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mMinuteNPicker.setMaxFlingVelocityCoefficient(coefficient);
        mSecondNPicker.setMaxFlingVelocityCoefficient(coefficient);
    }

    public void setImeOptions(int imeOptions) {
        mYearNPicker.setImeOptions(imeOptions);
        mMonthNPicker.setImeOptions(imeOptions);
        mDayNPicker.setImeOptions(imeOptions);
        mHourNPicker.setImeOptions(imeOptions);
        mMinuteNPicker.setImeOptions(imeOptions);
        mSecondNPicker.setImeOptions(imeOptions);
    }

    public void setItemSpacing(int itemSpacing) {
        mYearNPicker.setItemSpacing(itemSpacing);
        mMonthNPicker.setItemSpacing(itemSpacing);
        mDayNPicker.setItemSpacing(itemSpacing);
        mHourNPicker.setItemSpacing(itemSpacing);
        mMinuteNPicker.setItemSpacing(itemSpacing);
        mSecondNPicker.setItemSpacing(itemSpacing);
    }
}