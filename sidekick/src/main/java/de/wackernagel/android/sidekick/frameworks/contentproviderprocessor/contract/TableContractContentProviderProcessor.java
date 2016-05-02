package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.content.UriMatcher;
import android.net.Uri;
import android.support.annotation.NonNull;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.AbstractContentProviderProcessor;

public class TableContractContentProviderProcessor extends AbstractContentProviderProcessor {
    private static final int MANY_ITEMS = 1;
    private static final int ONE_ITEM = 2;

    private final TableContract contract;
    private final String authority;
    private final UriMatcher uriMatcher;

    public TableContractContentProviderProcessor(@NonNull final TableContract contract, @NonNull final String authority) {
        this.contract = contract;
        this.authority = authority;

        uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
        uriMatcher.addURI( authority, contract.getTable(), MANY_ITEMS);
        uriMatcher.addURI( authority, contract.getTable() + "/#", ONE_ITEM);
    }

    @Override
    public String getTable() {
        return contract.getTable();
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String getUniqueType() {
        return contract.getTable();
    }

    @Override
    public boolean isItemType( @NonNull Uri uri) {
        return (uriMatcher.match( uri ) == ONE_ITEM);
    }

    @Override
    public boolean isDirectoryType( @NonNull Uri uri) {
        return ( uriMatcher.match( uri ) == MANY_ITEMS );
    }

    @Override
    public boolean canProcess( @NonNull Uri uri ) {
        return ( uriMatcher.match( uri ) != UriMatcher.NO_MATCH  );
    }
}
