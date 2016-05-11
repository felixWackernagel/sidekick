package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.AbstractContentProviderProcessor;

public class JoinContractContentProviderProcessor extends AbstractContentProviderProcessor {
    private static final int MANY_ITEMS = 1;
    private static final int ONE_ITEM = 2;

    private final JoinContract contract;
    private final String authority;
    private final UriMatcher uriMatcher;

    public JoinContractContentProviderProcessor(@NonNull final JoinContract contract, @NonNull final String authority ) {
        this.authority = authority;
        this.contract = contract;

        final String tables = concat( contract.getTables(), "/");
        uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
        uriMatcher.addURI( authority, tables, MANY_ITEMS);
        uriMatcher.addURI( authority, tables + "/#", ONE_ITEM);
    }

    @Override
    public Cursor query(@NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables( contract.getJoinStatement() );

        if( isItemType( uri ) ) {
            final List<String> path = uri.getPathSegments();
            final String id = path.get(path.size() - 1);
            final String table = path.get(path.size() - 2);
            builder.appendWhere( table + "." + BaseColumns._ID + "=" + id );
        }

        String groupBy = uri.getQueryParameter( QUERY_PARAMETER_GROUP_BY );
        String having = uri.getQueryParameter( QUERY_PARAMETER_HAVING );
        String limit = uri.getQueryParameter( QUERY_PARAMETER_LIMIT );

        Cursor cursor = builder.query( db, projection, selection, selectionArgs, groupBy, having, sortOrder, limit );
        if( resolver != null ) {
            cursor.setNotificationUri( resolver, uri );
        }
        return cursor;
    }

    @Override
    public Uri insert(@NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException( "A uri for joined tables can't be insert." );
    }

    @Override
    public int update(@NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException( "A uri for joined tables can't be update." );
    }

    @Override
    public int delete(@NonNull SQLiteDatabase db, @Nullable ContentResolver resolver, @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException( "A uri for joined tables can't be delete." );
    }

    @Override
    public String getTable() {
        return contract.getJoinStatement();
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String getUniqueType() {
        return concat( contract.getTables(), "." );
    }

    @Override
    public boolean isItemType( @NonNull Uri uri) {
        return uriMatcher.match(uri) == ONE_ITEM;
    }

    @Override
    public boolean isDirectoryType( @NonNull Uri uri) {
        return uriMatcher.match(uri) == MANY_ITEMS;
    }

    @Override
    public boolean canProcess( @NonNull Uri uri ) {
        return ( uriMatcher.match( uri ) != UriMatcher.NO_MATCH  );
    }

    private String concat( String[] parts, String delimeter ) {
        final StringBuilder builder = new StringBuilder();
        final int size = parts.length;
        for( int index = 0; index < size; index++ ) {
            if( index > 0 ) {
                builder.append( delimeter );
            }
            builder.append( parts[index] );
        }
        return builder.toString();
    }
}
