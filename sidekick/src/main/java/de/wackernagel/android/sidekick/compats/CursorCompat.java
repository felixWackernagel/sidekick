package de.wackernagel.android.sidekick.compats;

import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CursorCompat {

    public static final int FIELD_TYPE_NULL = 0;
    public static final int FIELD_TYPE_INTEGER = 1;
    public static final int FIELD_TYPE_FLOAT = 2;
    public static final int FIELD_TYPE_STRING = 3;
    public static final int FIELD_TYPE_BLOB = 4;

    @IntDef({FIELD_TYPE_NULL, FIELD_TYPE_INTEGER, FIELD_TYPE_FLOAT, FIELD_TYPE_BLOB, FIELD_TYPE_STRING})
    @Retention( RetentionPolicy.SOURCE)
    public @interface CursorType {}

    private CursorCompat() {
    }

    /**
     * Returns data type of the given column's value.
     * The preferred type of the column is returned but the data may be converted to other types
     * as documented in the get-type methods such as {@link Cursor#getInt(int)}, {@link Cursor#getFloat(int)}
     * etc.<br>
     * Returned column types are:<br>
     * <ul>
     *   <li>{@link #FIELD_TYPE_NULL}</li>
     *   <li>{@link #FIELD_TYPE_INTEGER}</li>
     *   <li>{@link #FIELD_TYPE_FLOAT}</li>
     *   <li>{@link #FIELD_TYPE_STRING}</li>
     *   <li>{@link #FIELD_TYPE_BLOB}</li>
     *</ul>
     *
     * @param columnIndex the zero-based index of the target column.
     * @return column value type
     */
    @CursorType
    public static int getType( @NonNull final Cursor cursor, int columnIndex ) {
        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ) {
            final SQLiteCursor sqLiteCursor = (SQLiteCursor) cursor;
            final CursorWindow cursorWindow = sqLiteCursor.getWindow();
            final int position = cursor.getPosition();
            if ( cursorWindow.isNull(position, columnIndex ) ) {
                return FIELD_TYPE_NULL;
            } else if (cursorWindow.isLong( position, columnIndex ) ) {
                return FIELD_TYPE_INTEGER;
            } else if (cursorWindow.isFloat( position, columnIndex ) ) {
                return FIELD_TYPE_FLOAT;
            } else if (cursorWindow.isBlob(position, columnIndex) ) {
                return FIELD_TYPE_BLOB;
            } else {
                return FIELD_TYPE_STRING;
            }
        } else {
            switch( cursor.getType( columnIndex ) ) {
                case Cursor.FIELD_TYPE_BLOB:
                    return FIELD_TYPE_BLOB;
                case Cursor.FIELD_TYPE_FLOAT:
                    return FIELD_TYPE_FLOAT;
                case Cursor.FIELD_TYPE_INTEGER:
                    return FIELD_TYPE_INTEGER;
                case Cursor.FIELD_TYPE_NULL:
                    return FIELD_TYPE_NULL;
                case Cursor.FIELD_TYPE_STRING:
                    return FIELD_TYPE_STRING;
                default:
                    throw new RuntimeException( "Unknown cursor type!" );
            }
        }
    }
}