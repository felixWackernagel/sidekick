package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public abstract class AbstractContentProvider extends ContentProvider implements SQLiteTransactionListener {

    private SQLiteOpenHelper dbHelper;
    private ContentProviderProcessor[] processors;
    private final ThreadLocal<Boolean> inTransaction = new ThreadLocal<>();

    @Override
    public boolean onCreate() {
        dbHelper = onCreateSQLiteOpenHelper();
        processors = onCreateContentProviderProcessors();
        return true;
    }

    @Override
    public void shutdown() {
        if( dbHelper != null ) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    @NonNull
    public abstract SQLiteOpenHelper onCreateSQLiteOpenHelper();

    @NonNull
    public abstract ContentProviderProcessor[] onCreateContentProviderProcessors();

    @Override
    public String getType(@NonNull final Uri uri) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            return null;
        }
        return processor.getType(uri);
    }

    @Override
    public Cursor query(@NonNull final Uri uri, @Nullable final String[] projection, @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable final String sortOrder) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return processor.query(dbHelper.getReadableDatabase(), getContentResolver(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public Uri insert(@NonNull final Uri uri, @Nullable final ContentValues values) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return processor.insert(dbHelper.getWritableDatabase(), getContentResolver(), uri, values);
    }

    @Override
    public int update(@NonNull final Uri uri, @Nullable final ContentValues values, @Nullable final String selection, @Nullable final String[] selectionArgs) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return processor.update(dbHelper.getWritableDatabase(), getContentResolver(), uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(@NonNull final Uri uri, @Nullable final String selection, @Nullable final String[] selectionArgs) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return processor.delete(dbHelper.getWritableDatabase(), getContentResolver(), uri, selection, selectionArgs);
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside
     * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @NonNull
    @Override
    public ContentProviderResult[] applyBatch( @NonNull final ArrayList<ContentProviderOperation> operations ) throws OperationApplicationException {
        if( operations.isEmpty() ) {
            return new ContentProviderResult[0];
        }

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransactionWithListener( this );
        try {
            inTransaction.set(Boolean.TRUE);
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for( int index = 0; index < numOperations; index++ ) {
                final ContentProviderOperation operation = operations.get(index);
                results[index] = operation.apply( this, results, index );
                if( operation.isYieldAllowed() ) {
                    db.yieldIfContendedSafely();
                }
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
            inTransaction.set(Boolean.FALSE);
        }
    }

    @Override
    public int bulkInsert( @NonNull final Uri uri, @NonNull final ContentValues[] values) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if( processor == null ) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if( values.length == 0 ) {
            return 0;
        }

        int inserts = 0;
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransactionWithListener( this );
        try {
            inTransaction.set(Boolean.TRUE);
            final int operations = values.length;
            for( int index = 0; index < operations; index++ ) {
                if( processor.insert(db, getContentResolver(), uri, values[index]) != null ) {
                    inserts++;
                } else {
                    // if one operation fails then stop inserts and rollback
                    return 0;
                }
                db.yieldIfContendedSafely();
            }
            db.setTransactionSuccessful();
            return inserts;
        } finally {
            db.endTransaction();
            inTransaction.set(Boolean.FALSE);
        }
    }

    @Override
    public void onBegin() {
    }

    @Override
    public void onCommit() {
    }

    @Override
    public void onRollback() {
    }

    private boolean isInTransaction() {
        final Boolean inTransaction = this.inTransaction.get();
        return inTransaction != null && inTransaction;
    }

    @Nullable
    private ContentProviderProcessor findProcessor(@NonNull final Uri uri) {
        final int size = processors.length;
        for( int index = 0; index < size; index++ ) {
            if( processors[index].canProcess( uri ) ) {
                return processors[index];
            }
        }
        return null;
    }

    @Nullable
    private ContentResolver getContentResolver() {
        final Context context = getContext();
        return context != null ? context.getContentResolver() : null;
    }

    @NonNull
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return dbHelper;
    }

    @NonNull
    public ContentProviderProcessor[] getProcessors() {
        return processors;
    }
}