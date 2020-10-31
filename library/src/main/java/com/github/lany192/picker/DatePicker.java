package com.github.lany192.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
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
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lany192.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class DatePicker extends BasePicker {
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final boolean DEFAULT_NPickerS_SHOWN = true;
    private static final boolean DEFAULT_DAY_VIEW_SHOWN = true;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private final LinearLayout mNPickers;
    private final EditText mDayEditText;
    private final EditText mMonthEditText;
    private final EditText mYearEditText;

    private final NumberPicker dayNumberPicker;
    private final NumberPicker monthNumberPicker;
    private final NumberPicker yearNumberPicker;
    private Locale mCurrentLocale;
    private OnDateChangedListener mOnDateChangedListener;
    private String[] mShortMonths;
    private int mNumberOfMonths;

    private Calendar mTempDate;

    private Calendar mMinDate;

    private Calendar mMaxDate;

    private Calendar mCurrentDate;

    private boolean mIsEnabled = DEFAULT_ENABLED_STATE;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // initialization based on locale
        setCurrentLocale(Locale.getDefault());

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyle, 0);
        boolean spinnersShown = typedArray.getBoolean(R.styleable.DatePicker_picker_spinnersShown, DEFAULT_NPickerS_SHOWN);
        boolean dayViewShown = typedArray.getBoolean(R.styleable.DatePicker_picker_dayViewShown, DEFAULT_DAY_VIEW_SHOWN);

        int startYear = typedArray.getInt(R.styleable.DatePicker_picker_startYear, DEFAULT_START_YEAR);
        int endYear = typedArray.getInt(R.styleable.DatePicker_picker_endYear, DEFAULT_END_YEAR);
        String minDate = typedArray.getString(R.styleable.DatePicker_picker_minDate);
        String maxDate = typedArray.getString(R.styleable.DatePicker_picker_maxDate);
        int layoutResourceId = typedArray.getResourceId(R.styleable.DatePicker_picker_picker_internalLayout, R.layout.picker_date);


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
        Drawable virtualButtonPressedDrawable = typedArray.getDrawable(R.styleable.DateTimePicker_picker_virtualButtonPressedDrawable);
        int selectionTextSize = (int) typedArray.getDimension(R.styleable.DateTimePicker_picker_selectionTextSize, SIZE_UNSPECIFIED);
        int selectionTextColor = typedArray.getColor(R.styleable.DateTimePicker_picker_selectionTextColor, Color.BLACK);


        typedArray.recycle();

        LayoutInflater.from(getContext()).inflate(layoutResourceId, this);

        NumberPicker.OnValueChangeListener onChangeListener = new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateInputState();
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                // take care of wrapping of days and months to update greater
                // fields
                if (picker == dayNumberPicker) {
                    int maxDayOfMonth = mTempDate
                            .getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
                    }
                } else if (picker == monthNumberPicker) {
                    if (oldVal == 11 && newVal == 0) {
                        mTempDate.add(Calendar.MONTH, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        mTempDate.add(Calendar.MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.MONTH, newVal - oldVal);
                    }
                } else if (picker == yearNumberPicker) {
                    mTempDate.set(Calendar.YEAR, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                // now set the date to the adjusted one
                setDate(mTempDate.get(Calendar.YEAR),
                        mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH));
                updateNPickers();
                notifyDateChanged();
            }
        };

        mNPickers = findViewById(R.id.pickers);
        // day
        dayNumberPicker = findViewById(R.id.day);
        dayNumberPicker.setFormatter(NumberPicker.getTwoDigitFormatter());
        dayNumberPicker.setOnLongPressUpdateInterval(100);
        dayNumberPicker.setOnValueChangedListener(onChangeListener);
        mDayEditText = dayNumberPicker.findViewById(R.id.number_picker_edit_text);

        // show only what the user required but make sure we
        // show something and the NPickers have higher priority
        if (!spinnersShown && !dayViewShown) {
            setspinnersShown(true);
        } else {
            setspinnersShown(spinnersShown);
            setDayViewShown(dayViewShown);
        }


        // month
        monthNumberPicker = findViewById(R.id.month);
        monthNumberPicker.setMinValue(0);
        monthNumberPicker.setMaxValue(mNumberOfMonths - 1);
        monthNumberPicker.setDisplayedValues(mShortMonths);
        monthNumberPicker.setOnLongPressUpdateInterval(200);
        monthNumberPicker.setOnValueChangedListener(onChangeListener);
        mMonthEditText = monthNumberPicker.findViewById(R.id.number_picker_edit_text);

        // year
        yearNumberPicker = findViewById(R.id.year);
        yearNumberPicker.setOnLongPressUpdateInterval(100);
        yearNumberPicker.setOnValueChangedListener(onChangeListener);
        mYearEditText = yearNumberPicker.findViewById(R.id.number_picker_edit_text);

        // show only what the user required but make sure we
        // show something and the NPickers have higher priority
        if (!spinnersShown) {
            setspinnersShown(true);
        } else {
            setspinnersShown(spinnersShown);
        }

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
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH), mCurrentDate.get(Calendar.DAY_OF_MONTH));

        // re-order the number NPickers to match the current date format
        reorderNPickers();

        // If not explicitly specified this view is important for accessibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    public void setSelectionDivider(Drawable selectionDivider) {
        dayNumberPicker.setSelectionDivider(selectionDivider);
        monthNumberPicker.setSelectionDivider(selectionDivider);
        yearNumberPicker.setSelectionDivider(selectionDivider);
    }

    public void setSelectionDividerHeight(int selectionDividerHeight) {
        dayNumberPicker.setSelectionDividerHeight(selectionDividerHeight);
        monthNumberPicker.setSelectionDividerHeight(selectionDividerHeight);
        yearNumberPicker.setSelectionDividerHeight(selectionDividerHeight);
    }


    /**
     * Sets the minimal date supported by this {@link NumberPicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param minDate The minimal supported date.
     */
    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        }
        updateNPickers();
    }


    /**
     * Sets the maximal date supported by this {@link DatePicker} in
     * milliseconds since January 1, 1970 00:00:00 in
     * {@link TimeZone#getDefault()} time zone.
     *
     * @param maxDate The maximal supported date.
     */
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
        dayNumberPicker.setEnabled(enabled);
        monthNumberPicker.setEnabled(enabled);
        yearNumberPicker.setEnabled(enabled);
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
        event.setClassName(DatePicker.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(DatePicker.class.getName());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    /**
     * Sets whether the {@link CalendarView} is shown.
     *
     * @param shown True if the calendar view is to be shown.
     */
    public void setDayViewShown(boolean shown) {
        dayNumberPicker.setVisibility(shown ? VISIBLE : GONE);
    }

    /**
     * Gets whether the NPickers are shown.
     *
     * @return True if the NPickers are shown.
     */
    public boolean getspinnersShown() {
        return mNPickers.isShown();
    }

    /**
     * Sets whether the NPickers are shown.
     *
     * @param shown True if the NPickers are to be shown.
     */
    public void setspinnersShown(boolean shown) {
        mNPickers.setVisibility(shown ? VISIBLE : GONE);
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

    /**
     * Gets a calendar for locale bootstrapped with the value of a given
     * calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale      The locale.
     */
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

    /**
     * Reorders the NPickers according to the date format that is explicitly set
     * by the user and if no such is set fall back to the current locale's
     * default format.
     */
    private void reorderNPickers() {
        mNPickers.removeAllViews();
        char[] order;
        try {
            order = DateFormat.getDateFormatOrder(getContext());
        } catch (IllegalArgumentException expected) {
            order = new char[0];
        }
        final int NPickerCount = order.length;
        for (int i = 0; i < NPickerCount; i++) {
            switch (order[i]) {
                case 'd':
                    mNPickers.addView(dayNumberPicker);
                    setImeOptions(dayNumberPicker, NPickerCount, i);
                    break;
                case 'M':
                    mNPickers.addView(monthNumberPicker);
                    setImeOptions(monthNumberPicker, NPickerCount, i);
                    break;
                case 'y':
                    mNPickers.addView(yearNumberPicker);
                    setImeOptions(yearNumberPicker, NPickerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Updates the current date.
     *
     * @param year       The year.
     * @param month      The month which is <strong>starting from zero</strong>.
     * @param dayOfMonth The day of the month.
     */
    public void updateDate(int year, int month, int dayOfMonth) {
        if (!isNewDate(year, month, dayOfMonth)) {
            return;
        }
        setDate(year, month, dayOfMonth);
        updateNPickers();
        notifyDateChanged();
    }

    // Override so we are in complete control of save / restore for this widget.
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateNPickers();
    }

    /**
     * Initialize the state. If the provided values designate an inconsistent
     * date the values are normalized before updating the NPickers.
     *
     * @param year        The initial year.
     * @param monthOfYear The initial month <strong>starting from zero</strong>.
     * @param dayOfMonth  The initial day of the month.
     */
    public void init(int year, int monthOfYear, int dayOfMonth) {
        setDate(year, monthOfYear, dayOfMonth);
        updateNPickers();
    }


    /**
     * Parses the given <code>date</code> and in case of success sets the result
     * to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
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

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (mCurrentDate.get(Calendar.YEAR) != year
                || mCurrentDate.get(Calendar.MONTH) != dayOfMonth
                || mCurrentDate.get(Calendar.DAY_OF_MONTH) != month);
    }

    private void setDate(int year, int month, int dayOfMonth) {
        mCurrentDate.set(year, month, dayOfMonth);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }

    private void updateNPickers() {
        // set the NPicker ranges respecting the min and max dates
        if (mCurrentDate.equals(mMinDate)) {
            dayNumberPicker.setMinValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            dayNumberPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            dayNumberPicker.setWrapSelectorWheel(false);
            monthNumberPicker.setDisplayedValues(null);
            monthNumberPicker.setMinValue(mCurrentDate.get(Calendar.MONTH));
            monthNumberPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.MONTH));
            monthNumberPicker.setWrapSelectorWheel(false);
        } else if (mCurrentDate.equals(mMaxDate)) {
            dayNumberPicker.setMinValue(mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            dayNumberPicker.setMaxValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            dayNumberPicker.setWrapSelectorWheel(false);
            monthNumberPicker.setDisplayedValues(null);
            monthNumberPicker.setMinValue(mCurrentDate.getActualMinimum(Calendar.MONTH));
            monthNumberPicker.setMaxValue(mCurrentDate.get(Calendar.MONTH));
            monthNumberPicker.setWrapSelectorWheel(false);
        } else {
            dayNumberPicker.setMinValue(1);
            dayNumberPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            dayNumberPicker.setWrapSelectorWheel(true);
            monthNumberPicker.setDisplayedValues(null);
            monthNumberPicker.setMinValue(0);
            monthNumberPicker.setMaxValue(11);
            monthNumberPicker.setWrapSelectorWheel(true);
        }

        // make sure the month names are a zero based array
        // with the months in the month NPicker
        String[] displayedValues = Arrays.copyOfRange(mShortMonths, monthNumberPicker.getMinValue(), monthNumberPicker.getMaxValue() + 1);
        monthNumberPicker.setDisplayedValues(displayedValues);

        // year NPicker range does not change based on the current date
        yearNumberPicker.setMinValue(mMinDate.get(Calendar.YEAR));
        yearNumberPicker.setMaxValue(mMaxDate.get(Calendar.YEAR));
        yearNumberPicker.setWrapSelectorWheel(false);

        // set the NPicker values
        yearNumberPicker.setValue(mCurrentDate.get(Calendar.YEAR));
        monthNumberPicker.setValue(mCurrentDate.get(Calendar.MONTH));
        dayNumberPicker.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
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
     * Notifies the listener, if such, for a change in the selected date.
     */
    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    /**
     * Sets the IME options for a NPicker based on its ordering.
     *
     * @param picker            The NPicker.
     * @param numberPickerCount The total NumberPicker count.
     * @param numberPickerIndex The index of the given NumberPicker.
     */
    private void setImeOptions(NumberPicker picker, int numberPickerCount, int numberPickerIndex) {
        final int imeOptions;
        if (numberPickerIndex < numberPickerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        TextView input = picker.findViewById(R.id.number_picker_edit_text);
        input.setImeOptions(imeOptions);
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
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
            }
        }
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mOnDateChangedListener = listener;
    }

    /**
     * The callback used to indicate the user changes\d the date.
     */
    public interface OnDateChangedListener {

        /**
         * Called upon a date change.
         *
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility with
         *                    {@link Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Creator<SavedState> CREATOR = new Creator<DatePicker.SavedState>() {

            public DatePicker.SavedState createFromParcel(Parcel in) {
                return new DatePicker.SavedState(in);
            }

            public DatePicker.SavedState[] newArray(int size) {
                return new DatePicker.SavedState[size];
            }
        };
        private final int mYear;
        private final int mMonth;
        private final int mDay;

        /**
         * Constructor called from {@link DatePicker#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
        }
    }
}
