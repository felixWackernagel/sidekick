package de.wackernagel.android.sidekick.utils;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Locale;

import de.wackernagel.android.sidekick.compats.CursorCompat;

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
            builder.append( toString(intent.getExtras()) );
        }
        return builder.toString();
    }

    public static String toString( @Nullable final Cursor cursor, final boolean close, final boolean skipNull ) {
        final StringBuilder builder = new StringBuilder();
        if( cursor == null ) {
            builder.append( "Cursor is null." );
        } else if( !cursor.moveToFirst() ) {
            builder.append( "Cursor is empty." );
        } else {
            final int rowCount = cursor.getCount();
            final int columnCount = cursor.getColumnCount();
            int columnType;
            for( int row = 0; row < rowCount; row++ ) {
                cursor.moveToPosition( row );
                for( int column = 0; column < columnCount; column++ ) {
                    columnType = CursorCompat.getType( cursor, column );
                    if( skipNull && columnType == Cursor.FIELD_TYPE_NULL ) {
                        continue;
                    }
                    if( !(row == 0 && column == 0 ) ) {
                        builder.append( "\n" );
                    }
                    builder.append( "[" ).append( row ).append( ":" ).append( column ).append( "] (" ).append( cursor.getColumnName( column ) ).append( "/" );
                    switch ( columnType ) {
                        case Cursor.FIELD_TYPE_FLOAT:
                            builder.append( "float) " ).append(cursor.getFloat(column));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            builder.append( "integer) " ).append(cursor.getInt(column));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            builder.append( "string) " ).append(cursor.getString(column));
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                            builder.append("null)");
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            builder.append( "blob) " ).append( new String( cursor.getBlob( column) ) );
                            break;
                        default:
                            builder.append( "unknown)" );
                            break;
                    }
                }
            }
        }

        if( cursor != null && close ) {
            cursor.close();
        }
        return builder.toString();
    }

}