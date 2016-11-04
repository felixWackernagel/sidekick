package de.wackernagel.android.sidekick.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;

public class SparseArrayUtils {

    private SparseArrayUtils() {}

    /**
     * Return a list with all elements from SparseArray. If the SparseArray was null it returns a empty list.
     *
     * @param array data source for list
     * @return list with all elements from array
     */
    @NonNull
    public static <T> ArrayList<T> asList( @Nullable final SparseArrayCompat<T> array ) {
        if( array == null || array.size() == 0 ) {
            return new ArrayList<>( 0 );
        }

        final int size = array.size();
        final ArrayList<T> list = new ArrayList<>( size );
        for( int index = 0; index < size; index++ ) {
            list.add( array.valueAt( index ) );
        }
        return list;
    }

}
