package com.github.lany192.picker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

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

    protected void setAccessibilityDescriptionEnabled(boolean enabled, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setAccessibilityDescriptionEnabled(enabled);
        }
    }

    protected void setDividerColor(@ColorInt int color, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerColor(color);
        }
    }

    protected void setDividerColorResource(@ColorRes int colorId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerColor(ContextCompat.getColor(getContext(), colorId));
        }
    }

    protected void setDividerDistance(int distance, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerDistance(distance);
        }
    }

    protected void setDividerDistanceResource(@DimenRes int dimenId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerDistanceResource(dimenId);
        }
    }

    protected void setDividerType(@NumberPicker.DividerType int dividerType, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerType(dividerType);
        }
    }

    protected void setDividerThickness(int thickness, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerThickness(thickness);
        }
    }

    protected void setDividerThicknessResource(@DimenRes int dimenId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setDividerThicknessResource(dimenId);
        }
    }

    protected void setOrder(@NumberPicker.Order int order, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setOrder(order);
        }
    }

    protected void setOrientation(@NumberPicker.Orientation int orientation, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setOrientation(orientation);
        }
    }

    protected void setWheelItemCount(int count, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setWheelItemCount(count);
        }
    }

    protected void setFormatter(final String formatter, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setFormatter(formatter);
        }
    }

    protected void setFormatter(@StringRes int stringId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setFormatter(getResources().getString(stringId));
        }
    }

    protected void setFadingEdgeEnabled(boolean fadingEdgeEnabled, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setFadingEdgeEnabled(fadingEdgeEnabled);
        }
    }

    protected void setFadingEdgeStrength(float strength, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setFadingEdgeStrength(strength);
        }
    }

    protected void setScrollerEnabled(boolean scrollerEnabled, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setScrollerEnabled(scrollerEnabled);
        }
    }

    protected void setSelectedTextAlign(@NumberPicker.Align int align, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextAlign(align);
        }
    }

    protected void setSelectedTextColor(@ColorInt int color, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextColor(color);
        }
    }

    protected void setSelectedTextColorResource(@ColorRes int colorId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextColorResource(colorId);
        }
    }

    protected void setSelectedTextSize(float textSize, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextSize(textSize);
        }
    }

    protected void setSelectedTextSize(@DimenRes int dimenId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextSize(getResources().getDimension(dimenId));
        }
    }

    protected void setSelectedTextStrikeThru(boolean strikeThruText, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextStrikeThru(strikeThruText);
        }
    }

    protected void setSelectedTextUnderline(boolean underlineText, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTextUnderline(underlineText);
        }
    }

    protected void setSelectedTypeface(Typeface typeface, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTypeface(typeface);
        }
    }

    protected void setSelectedTypeface(String string, int style, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTypeface(string, style);
        }
    }

    protected void setSelectedTypeface(String string, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTypeface(string, Typeface.NORMAL);
        }
    }

    protected void setSelectedTypeface(@StringRes int stringId, int style, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTypeface(getResources().getString(stringId), style);
        }
    }

    protected void setSelectedTypeface(@StringRes int stringId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setSelectedTypeface(stringId, Typeface.NORMAL);
        }
    }

    protected void setTextAlign(@NumberPicker.Align int align, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextAlign(align);
        }
    }

    protected void setTextColor(@ColorInt int color, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextColor(color);
        }
    }

    protected void setTextColorResource(@ColorRes int colorId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextColorResource(colorId);
        }
    }

    protected void setTextSize(float textSize, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextSize(textSize);
        }
    }

    protected void setTextSize(@DimenRes int dimenId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextSize(dimenId);
        }
    }

    protected void setTextStrikeThru(boolean strikeThruText, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextStrikeThru(strikeThruText);
        }
    }

    protected void setTextUnderline(boolean underlineText, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTextUnderline(underlineText);
        }
    }

    protected void setTypeface(Typeface typeface, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTypeface(typeface);
        }
    }

    protected void setTypeface(String string, int style, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTypeface(string, style);
        }
    }

    protected void setTypeface(String string, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTypeface(string);
        }
    }

    protected void setTypeface(@StringRes int stringId, int style, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTypeface(stringId, style);
        }
    }

    protected void setTypeface(@StringRes int stringId, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setTypeface(stringId);
        }
    }

    protected void setLineSpacingMultiplier(float multiplier, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setLineSpacingMultiplier(multiplier);
        }
    }

    protected void setMaxFlingVelocityCoefficient(int coefficient, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setMaxFlingVelocityCoefficient(coefficient);
        }
    }

    protected void setImeOptions(int imeOptions, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setImeOptions(imeOptions);
        }
    }

    protected void setItemSpacing(int itemSpacing, NumberPicker... pickers) {
        for (NumberPicker picker : pickers) {
            picker.setItemSpacing(itemSpacing);
        }
    }
}
