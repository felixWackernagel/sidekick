package de.wackernagel.android.sidekick.frameworks.objectcursor;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

public class ColumnIndexCache {

    private ArrayMap<String, Integer> cache = new ArrayMap<>();

    /**
     * Returns the column index for a given column name from a cache or
     * on cache miss it returns the column index from the cursor directly and fills the cache.
     *
     * @param cursor to get its column index
     * @param columnName name of target column
     * @return the zero-based column index for the given column name, or -1 if the column name does not exist.
     */
    public int getColumnIndex( @NonNull final Cursor cursor, @NonNull final String columnName ) {
        if (!cache.containsKey(columnName))
            cache.put(columnName, cursor.getColumnIndex(columnName));
        return cache.get(columnName);
    }

    /**
     * Clears the cache. Call this this method if you get all data from cursor.
     */
    public void clear() {
        cache.clear();
    }

}
