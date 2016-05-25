package de.wackernagel.android.sidekick.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Locale;

public class LogUtils {

    public static String toString( @Nullable final Bundle bundle ) {
        final StringBuilder builder = new StringBuilder();
        builder.append( "Bundle:\n" );
        if( bundle != null && !bundle.isEmpty() ) {
            builder.append(String.format(Locale.getDefault(), "%-10s%s", "key", "value"));
            for( String key : bundle.keySet() ) {
                builder.append( String.format( Locale.getDefault(), "\n%-10s%s", key, bundle.get( key ) ) );
            }
        }
        return builder.toString();
    }

    public static String toString( @Nullable final Intent intent ) {
        final StringBuilder builder = new StringBuilder();
        builder.append( "Intent:\n" );
        if( intent != null ) {
            builder.append( "Action=" ).append( intent.getAction() ).append("\n");
            builder.append( "Data=" ).append( intent.getData() ).append( "\n" );
            builder.append( toString( intent.getExtras() ) );
        }
        return builder.toString();
    }

}