package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.util.List;

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

	public static Uri appendDistinct( @NonNull final Uri uri ) {
		if( uri == null ) {
			throw new IllegalArgumentException( "Uri can't be null" );
		}

		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_DISTINCT, "distinct" )
				.build();
	}

    /**
     * @param table
     * @param projection
     * @return a array in which each projection element has the table as prefix like "table.projectionElement"
     */
    public static String[] joinProjection( @NonNull final String table, @NonNull final String[] projection ) {
        final int size = projection.length;
        final String[] joinProjection = new String[ size ];
        for( int index = 0; index < size; index++ ) {
            joinProjection[ index ] = table.concat( "." ).concat( projection[ index ] );
        }
        return joinProjection;
    }

    /**
     * @param tablesWithProjection
     * @return a single array of all projections with the table name as prefix
     */
	public static String[] joinProjections( @NonNull final List<Pair<String, String[]>> tablesWithProjection ) {
		final StringBuilder result = new StringBuilder();
        final String divider = "|";
        final int tableCount = tablesWithProjection.size();

        String table;
        String[] projection;
        int columnCount;

		for( int i = 0; i < tableCount; i++ ) {

			table = tablesWithProjection.get(i).first;
			projection = tablesWithProjection.get(i).second;
			columnCount = projection.length;

            for( int j = 0; j < columnCount; j++ ) {
                if( !(i == 0 && j == 0) ) {
                    result.append( divider );
                }
				result.append( table ).append( "." ).append( projection[j]);
			}
		}
		return result.toString().split( divider );
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

	/**
	 * If the projection or column are empty it returns immediate -1.
	 *
	 * @param projection of a query
	 * @param column to find its index in project
	 * @return index of column in projection or -1
	 */
	public static int getColumnIndex( @NonNull final String[] projection, @NonNull final String column ) {
		if( projection.length == 0 || TextUtils.isEmpty( column ) ) {
			return -1;
		}

		final int size = projection.length;
		for( int index = 0; index < size; index++ ) {
			if( projection[index].equalsIgnoreCase( column ) ) {
				return index;
			}
		}
		return -1;
	}

}
