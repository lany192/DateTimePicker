package com.github.lany192.picker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class ItemEditText extends EditText {

    public ItemEditText(Context context) {
        super(context);
    }

    public ItemEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            clearFocus();
        }
    }
}