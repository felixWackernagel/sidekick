package de.wackernagel.android.sidekick.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import de.wackernagel.android.sidekick.R;

public class DeviceUtils {

    /**
     * @param context of device
     * @return screen width in pixels
     */
    public static int getWidth( @NonNull final Context context ) {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( displaymetrics );
        return displaymetrics.widthPixels;
    }

    /**
     * @param context of device
     * @return screen height in pixels
     */
    public static int getHeight( @NonNull final Context context ) {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( displaymetrics );
        return displaymetrics.heightPixels;
    }

    /**
     * @param dp to convert
     * @param context of device
     * @return calculated pixels from dips
     */
    public static int dpToPx( float dp, @NonNull final Context context ) {
        return Math.round( TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()) );
    }

    /**
     * @param px to convert
     * @param context of device
     * @return calculated dips from pixels
     */
    public static float pxToDp( int px, @NonNull final Context context ) {
        return (float) px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * @param context of device
     * @return true if device has sw-600dp otherwise false
     */
    public static boolean isTablet( @NonNull final Context context ) {
        return context.getResources().getBoolean( R.bool.sidekick_tablet );
    }

    /**
     * @param context of device
     * @return true if device is in landscape mode otherwise false for portrait mode
     */
    public static boolean isLandscape( @NonNull final Context context ) {
        return context.getResources().getBoolean( R.bool.sidekick_landscape );
    }

}
