package de.wackernagel.android.sidekick.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DeviceUtils {

    public static int getWidthAsPx( @NonNull final Activity activity ) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static int getHeightAsPx( @NonNull final Activity activity ) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static int dpToPx( float dp, @NonNull final Resources res ) {
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics() ) );
    }

    public static float pxToDp( int px, @NonNull final Resources res ) {
        return (float) px / res.getDisplayMetrics().density;
    }
}
