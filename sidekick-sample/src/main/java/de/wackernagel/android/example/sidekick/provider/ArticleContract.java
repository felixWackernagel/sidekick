package de.wackernagel.android.example.sidekick.provider;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.TableContract;

public class ArticleContract extends TableContract {

    public static final String TABLE = "article";

    public static final String COLUMN_TITLE = "title";

    public static final Uri CONTENT_URI = Uri.parse( "content://de.wackernagel.android.example.sidekick/" + TABLE );

    public static final String[] PROJECTION = {
            COLUMN_ID,
            COLUMN_TITLE
    };

    @Override
    public String getTable() {
        return TABLE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "CREATE TABLE IF NOT EXISTS " + TABLE + "(" +
                COLUMN_ID + " INTEGER CONSTRAINT " + COLUMN_ID + "_pk PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TITLE + " TEXT" +
                ");" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
