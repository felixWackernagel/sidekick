package de.wackernagel.android.sidekick.annotations.processor;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;

public class ColumnField {

    /**
     * Name for parameters
     */
    private String fieldName;

    /**
     * Name of the SQLite column
     */
    private String columnName;

    /**
     * Name of the column name constant
     */
    private String constantFieldName;

    /**
     * Object type
     */
    private TypeName objectType;

    /**
     * SQLite type
     */
    private String sqliteType;

    private Element origin;

    private boolean primitiveType;
    private boolean arrayType;
    private boolean stringType;

    private boolean isFinal;

    public ColumnField( final Element field, final TypeName type, final boolean primitiveType, final boolean arrayType, final boolean stringType, final Types typeUtils, final Elements elementUtils) {
        this.origin = field;
        this.objectType = type;
        this.primitiveType = primitiveType;
        this.arrayType = arrayType;
        this.stringType = stringType;

        fieldName = field.getSimpleName().toString();
        columnName = columnName(field);
        constantFieldName = "COLUMN" + ( columnName.startsWith( "_" ) ? "" : "_" ) + columnName.toUpperCase();

        sqliteType = resolveSQLiteType( objectType, stringType, primitiveType, arrayType );

        // simple foreign key
        if( !primitiveType && !arrayType && !stringType ) {
            objectType = TypeName.LONG;
            fieldName = fieldName.concat("Id");
            columnName = columnName.concat( "_id" );
            constantFieldName = constantFieldName.concat("_ID");
            sqliteType = "INTEGER";
        }
    }

    public static ColumnField primaryField() {
        return new ColumnField();
    }

    private ColumnField() {
        this.origin = null;
        this.fieldName = "id";
        this.columnName = "_id";
        this.constantFieldName = "COLUMN_ID";
        this.primitiveType = true;
        this.objectType = TypeName.get(long.class);
        this.sqliteType = "INTEGER";
        this.isFinal = true;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getConstantFieldName() {
        return constantFieldName;
    }

    public TypeName getObjectType() {
        return objectType;
    }

    public String getSQLiteType() {
        return sqliteType;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isNotNull() {
        return origin != null && origin.getAnnotation(NotNull.class) != null;
    }

    public boolean isPrimaryKey() {
        return origin == null;
    }

    public String defaultValue() {
        Default value = origin != null ? origin.getAnnotation( Default.class ) : null;
        if( value == null ) {
            return null;
        } else {
            return value.value();
        }
    }

    public boolean isBoolean() {
        return primitiveType && objectType.equals( TypeName.BOOLEAN );
    }

    public boolean isString() {
        return stringType;
    }

    public boolean isUnique() {
        return origin != null && origin.getAnnotation( Unique.class ) != null;
    }

    private String resolveSQLiteType( TypeName type, boolean stringType, boolean primitiveType, boolean arrayType) {
        if( stringType ) {
            return "TEXT";
        } else if( primitiveType ) {
            if( type.equals( TypeName.BOOLEAN ) ||
                    type.equals( TypeName.INT ) ||
                    type.equals( TypeName.LONG ) ||
                    type.equals( TypeName.SHORT ) ) {
                return "INTEGER";
            } else if( type.equals( TypeName.DOUBLE ) ||
                    type.equals( TypeName.FLOAT ) ) {
                return "REAL";
            } else {
                return "BLOB";
            }
        } else {
            return "BLOB";
        }
    }

    /**
     * Convert the field name to a database column name.
     * Makes 'deliveryAddress' to 'delivery_address'.
     *
     * @param field as name template
     * @return name of column
     */
    private static String columnName(final Element field) {
        final String name = field.getSimpleName().toString();
        final StringBuilder sb = new StringBuilder(name);
        final int length = sb.length();
        int offset = 0;
        for (int index = 0; index < length; index++) {
            if (index > 0 && Character.isUpperCase(name.charAt(index))) {
                sb.insert(index + offset, "_");
                offset++;
            }
        }
        return sb.toString().toLowerCase();
    }

}
