package com.github.lany192.picker;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

public abstract class BasePicker extends FrameLayout {
    protected final String TAG = getClass().getSimpleName();
    /**
     * Constant for unspecified size.
     */
    protected final int SIZE_UNSPECIFIED = -1;

    public BasePicker(Context context) {
        super(context);
    }

    public BasePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }

}
