package com.expensetracker.monthly.ui.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.google.android.material.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

/**
 * An AutoCompleteTextView that adheres to the exact styling, measurement, padding,
 * text appearance, and TextInputLayout integration of TextInputEditText.
 */
public class TextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    private final Rect parentRect = new Rect();
    private boolean textInputLayoutFocusedRectEnabled = true;

    public TextInputAutoCompleteTextView(@NonNull Context context) {
        this(context, null);
    }

    public TextInputAutoCompleteTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public TextInputAutoCompleteTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, 0), attrs, defStyleAttr);
    }

    @Nullable
    @Override
    public CharSequence getHint() {
        TextInputLayout textInputLayout = getTextInputLayout();
        if (textInputLayout != null && textInputLayout.isProvidingHint()) {
            return textInputLayout.getHint();
        }
        return super.getHint();
    }

    @Nullable
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null && outAttrs.hintText == null) {
            TextInputLayout textInputLayout = getTextInputLayout();
            if (textInputLayout != null) {
                outAttrs.hintText = textInputLayout.getHint();
            }
        }
        return ic;
    }

    @Nullable
    private TextInputLayout getTextInputLayout() {
        ViewParent parent = getParent();
        while (parent instanceof View) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    public void getFocusedRect(@Nullable Rect r) {
        super.getFocusedRect(r);
        TextInputLayout textInputLayout = getTextInputLayout();
        if (shouldUseTextInputLayoutFocusedRect(textInputLayout) && r != null) {
            textInputLayout.getFocusedRect(parentRect);
            r.bottom = parentRect.bottom;
        }
    }

    @Override
    public boolean getGlobalVisibleRect(@Nullable Rect r, @Nullable Point globalOffset) {
        TextInputLayout textInputLayout = getTextInputLayout();
        boolean useLayout = shouldUseTextInputLayoutFocusedRect(textInputLayout);
        return useLayout && textInputLayout != null
                ? textInputLayout.getGlobalVisibleRect(r, globalOffset)
                : super.getGlobalVisibleRect(r, globalOffset);
    }

    @Override
    public boolean requestRectangleOnScreen(@Nullable Rect rectangle) {
        TextInputLayout textInputLayout = getTextInputLayout();
        boolean useLayout = shouldUseTextInputLayoutFocusedRect(textInputLayout);
        if (useLayout && textInputLayout != null && rectangle != null) {
            int bottomOffset = textInputLayout.getHeight() - getHeight();
            parentRect.set(
                    rectangle.left,
                    rectangle.top,
                    rectangle.right,
                    rectangle.bottom + bottomOffset
            );
            return super.requestRectangleOnScreen(parentRect);
        }
        return super.requestRectangleOnScreen(rectangle);
    }

    private boolean shouldUseTextInputLayoutFocusedRect(@Nullable TextInputLayout textInputLayout) {
        return textInputLayout != null && textInputLayoutFocusedRectEnabled;
    }

    public void setTextInputLayoutFocusedRectEnabled(boolean enabled) {
        this.textInputLayoutFocusedRectEnabled = enabled;
    }

    public boolean isTextInputLayoutFocusedRectEnabled() {
        return textInputLayoutFocusedRectEnabled;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT < 23) {
            TextInputLayout textInputLayout = getTextInputLayout();
            if (textInputLayout != null) {
                CharSequence text = getText();
                CharSequence hint = textInputLayout.getHint();
                boolean hasText = !TextUtils.isEmpty(text);
                boolean hasHint = !TextUtils.isEmpty(hint);
                if (hasText && hasHint) {
                    info.setText(text + ", " + hint);
                } else if (hasHint) {
                    info.setText(hint);
                }
            }
        }
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    private OnBackPressedListener onBackPressedListener;

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
