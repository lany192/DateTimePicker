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
 * 年月日时
 * custom year/month/day/hour picker
 */
public class YMDHPicker extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    private final String TAG = getClass().getSimpleName();
    private EditText mHourEditText;
    private EditText mDayEditText;
    private EditText mMonthEditText;
    private EditText mYearEditText;

    private NumberPicker mHourNPicker;
    private NumberPicker mDayNPicker;
    private NumberPicker mMonthNPicker;
    private NumberPicker mYearNPicker;
    private Locale mCurrentLocale;
    private OnDateChangedListener mOnDateChangedListener;
    private String[] mShortMonths;
    private int mNumberOfMonths;

    private Calendar mTempDate;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private Calendar mCurrentDate;

    private boolean mIsEnabled = true;

    public YMDHPicker(Context context) {
        super(context);
        init(null);
    }

    public YMDHPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public YMDHPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.picker_ymdh, this);
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
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateInputState();
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                // take care of wrapping of days and months to update greater
                // fields
                if (picker == mDayNPicker) {
                    int maxDayOfMonth = mTempDate
                            .getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
                    }
                } else if (picker == mMonthNPicker) {
                    if (oldVal == 11 && newVal == 0) {
                        mTempDate.add(Calendar.MONTH, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        mTempDate.add(Calendar.MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.MONTH, newVal - oldVal);
                    }
                } else if (picker == mYearNPicker) {
                    mTempDate.set(Calendar.YEAR, newVal);
                } else if (picker == mHourNPicker) {
                    mTempDate.set(Calendar.HOUR_OF_DAY, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                // now set the date to the adjusted one
                setDate(mTempDate.get(Calendar.YEAR),
                        mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH),
                        mTempDate.get(Calendar.HOUR_OF_DAY));
                updateNPickers();
                notifyDateChanged();
            }
        };

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
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH),
                mCurrentDate.get(Calendar.DAY_OF_MONTH),
                mCurrentDate.get(Calendar.HOUR_OF_DAY), null);

        // re-order the number NPickers to match the current date format
        reorderNPickers();

        // If not explicitly specified this view is important for accessibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mOnDateChangedListener = onDateChangedListener;
    }

    public void setSelectionDivider(Drawable selectionDivider) {
        mDayNPicker.setSelectionDivider(selectionDivider);
        mMonthNPicker.setSelectionDivider(selectionDivider);
        mYearNPicker.setSelectionDivider(selectionDivider);
        mHourNPicker.setSelectionDivider(selectionDivider);
    }

    public void setSelectionDividerHeight(int selectionDividerHeight) {
        mDayNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mMonthNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mYearNPicker.setSelectionDividerHeight(selectionDividerHeight);
        mHourNPicker.setSelectionDividerHeight(selectionDividerHeight);
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

        final int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_SHOW_YEAR;
        String selectedDateUtterance = DateUtils.formatDateTime(getContext(),
                mCurrentDate.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(YMDHPicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(YMDHPicker.class.getName());
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
            mShortMonths[i] = DateUtils.getMonthString(Calendar.JANUARY + i,
                    DateUtils.LENGTH_MEDIUM);
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

    public void updateDate(int year, int month, int dayOfMonth, int hourOfDay) {
        if (!isNewDate(year, month, dayOfMonth, hourOfDay)) {
            return;
        }
        setDate(year, month, dayOfMonth, hourOfDay);
        updateNPickers();
        notifyDateChanged();
    }

    @Override
    protected void dispatchRestoreInstanceState(
            SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(),
                getDayOfMonth(), getHourOfDay());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay, ss.mHour);
        updateNPickers();
    }

    public void init(int year, int monthOfYear, int dayOfMonth, int hourOfDay, OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth, hourOfDay);
        updateNPickers();
        mOnDateChangedListener = onDateChangedListener;
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

    private boolean isNewDate(int year, int month, int dayOfMonth, int hourOfDay) {
        return (mCurrentDate.get(Calendar.YEAR) != year
                || mCurrentDate.get(Calendar.MONTH) != dayOfMonth
                || mCurrentDate.get(Calendar.DAY_OF_MONTH) != month || mCurrentDate
                .get(Calendar.HOUR_OF_DAY) != hourOfDay);
    }

    private void setDate(int year, int month, int dayOfMonth, int hourOfDay) {
        mCurrentDate.set(year, month, dayOfMonth, hourOfDay, 0);
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

        // set the NPicker values
        mYearNPicker.setValue(mCurrentDate.get(Calendar.YEAR));
        mMonthNPicker.setValue(mCurrentDate.get(Calendar.MONTH));
        mDayNPicker.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
        mHourNPicker.setValue(mCurrentDate.get(Calendar.HOUR_OF_DAY));
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

    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(),
                    getDayOfMonth(), getHourOfDay());
        }
    }

    /**
     * Sets the IME options for a NPicker based on its ordering.
     *
     * @param NPicker      The NPicker.
     * @param NPickerCount The total NPicker count.
     * @param NPickerIndex The index of the given NPicker.
     */
    private void setImeOptions(NumberPicker NPicker, int NPickerCount,
                               int NPickerIndex) {
        final int imeOptions;
        if (NPickerIndex < NPickerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        TextView input = (TextView) NPicker
                .findViewById(R.id.number_picker_edit_text);
        input.setImeOptions(imeOptions);
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
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
            }
        }
    }

    public interface OnDateChangedListener {
        void onDateChanged(YMDHPicker view, int year, int monthOfYear, int dayOfMonth, int hourOfDay);
    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mYear;
        private final int mMonth;
        private final int mDay;
        private final int mHour;

        /**
         * Constructor called from {@link YMDHPicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day,
                           int hour) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
            mHour = hour;
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
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
            dest.writeInt(mHour);
        }
    }
}