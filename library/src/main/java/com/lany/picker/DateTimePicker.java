package com.lany.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lany.numberpicker.NumberPicker;
import com.lany.picker.utils.ArraysUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 年月日时分秒
 */
public class DateTimePicker extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    private final String TAG = getClass().getSimpleName();

    private EditText mMinuteEditText;
    private EditText mSecondEditText;
    private EditText mHourEditText;
    private EditText mDayEditText;
    private EditText mMonthEditText;
    private EditText mYearEditText;

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
        LayoutInflater.from(getContext()).inflate(R.layout.picker_date_time, this);
        setCurrentLocale(Locale.getDefault());
        int startYear = DEFAULT_START_YEAR;
        int endYear = DEFAULT_END_YEAR;
        String minDate = "";
        String maxDate = "";
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.YMDHPicker);
            startYear = typedArray.getInt(R.styleable.YMDHPicker_startYear, DEFAULT_START_YEAR);
            endYear = typedArray.getInt(R.styleable.YMDHPicker_endYear, DEFAULT_END_YEAR);
            minDate = typedArray.getString(R.styleable.YMDHPicker_minDate);
            maxDate = typedArray.getString(R.styleable.YMDHPicker_maxDate);
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
        // minute
        mMinuteNPicker = findViewById(R.id.minute);
        mMinuteNPicker.setMinValue(0);
        mMinuteNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mMinuteNPicker.setOnValueChangedListener(onChangeListener);
        mMinuteEditText = mMinuteNPicker.findViewById(R.id.number_picker_edit_text);
        mMinuteEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        // second
        mSecondNPicker = findViewById(R.id.second);
        mSecondNPicker.setMinValue(0);
        mSecondNPicker.setMaxValue(59);
        mMinuteNPicker.setOnLongPressUpdateInterval(100);
        mMinuteNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mSecondNPicker.setOnValueChangedListener(onChangeListener);
        mSecondEditText = mSecondNPicker.findViewById(R.id.number_picker_edit_text);
        mSecondEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        // hour
        mHourNPicker = findViewById(R.id.hour);
        mHourNPicker.setOnLongPressUpdateInterval(100);
        mHourNPicker.setOnValueChangedListener(onChangeListener);
        mHourEditText = mHourNPicker.findViewById(R.id.number_picker_edit_text);
        // day
        mDayNPicker = findViewById(R.id.day);
        mDayNPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        mDayNPicker.setOnLongPressUpdateInterval(100);
        mDayNPicker.setOnValueChangedListener(onChangeListener);
        mDayEditText = mDayNPicker.findViewById(R.id.number_picker_edit_text);
        // month
        mMonthNPicker = findViewById(R.id.month);
        mMonthNPicker.setMinValue(0);
        mMonthNPicker.setMaxValue(mNumberOfMonths - 1);
        mMonthNPicker.setDisplayedValues(mShortMonths);
        mMonthNPicker.setOnLongPressUpdateInterval(200);
        mMonthNPicker.setOnValueChangedListener(onChangeListener);
        mMonthEditText = mMonthNPicker.findViewById(R.id.number_picker_edit_text);

        // year
        mYearNPicker = findViewById(R.id.year);
        mYearNPicker.setOnLongPressUpdateInterval(100);
        mYearNPicker.setOnValueChangedListener(onChangeListener);
        mYearEditText = mYearNPicker.findViewById(R.id.number_picker_edit_text);

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

    public void setSelectionDivider(Drawable selectionDivider) {
        mDayNPicker.setSelectionDivider(selectionDivider);
        mMonthNPicker.setSelectionDivider(selectionDivider);
        mYearNPicker.setSelectionDivider(selectionDivider);
        mHourNPicker.setSelectionDivider(selectionDivider);
        mMinuteNPicker.setSelectionDivider(selectionDivider);
        mSecondNPicker.setSelectionDivider(selectionDivider);
    }

    public void setSelectionDividerHeight(int selectionDividerHeight) {
        mDayNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mMonthNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mYearNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mHourNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mMinuteNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mSecondNPicker.setSelectionDividerHeight(selectionDividerHeight);
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
        char[] order = DateFormat.getDateFormatOrder(getContext());
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
            outDate.setTime(new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
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
        String[] displayedValues = ArraysUtils.copyOfRange(mShortMonths, mMonthNPicker.getMinValue(), mMonthNPicker.getMaxValue() + 1);
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
     * @param NPicker      The NPicker.
     * @param NPickerCount The total NPicker count.
     * @param NPickerIndex The index of the given NPicker.
     */
    private void setImeOptions(NumberPicker NPicker, int NPickerCount, int NPickerIndex) {
        final int imeOptions;
        if (NPickerIndex < NPickerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        TextView input = NPicker.findViewById(R.id.number_picker_edit_text);
        input.setImeOptions(imeOptions);
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            if (inputMethodManager.isActive(mYearEditText)) {
                mYearEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mMonthEditText)) {
                mMonthEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mDayEditText)) {
                mDayEditText.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            } else if (inputMethodManager.isActive(mHourEditText)) {
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
}