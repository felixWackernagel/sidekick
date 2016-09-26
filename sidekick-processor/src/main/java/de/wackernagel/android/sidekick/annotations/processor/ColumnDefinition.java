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
    private TypeName collectionElementType;
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

    /**
     * @param field is member variable of defined class with Contract annotation
     * @param type of member variable from defined class with Contract annotation
     * @param primitiveType when Element is primitive type
     * @param arrayType when Element is arrayType
     * @param stringType when Element is String
     * @param types as helper
     * @param elements as helper
     * @param log as helper
     */
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

        sqliteType = resolveSQLiteType( objectType, stringType, primitiveType );

        // one-one foreign key
        if( !primitiveType && !arrayType && !stringType && !collectionType ) {
            objectType = ClassName.bestGuess( type.toString() + "Model" );

            constantFieldName = constantFieldName.concat("_ID");
            columnName = columnName.concat( "_id" );
            sqliteType = "INTEGER";
        }

        // one-many/many-many foreign key
        if( !primitiveType && !arrayType && !stringType && collectionType ) {
            collectionElementType = ClassName.bestGuess( JavaUtils.getGenericTypes( field ).iterator().next().toString() );
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

    /**
     * @return origin name of member variable
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return name of sqlite column
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @return name of member variable for column name constants
     */
    public String getConstantFieldName() {
        return constantFieldName;
    }

    /**
     * @return type of member variable like int, array[] or ...Model or List<...Model>
     */
    public TypeName getObjectType() {
        return objectType;
    }

    /**
     * @return true if objectType is non-primitive and non-collection type with @Contract annotation otherwise false
     */
    public boolean isContractObjectType() {
        return !primitiveType && !arrayType && !stringType && !collectionType;
    }

    /**
     * @return sqlite type
     */
    public String getSQLiteType() {
        return sqliteType;
    }

    /**
     * @return true if objectType inherits from Collection class otherwise false
     */
    public boolean isCollectionType() {
        return collectionType;
    }

    /**
     * Maps objectType List to ArrayList and Set to LinkedHashSet.
     * Every other type which inherits from Collection is used like defined.
     *
     * @return objectType of a collection type which might be instantiable.
     */
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

    /**
     * @return objectType of original Collection element type
     */
    public TypeName getCollectionElementType() {
        return collectionElementType;
    }

    /**
     * @return objectType of Collection element as ...Model
     */
    public TypeName getCollectionElementModelType() {
        return collectionElementModelType;
    }

    /**
     * @return true if member variable inside model has no setter otherwise false
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * @return NotNull annotation or null if no one exist
     */
    public NotNull notNull() {
        return origin != null ? origin.getAnnotation(NotNull.class) : null;
    }

    /**
     * @return true if objectType is collection or a NotNull annotation exist
     */
    public boolean isNotNull() {
        return collectionType || notNull() != null;
    }

    /**
     * @return true only for _id column
     */
    public boolean isPrimaryKey() {
        return origin == null;
    }

    /**
     * @return value of Default annotation or null if no one exist
     */
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

    /**
     * @return true if objectType is the primitive boolean otherwise false
     */
    public boolean isBoolean() {
        return primitiveType && objectType.equals( TypeName.BOOLEAN );
    }

    /**
     * @return true if objectType is String otherwise false
     */
    public boolean isString() {
        return stringType;
    }

    /**
     * @return true if objectType is Collection<@Contract> or Class with @Contract
     */
    public boolean isForeignKey() {
        return !stringType && !primitiveType && !arrayType;
    }

    /**
     * @return ForeignKey annotation or null
     */
    public ForeignKey foreignKey() {
        return origin != null ? origin.getAnnotation(ForeignKey.class) : null;
    }

    /**
     * @return Unique annotation or null
     */
    public Unique unique() {
        return origin != null ? origin.getAnnotation( Unique.class ) : null;
    }

    /**
     * @return true if objectType is Collection
     */
    public boolean skipSQLite() {
        return skipSQLite;
    }

    private String resolveSQLiteType( TypeName type, boolean stringType, boolean primitiveType ) {
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
