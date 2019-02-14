package com.localz.spotz.sdk.app.widgets;

import android.content.Context;
import android.graphics.Typeface;

public enum CustomFont {
    roboto_thin("fonts/Roboto-Thin.ttf"),
    roboto_regular("fonts/Roboto-Regular.ttf");

    private final String path;
    private Typeface typeface;

    CustomFont(String path) {
        this.path = path;
    }

    public Typeface getTypeface(Context context) {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), path);
        }

        return typeface;
    }
}