package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class SQLiteTableOpenHelper extends SQLiteOpenHelper {

	private static final String TAG = "SQLiteTableOpenHelper";
	
	private final List<SQLiteTable> tables;
	
	public SQLiteTableOpenHelper( @NonNull final Context context, @NonNull final String databaseName, @Nullable final SQLiteDatabase.CursorFactory factory, int version, @NonNull final List<SQLiteTable> tables) {
		super( context, databaseName, factory, version );
		this.tables = tables;
	}

	@Override
	public void onCreate( SQLiteDatabase db ) {
		Log.i( TAG, "onCreate()" );
		for( SQLiteTable table : tables) {
			Log.i(TAG, "> for " + table.getClass().getSimpleName());
			table.onCreate(db);
		}
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
		final int difference = newVersion - oldVersion;
		for( int next = 0; next < difference; next++  ) {
			int from = oldVersion + next;
			int to = from + 1;

			Log.i( TAG, "onUpgrade( from=" + from + ", to=" + to + " )" );
			for( SQLiteTable table : tables) {
				Log.i(TAG, "> for " + table.getClass().getSimpleName());
				table.onUpgrade(db, from, to);
			}
		}
	}

}
