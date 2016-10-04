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
    private final String fieldName;

    /**
     * Name of the SQLite column
     */
    private final String columnName;

    /**
     * Name of the column name constant
     */
    private final String constantFieldName;

    /**
     * Object type
     */
    private final TypeName objectType;
    private final TypeName collectionElementType;
    private final TypeName collectionElementModelType;

    /**
     * SQLite type
     */
    private final String sqliteType;

    private final Element origin;

    private final boolean primaryKey;
    private final boolean primitiveType;
    private final boolean collectionType;

    private final boolean skipSQLite;

    public static ColumnDefinition primaryField( final Types types, final Elements elements, final Messager log ) {
        return new ColumnDefinition( types, elements, log);
    }

    public static ColumnDefinition primitiveField( final Element field, final TypeName type, final Types types, final Elements elements, final Messager log ) {
        return new ColumnDefinition( field, type, types, elements, log );
    }

    public static ColumnDefinition contractField( final Element field, final TypeName type, final Types types, final Elements elements, final Messager log ) {
        return new ColumnDefinition( field, type, true, types, elements, log );
    }

    public static ColumnDefinition collectionContractField(final Element field, final TypeName type, final Types types, final Elements elements, final Messager log) {
        return new ColumnDefinition( field, type, false, true, types, elements, log );
    }

    // Collection<Contract>
    private ColumnDefinition(final Element field, final TypeName type, final boolean isModel, final boolean isCollection, final Types types, final Elements elements, final Messager log ) {
        super(types, elements, log);
        this.origin = field;
        this.primitiveType = false;
        this.collectionType = true;
        this.primaryKey = false;
        this.skipSQLite = true;
        this.collectionElementType = ClassName.bestGuess( JavaUtils.getGenericTypes( origin ).iterator().next().toString() );
        this.collectionElementModelType = ClassName.bestGuess( JavaUtils.getGenericTypes( origin ).iterator().next().toString() + "Model" );
        this.objectType = ParameterizedTypeName.get(
                ClassName.get(( TypeElement ) (( DeclaredType ) origin.asType()).asElement()),
                collectionElementModelType);

        fieldName = field != null ? field.getSimpleName().toString() : JavaUtils.toVariableCase(JavaUtils.getSimpleName(type));
        columnName = formatNameForSQL(fieldName).concat("_id");
        constantFieldName = "COLUMN" + (columnName.startsWith("_") ? "" : "_") + columnName.toUpperCase();
        sqliteType = "";
    }

    // Contract
    private ColumnDefinition(final Element field, final TypeName type, final boolean isModel, final Types types, final Elements elements, final Messager log ) {
        super(types, elements, log);
        this.origin = field;
        this.objectType = type;
        this.primitiveType = false;
        this.collectionType = false;
        this.primaryKey = false;
        this.skipSQLite = false;
        this.collectionElementType = null;
        this.collectionElementModelType = null;

        fieldName = field != null ? field.getSimpleName().toString() : JavaUtils.toVariableCase(JavaUtils.getSimpleName(type));
        columnName = formatNameForSQL(fieldName).concat("_id");
        constantFieldName = "COLUMN" + (columnName.startsWith("_") ? "" : "_") + columnName.toUpperCase();
        sqliteType = "INTEGER";
    }

    /**
     * @param field is member variable of defined class with Contract annotation
     * @param type of member variable from defined class with Contract annotation
     * @param types as helper
     * @param elements as helper
     * @param log as helper
     */
    // Primitive
    private ColumnDefinition(final Element field, final TypeName type, final Types types, final Elements elements, final Messager log ) {
        super( types, elements, log );
        this.origin = field;
        this.objectType = type;
        this.primitiveType = true;
        this.collectionType = false;
        this.primaryKey = false;
        this.skipSQLite = false;
        this.collectionElementType = null;
        this.collectionElementModelType = null;

        fieldName = field != null ? field.getSimpleName().toString() : JavaUtils.toVariableCase( JavaUtils.getSimpleName( type ) );
        columnName = formatNameForSQL(fieldName);
        constantFieldName = "COLUMN" + ( columnName.startsWith( "_" ) ? "" : "_" ) + columnName.toUpperCase();
        sqliteType = resolveSQLiteType( objectType );
    }

    // Primary
    private ColumnDefinition( final Types types, final Elements elements, final Messager log ) {
        super(types, elements, log);
        this.origin = null;
        this.fieldName = "id";
        this.columnName = "_id";
        this.constantFieldName = "COLUMN_ID";
        this.primitiveType = true;
        this.objectType = TypeName.get(long.class);
        this.sqliteType = "INTEGER";
        this.collectionType = false;
        this.primaryKey = true;
        this.skipSQLite = false;
        this.collectionElementType = null;
        this.collectionElementModelType = null;
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
        if( !primitiveType && !collectionType ) {
            return ClassName.bestGuess( objectType.toString() + "Model" );
        }
        return objectType;
    }

    /**
     * @return true if objectType is non-primitive and non-collection type with @Contract annotation otherwise false
     */
    public boolean isContractObjectType() {
        return !primitiveType && !collectionType;
    }

    /**
     * @return sqlite type
     */
    public String getSQLiteType() {
        return sqliteType;
    }

    /**
     * @return true if objectType can be null like all non primitive types otherwise false
     */
    public boolean isObjectTypeNotPrimitive() {
        return collectionType || !primitiveType || objectType.isBoxedPrimitive();
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
        return true;
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
        return primaryKey;
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
        return String.class.getName().equals( objectType.toString() );
    }

    /**
     * @return true if objectType is Collection<@Contract> or Class with @Contract
     */
    public boolean isForeignKey() {
        return ( !primitiveType && !collectionType ) || collectionType;
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

    private String resolveSQLiteType( final TypeName type ) {
        if( String.class.getName().equals( type.toString() ) ) {
            return "TEXT";
        }
        if( collectionType ) {
            return "INTEGER";
        }

        final TypeName unboxedType = type.isBoxedPrimitive() ? type.unbox() : type;
        if( unboxedType.equals( TypeName.BOOLEAN ) ||
                unboxedType.equals( TypeName.INT ) ||
                unboxedType.equals( TypeName.LONG ) ||
                unboxedType.equals( TypeName.SHORT ) ) {
            return "INTEGER";
        } else if( unboxedType.equals( TypeName.DOUBLE ) ||
                unboxedType.equals( TypeName.FLOAT ) ) {
            return "REAL";
        } else {
            return "BLOB";
        }
    }

    @Override
    public String toString() {
        return "Definition of " + objectType + " " + fieldName;
    }

}
