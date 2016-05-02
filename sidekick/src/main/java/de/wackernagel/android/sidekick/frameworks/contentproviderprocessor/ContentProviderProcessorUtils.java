package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ContentProviderProcessorUtils {

	public static Uri appendLimit( Uri uri, String limit ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}

		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_LIMIT, limit )
				.build();
	}

	public static Uri appendGroupBy( Uri uri, String groupBy ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}
		
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_GROUP_BY, groupBy )
				.build();
	}
	
	public static Uri appendHaving( Uri uri, String having ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}
		
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_HAVING, having )
				.build();
	}

	public static boolean existColumn( SQLiteDatabase db, String table, String newColumn ) {
		final Cursor cursor = db.rawQuery( "PRAGMA table_info(" + table + ")", null);
		if( cursor != null && cursor.moveToFirst() ) {
			do {
				String existingColumn = cursor.getString( 1 );
				if( newColumn.equals( existingColumn ) ) {
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
