package de.wackernagel.android.sidekick.text;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomTypefaceSpan extends MetricAffectingSpan {
    private Typeface typeface;

    public CustomTypefaceSpan( @NonNull final Typeface typeface ) {
        this.typeface = typeface;
    }

    @NonNull
    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface( @NonNull final Typeface typeface) {
        this.typeface = typeface;
    }

    @Override
    public void updateDrawState(final TextPaint drawState) {
        apply(typeface, drawState);
    }

    @Override
    public void updateMeasureState(final TextPaint paint) {
        apply(typeface, paint);
    }

    private static void apply(final Typeface typeface, final Paint paint) {
        final Typeface oldTypeface = paint.getTypeface();
        final int oldStyle = oldTypeface != null ? oldTypeface.getStyle() : 0;
        final int fakeStyle = oldStyle & ~typeface.getStyle();

        if ((fakeStyle & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fakeStyle & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(typeface);
    }
}