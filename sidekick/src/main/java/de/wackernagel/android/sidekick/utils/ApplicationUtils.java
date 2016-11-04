package de.wackernagel.android.sidekick.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ApplicationUtils {

    private ApplicationUtils() {
    }

    /**
     * @param context of application
     * @return VersionName from manifest or null
     */
    @Nullable
    public static String getVersionName( @NonNull final Context context ) {
        try {
            final PackageInfo pInfo = context.getPackageManager().getPackageInfo( context.getPackageName(), 0 );
            return pInfo != null ? pInfo.versionName : null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * @param context of application
     * @return VersionCode from manifest or 0
     */
    public static int getVersionCode( @NonNull final Context context ) {
        try {
            final PackageInfo pInfo = context.getPackageManager().getPackageInfo( context.getPackageName(), 0 );
            return pInfo != null ? pInfo.versionCode : 0;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

}