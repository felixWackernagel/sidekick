package de.wackernagel.android.sidekick.utils;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class TintUtils {

    /**
     * Tint all compound drawables with the given color.
     * Do nothing when textView is null.
     *
     * @param textView which drawables are tinted
     * @param color of tint
     */
    public static void tintCompoundDrawables( @Nullable final TextView textView, @ColorInt final int color) {
        if( textView != null ) {
            final Drawable[] tinted = tintDrawables( textView.getCompoundDrawables(), color );
            textView.setCompoundDrawables( tinted[0], tinted[1], tinted[2], tinted[3] );
        }
    }

    /**
     * Tint all drawables with the given color.
     * The result array can contain null values.
     *
     * @param drawables which are tinted
     * @param color of tint
     */
    @NonNull
    private static Drawable[] tintDrawables( @NonNull final Drawable[] drawables, @ColorInt final int color ) {
        int size = drawables.length;
        Drawable[] tinted = new Drawable[size];
        for( int index = 0; index < size; index++ ) {
           tinted[index] = tint( drawables[index], color );
        }
        return tinted;
    }

    /**
     * Tint the drawable with the given color.
     * Do nothing when drawable is null.
     *
     * @param drawable which are tinted
     * @param color of tint
     */
    @Nullable
    public static Drawable tint( @Nullable final Drawable drawable, @ColorInt final int color ) {
        if( drawable == null ) {
            return null;
        }

        final Drawable tinted = drawable.mutate();
        tinted.setColorFilter( new PorterDuffColorFilter( color, PorterDuff.Mode.SRC_IN ) );
        return tinted;
    }

    /**
     * Clear a previous applied tint.
     *
     * @param drawable of which the tint is cleared
     */
    public static void clearTint( @Nullable final Drawable drawable ) {
        if( drawable != null ) {
            drawable.setColorFilter( null );
        }
    }

}
