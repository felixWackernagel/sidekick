package de.wackernagel.android.sidekick.annotations.processor.definitions;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;

import de.wackernagel.android.sidekick.annotations.processor.JavaUtils;

/**
 * String, byte[]
 * boolean, byte, double, float, int, long, short
 * Boolean, Byte, Double, Float, Integer, Long, Short
 */
public class PrimitiveColumnDefinition extends BaseDefinition {

    private final String fieldName;
    private final String columnName;
    private final String constantFieldName;
    private final TypeName objectType;

    public PrimitiveColumnDefinition( TypeName type, String javaFieldName ) {
        this( null, type, javaFieldName );
    }

    public PrimitiveColumnDefinition( Element element, TypeName type ) {
        this( element, type, null );
    }

    private PrimitiveColumnDefinition( Element element, TypeName type, String javaFieldName ) {
        super(element);
        objectType = type;
        fieldName = javaFieldName != null ? javaFieldName :
            element != null ? element.getSimpleName().toString() :
                JavaUtils.toVariableCase(JavaUtils.getSimpleName(type));
        columnName = formatNameForSQL(fieldName);
        constantFieldName = "COLUMN" + ( columnName.startsWith( "_" ) ? "" : "_" ) + columnName.toUpperCase();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public String getConstantFieldName() {
        return constantFieldName;
    }

    @Override
    public TypeName getObjectType() {
        return objectType;
    }

    @Override
    public String getSQLiteType() {
        if( String.class.getName().equals( objectType.toString() ) ) {
            return "TEXT";
        }

        final TypeName type = objectType.isBoxedPrimitive() ? objectType.unbox() : objectType;
        if( TypeName.BOOLEAN.equals( type ) ||
                TypeName.INT.equals( type ) ||
                TypeName.LONG.equals( type ) ||
                TypeName.SHORT.equals( type ) ) {
            return "INTEGER";
        } else if( TypeName.DOUBLE.equals( type ) ||
                TypeName.FLOAT.equals( type ) ) {
            return "REAL";
        } else if( TypeName.BYTE.equals( type ) ||
                "byte[]".equals(type.toString()) ) {
            return "BLOB";
        } else {
            throw new NullPointerException( "Primitive: can't resolve SQLite type of " + objectType.toString() );
        }
    }

    @Override
    public String getCursorMethod() {
        if( String.class.getName().equals( objectType.toString() ) ) {
            return "String";
        }

        final TypeName unboxedType = objectType.isBoxedPrimitive() ? objectType.unbox() : objectType;
        if( unboxedType.equals( TypeName.INT ) || unboxedType.equals( TypeName.BOOLEAN ) ) {
            return "Int";
        } else if( unboxedType.equals( TypeName.SHORT ) ) {
            return "Short";
        } else if( unboxedType.equals( TypeName.LONG ) ) {
            return "Long";
        } else if( unboxedType.equals( TypeName.DOUBLE ) ) {
            return "Double";
        } else if( unboxedType.equals( TypeName.FLOAT ) ) {
            return "Float";
        } else if( "byte[]".equals(objectType.toString()) ) {
            return "Blob";
        } else {
            return "Int";
        }
    }

    /* ************************************************************* */

    @Override
    public boolean isObjectTypeNotPrimitive() {
        return objectType.isBoxedPrimitive() || isString();
    }

    @Override
    public boolean isBoolean() {
        final TypeName type = objectType.isBoxedPrimitive() ? objectType.unbox() : objectType;
        return TypeName.BOOLEAN.equals(type);
    }

    @Override
    public boolean isString() {
        return String.class.getName().equals(objectType.toString());
    }

    @Override
    public boolean isByte() {
        final TypeName type = objectType.isBoxedPrimitive() ? objectType.unbox() : objectType;
        return TypeName.BYTE.equals( type );
    }

    /********************************************************************/

    @Override
    public boolean equals(Object o) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        PrimitiveColumnDefinition that = ( PrimitiveColumnDefinition ) o;

        if( !fieldName.equals(that.fieldName) ) return false;
        return objectType.equals(that.objectType);

    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + objectType.hashCode();
        return result;
    }
}
