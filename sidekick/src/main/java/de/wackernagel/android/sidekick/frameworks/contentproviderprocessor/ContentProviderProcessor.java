package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public interface ContentProviderProcessor {
	Cursor query(SQLiteDatabase db, ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);
	Uri insert(SQLiteDatabase db, ContentResolver resolver, Uri uri, ContentValues values);
	int update(SQLiteDatabase db, ContentResolver resolver, Uri uri, ContentValues values, String selection, String[] selectionArgs);
	int delete(SQLiteDatabase db, ContentResolver resolver, Uri uri, String selection, String[] selectionArgs);
	String getType(Uri uri);
	boolean canProcess(Uri uri);
}
