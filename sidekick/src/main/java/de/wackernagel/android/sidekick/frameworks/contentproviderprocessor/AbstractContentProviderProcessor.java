package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public abstract class AbstractContentProviderProcessor implements ContentProviderProcessor {
	public static final String QUERY_PARAMETER_GROUP_BY = "parameter_group_by";
	public static final String QUERY_PARAMETER_HAVING = "parameter_having";
	public static final String QUERY_PARAMETER_LIMIT = "parameter_limit";

	@Override
	public Cursor query( @NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder ) {
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables( getTable() );

		if( isItemType( uri ) ) {
			builder.appendWhere( BaseColumns._ID + "=" + uri.getLastPathSegment() );
		}

		final String groupBy = uri.getQueryParameter( QUERY_PARAMETER_GROUP_BY );
		final String having = uri.getQueryParameter( QUERY_PARAMETER_HAVING );
		final String limit = uri.getQueryParameter( QUERY_PARAMETER_LIMIT );

		final Cursor cursor = builder.query( db, projection, selection, selectionArgs, groupBy, having, sortOrder, limit );
		if( resolver != null ) {
			cursor.setNotificationUri( resolver, uri );
		}
		return cursor;
	}

	@Override
	public Uri insert( @NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable ContentValues values ) {
		long id = 0;

		if( !isItemType( uri ) ) {
			id = db.insert( getTable(), null, values );
		}

		if( resolver != null ) {
			resolver.notifyChange(uri, null);
		}
		return ContentUris.withAppendedId( uri, id );
	}

	@Override
	public int update( @NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs ) {
		int updatedRows;

		if( isItemType( uri ) ) {
			String id = uri.getLastPathSegment();
			if( TextUtils.isEmpty( selection ) ) {
				updatedRows = db.update( getTable(), values, BaseColumns._ID + "=" + id, null );
			} else {
				updatedRows = db.update( getTable(), values, BaseColumns._ID + "=" + id + " and " + selection, selectionArgs );
			}
		} else {
			updatedRows = db.update( getTable(), values, selection, selectionArgs );
		}

		if( resolver != null ) {
			resolver.notifyChange( uri, null );
		}
		return updatedRows;
	}

	@Override
	public int delete( @NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs ) {
		int deletedRows;

		if( isItemType( uri ) ) {
			String id = uri.getLastPathSegment();
			if( TextUtils.isEmpty( selection ) ) {
				deletedRows = db.delete( getTable(), BaseColumns._ID + "=" + id, null);
			} else {
				deletedRows = db.delete( getTable(), BaseColumns._ID + "=" + id + " and " + selection, selectionArgs);
			}
		} else {
			deletedRows = db.delete( getTable(), selection, selectionArgs);
		}

		if( resolver != null ) {
			resolver.notifyChange( uri, null );
		}
		return deletedRows;
	}

	@Override
	public String getType( @NonNull Uri uri ) {
		if( isItemType( uri ) ) {
			return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + getAuthority() + "." + getUniqueType();
		} else if( isDirectoryType( uri )) {
			return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + getAuthority() + "." + getUniqueType();
		} else {
			return null;
		}
	}

	abstract public String getTable();

	abstract public String getAuthority();

	abstract public String getUniqueType();

	abstract public boolean isItemType( @NonNull Uri uri );

	abstract public boolean isDirectoryType( @NonNull Uri uri );
}