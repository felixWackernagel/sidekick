package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class ContentProviderProcessorUtils {

	public static Uri appendLimit( @NonNull final Uri uri, @NonNull final String limit ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}

		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_LIMIT, limit )
				.build();
	}

	public static Uri appendGroupBy( @NonNull final Uri uri, @NonNull final String groupBy ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}
		
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_GROUP_BY, groupBy )
				.build();
	}
	
	public static Uri appendHaving( @NonNull final Uri uri, @NonNull final String having ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}
		
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_HAVING, having )
				.build();
	}

    public static String[] joinProjection( @NonNull final String table, @NonNull final String[] projection ) {
        final int size = projection.length;
        final String[] joinProjection = new String[ size ];
        for( int index = 0; index < size; index++ ) {
            joinProjection[ index ] = table.concat( "." ).concat( projection[ index ] );
        }
        return joinProjection;
    }

	public static boolean existColumn( @NonNull final SQLiteDatabase db, @NonNull final String table,@NonNull final  String columnName ) {
		final Cursor cursor = db.rawQuery( "PRAGMA table_info(" + table + ")", null);
		if( cursor != null && cursor.moveToFirst() ) {
			do {
				String existingColumn = cursor.getString( 1 );
				if( columnName.equals( existingColumn ) ) {
					cursor.close();
					return true;
				}
			} while (cursor.moveToNext());
		}
		if( cursor != null ) {
			cursor.close();
		}
		return false;
	}

}
