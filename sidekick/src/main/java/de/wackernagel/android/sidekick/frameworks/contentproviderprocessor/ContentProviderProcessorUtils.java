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
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_LIMIT, limit )
				.build();
	}

	public static Uri appendGroupBy( @NonNull final Uri uri, @NonNull final String groupBy ) {
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_GROUP_BY, groupBy )
				.build();
	}
	
	public static Uri appendHaving( @NonNull final Uri uri, @NonNull final String having ) {
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_HAVING, having )
				.build();
	}

	public static Uri appendDistinct( @NonNull final Uri uri ) {
		return uri.buildUpon()
				.appendQueryParameter( AbstractContentProviderProcessor.QUERY_PARAMETER_DISTINCT, "distinct" )
				.build();
	}

    /**
     * @param tableName name of database table
     * @param projection columns of database table
     * @return a array in which each projection element has the tableName as prefix like "tableName.projectionColumn"
     */
    public static String[] qualifiedProjection(@NonNull final String tableName, @NonNull final String[] projection) {
        final int size = projection.length;
        final String[] joinProjection = new String[ size ];
        for( int index = 0; index < size; index++ ) {
            joinProjection[ index ] = tableName.concat( "." ).concat( projection[ index ] );
        }
        return joinProjection;
    }

    /**
     * @param tableProjections pairs of tableName and columns
     * @return a single array of all projections with the table name as prefix
     */
	public static String[] qualifiedProjection(@NonNull final List<Pair<String, String[]>> tableProjections) {
        int columns = 0;
        final int tables = tableProjections.size();
        for( int index = 0; index < tables; index++ )
            columns += tableProjections.get( index ).second.length;

        final String[] qualifiedProjection = new String[ columns ];
        int currentIndex = 0;
        for( int index = 0; index < tables; index++ ) {
            final Pair<String, String[]> table = tableProjections.get( index );
            final String[] qualifiedColumns = qualifiedProjection( table.first, table.second );
            System.arraycopy( qualifiedColumns, 0, qualifiedProjection, currentIndex, qualifiedColumns.length );
            currentIndex += qualifiedColumns.length;
        }
        return qualifiedProjection;
	}

	public static boolean existColumn(@NonNull final SQLiteDatabase db, @NonNull final String table, @NonNull final String columnName) {
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
