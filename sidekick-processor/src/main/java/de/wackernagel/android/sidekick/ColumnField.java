package de.wackernagel.android.sidekick;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import de.wackernagel.android.sidekick.annotations.Contract;

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

    private boolean primitive;
    private Element origin;

    public ColumnField(final Element field, Types typeUtils, Elements elementUtils) {
        origin = field;
        fieldName = field.getSimpleName().toString();
        columnName = columnName(field);
        constantFieldName = "COLUMN" + ( columnName.startsWith( "_" ) ? "" : "_" ) + columnName.toUpperCase();
        objectType = JavaUtils.getType(field.asType());
        primitive = objectType.isPrimitive();

        if( field.asType() instanceof DeclaredType ) {
            final List<? extends TypeMirror> generics = ( (DeclaredType) field.asType() ).getTypeArguments();
            for( TypeMirror g : generics ) {
                System.out.println( "Generic type of " + field + " is " + g.toString() );
            }
        }

        if( !primitive && !objectType.toString().equals( String.class.getName() ) ) {
            fieldName = fieldName.concat("Id");
            columnName = columnName.concat( "_id" );
            constantFieldName = constantFieldName.concat("_ID");
            objectType = TypeName.LONG;
        }
    }

    public ColumnField( final String constantFieldName, final String fieldName, final String columnName, final Class<?> objectType) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.constantFieldName = constantFieldName;
        this.primitive = objectType.isPrimitive();
        this.objectType = primitive ? TypeName.get(objectType) : ClassName.get(objectType);
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
