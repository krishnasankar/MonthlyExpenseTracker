package com.expensetracker.monthly.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

/**
 * A TextInputEditText that intercepts the back key before the IME consumes it,
 * allowing dialogs/screens to handle back press directly when the keyboard is visible.
 */
public class PreImeTextInputEditText extends TextInputEditText {

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    private OnBackPressedListener onBackPressedListener;

    public PreImeTextInputEditText(@NonNull Context context) {
        super(context);
    }

    public PreImeTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PreImeTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        this.onBackPressedListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (onBackPressedListener != null && onBackPressedListener.onBackPressed()) {
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
