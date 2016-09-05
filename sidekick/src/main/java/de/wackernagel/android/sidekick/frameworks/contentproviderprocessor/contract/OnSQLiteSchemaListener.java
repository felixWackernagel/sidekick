package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.database.sqlite.SQLiteDatabase;

public interface OnSQLiteSchemaListener {
    void onBeforeCreate( SQLiteDatabase sqLiteDatabase );
    void onAfterCreate( SQLiteDatabase sqLiteDatabase );
    void onBeforeUpgrade( SQLiteDatabase db, int oldVersion, int newVersion );
    void onAfterUpgrade( SQLiteDatabase db, int oldVersion, int newVersion );
}
