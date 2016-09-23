package de.wackernagel.android.sidekick.annotations.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.ForeignKey;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;

public class ColumnDefinition extends Definition {

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
    private TypeName collectionElementModelType;

    /**
     * SQLite type
     */
    private String sqliteType;

    private Element origin;

    private boolean primitiveType;
    private boolean arrayType;
    private boolean stringType;
    private boolean collectionType;

    private boolean isFinal;
    private boolean skipSQLite = false;

    public ColumnDefinition(final Element field, final TypeName type, final boolean primitiveType, final boolean arrayType, final boolean stringType, final Types types, final Elements elements, final Messager log ) {
        this(field, type, primitiveType, arrayType, stringType, false, types, elements, log);
    }

    public ColumnDefinition(final Element field, final TypeName type, final boolean primitiveType, final boolean arrayType, final boolean stringType, final boolean collectionType, final Types types, final Elements elements, final Messager log ) {
        super( types, elements, log );
        this.origin = field;
        this.objectType = type;
        this.primitiveType = primitiveType;
        this.arrayType = arrayType;
        this.stringType = stringType;
        this.collectionType = collectionType;

        fieldName = field.getSimpleName().toString();
        columnName = formatNameForSQL(fieldName);
        constantFieldName = "COLUMN" + ( columnName.startsWith( "_" ) ? "" : "_" ) + columnName.toUpperCase();

        sqliteType = resolveSQLiteType( objectType, stringType, primitiveType, arrayType );

        // one-one foreign key
        if( !primitiveType && !arrayType && !stringType && !collectionType ) {
            objectType = ClassName.bestGuess( type.toString() + "Model" );

            constantFieldName = constantFieldName.concat("_ID");
            columnName = columnName.concat( "_id" );
            sqliteType = "INTEGER";
        }

        // one-many/many-many foreign key
        if( !primitiveType && !arrayType && !stringType && collectionType ) {
            collectionElementModelType = ClassName.bestGuess( JavaUtils.getGenericTypes( field ).iterator().next().toString() + "Model" );
            objectType = ParameterizedTypeName.get(
                    ClassName.get((TypeElement)((DeclaredType)origin.asType()).asElement()),
                    collectionElementModelType);
            skipSQLite = true;
        }
    }


    public static ColumnDefinition primaryField( final Types types, final Elements elements, final Messager log ) {
        return new ColumnDefinition( types, elements, log);
    }

    private ColumnDefinition( final Types types, final Elements elements, final Messager log ) {
        super(types, elements, log);
        this.origin = null;
        this.fieldName = "id";
        this.columnName = "_id";
        this.constantFieldName = "COLUMN_ID";
        this.primitiveType = true;
        this.objectType = TypeName.get(long.class);
        this.sqliteType = "INTEGER";
        this.isFinal = true;
        this.collectionType = false;
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

    public boolean isContractObjectType() {
        return !primitiveType && !arrayType && !stringType && !collectionType;
    }

    public String getSQLiteType() {
        return sqliteType;
    }

    public boolean isCollectionType() {
        return collectionType;
    }

    public TypeName getInstantiableCollectionType() {
        if( JavaUtils.isListType( origin, elements, types) && origin.asType().toString().startsWith( List.class.getTypeName() ) ) {
            return ParameterizedTypeName.get(
                    ClassName.get(ArrayList.class),
                    collectionElementModelType);
        } else if( JavaUtils.isSetType(origin, elements, types) && origin.asType().toString().startsWith( Set.class.getTypeName() ) ) {
            return ParameterizedTypeName.get(
                    ClassName.get(LinkedHashSet.class),
                    collectionElementModelType);
        } else {
            return objectType;
        }
    }

    public TypeName getCollectionElementModelType() {
        return collectionElementModelType;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public NotNull notNull() {
        return origin != null ? origin.getAnnotation(NotNull.class) : null;
    }

    public boolean isNotNull() {
        return collectionType || notNull() != null;
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

    @Override
    public boolean equals(Object o) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        ColumnDefinition that = ( ColumnDefinition ) o;

        if( !fieldName.equals(that.fieldName) ) return false;
        return objectType.equals(that.objectType);

    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + objectType.hashCode();
        return result;
    }

    public boolean isBoolean() {
        return primitiveType && objectType.equals( TypeName.BOOLEAN );

    }

    public boolean isString() {
        return stringType;
    }

    public boolean isForeignKey() {
        return !stringType && !primitiveType && !arrayType;
    }

    public ForeignKey foreignKey() {
        return origin != null ? origin.getAnnotation(ForeignKey.class) : null;
    }

    public Unique unique() {
        return origin != null ? origin.getAnnotation( Unique.class ) : null;
    }

    public boolean skipSQLite() {
        return skipSQLite;
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

    @Override
    public String toString() {
        return "Definition of " + objectType + " " + fieldName;
    }
}
