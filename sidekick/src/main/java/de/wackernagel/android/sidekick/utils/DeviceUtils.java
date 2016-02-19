package de.wackernagel.android.sidekick.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import de.wackernagel.android.sidekick.R;

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

    public static int dpToPx( float dp, @NonNull final Context context ) {
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) );
    }

    public static float pxToDp( int px, @NonNull final Context context ) {
        return (float) px / context.getResources().getDisplayMetrics().density;
    }

    public static boolean isTablet( @NonNull final Context context ) {
        return context.getResources().getBoolean( R.bool.sidekick_tablet );
    }

    public static boolean isLandscape( @NonNull final Context context ) {
        return context.getResources().getBoolean( R.bool.sidekick_landscape );
    }
}
