package de.wackernagel.android.sidekick.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

public class NetworkUtils {

    public static boolean isOnline( @NonNull final Context context ) {
        final ConnectivityManager connectionManager =  (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        final NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return ( networkInfo != null && networkInfo.isConnectedOrConnecting() );
    }

    public static boolean isOnlineWifi( @NonNull final Context context ) {
        final ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE );
        final NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI );
    }

    public static boolean isOnlineMobile( @NonNull final Context context ) {
        final ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE );
        final NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE );
    }

}
