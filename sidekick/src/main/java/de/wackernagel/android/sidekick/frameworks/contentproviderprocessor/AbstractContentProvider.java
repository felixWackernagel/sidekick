package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class AbstractContentProvider extends ContentProvider {

    private SQLiteOpenHelper dbHelper;
    private ContentProviderProcessor[] processors;

    @Override
    public boolean onCreate() {
        dbHelper = onCreateSQLiteOpenHelper();
        processors = onCreateContentProviderProcessors();
        return true;
    }

    @NonNull
    public abstract SQLiteOpenHelper onCreateSQLiteOpenHelper();

    @NonNull
    public abstract ContentProviderProcessor[] onCreateContentProviderProcessors();

    @Override
    public String getType(@NonNull Uri uri) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            return null;
        }
        return processor.getType(uri);
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unkown URI: " + uri);
        }
        return processor.query(dbHelper.getWritableDatabase(), getContentResolver(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unkown URI: " + uri);
        }
        return processor.insert(dbHelper.getWritableDatabase(), getContentResolver(), uri, values);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unkown URI: " + uri);
        }
        return processor.update(dbHelper.getWritableDatabase(), getContentResolver(), uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final ContentProviderProcessor processor = findProcessor(uri);
        if (processor == null) {
            throw new IllegalArgumentException("Unkown URI: " + uri);
        }
        return processor.delete(dbHelper.getWritableDatabase(), getContentResolver(), uri, selection, selectionArgs);
    }

    @Nullable
    private ContentProviderProcessor findProcessor(@NonNull Uri uri) {
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

}