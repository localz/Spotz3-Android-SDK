package com.localz.spotz.sdk.app.widgets;

import com.localz.spotz.sdk.app.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomFontTextView extends TextView {
    public CustomFontTextView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFont, defStyle, 0);
        String font = a.getString(R.styleable.CustomFont_customFont);
        if (TextUtils.isEmpty(font)) {
            setTypeface(CustomFont.roboto_regular.getTypeface(context));
        }
        else {
            setTypeface(CustomFont.valueOf(font).getTypeface(context));
        }
    }
}
