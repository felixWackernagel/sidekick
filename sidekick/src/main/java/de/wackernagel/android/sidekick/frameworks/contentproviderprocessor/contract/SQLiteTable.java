package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.database.sqlite.SQLiteDatabase;

public interface SQLiteTable {

    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
