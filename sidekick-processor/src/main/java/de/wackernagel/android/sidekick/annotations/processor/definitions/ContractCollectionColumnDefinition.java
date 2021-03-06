package de.wackernagel.android.sidekick.annotations.processor.definitions;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import de.wackernagel.android.sidekick.annotations.processor.JavaUtils;

public class ContractCollectionColumnDefinition extends BaseDefinition {

    private final String fieldName;
    private final String columnName;
    private final String constantFieldName;

    private final TypeName originCollectionElementObjectType;
    private final TypeName collectionElementObjectType;
    private final TypeName collectionObjectType;

    private final Types types;
    private final Elements elements;

    public ContractCollectionColumnDefinition(Element element, final TypeName type, Types types, Elements elements) {
        super(element);
        this.types = types;
        this.elements = elements;

        fieldName = element != null ? element.getSimpleName().toString() : JavaUtils.toVariableCase(JavaUtils.getSimpleName(type));
        columnName = formatNameForSQL(fieldName).concat("_id");
        constantFieldName = "COLUMN" + (columnName.startsWith("_") ? "" : "_") + columnName.toUpperCase();

        final String originElementObjectType = JavaUtils.getGenericTypes(origin).iterator().next().toString();
        originCollectionElementObjectType = ClassName.bestGuess( originElementObjectType );
        collectionElementObjectType = ClassName.bestGuess(originElementObjectType.concat( "Model" ) );
        collectionObjectType = ParameterizedTypeName.get(
                ClassName.get(( TypeElement ) (( DeclaredType ) origin.asType()).asElement()),
                collectionElementObjectType);
    }

    /**
     * Collections stand for m-n- or m-one-relation.
     * The SQLite type is on the one-relation type or in a extra m-n table defined.
     *
     * @return always true
     */
    @Override
    public boolean skipSQLite() {
        return true;
    }

    @Override
    public String getSQLiteType() {
        throw new UnsupportedOperationException( "ContractCollection: No sqlite type needed for " + collectionObjectType.toString() );
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
    public boolean isObjectTypeNotPrimitive() {
        return true;
    }

    @Override
    public TypeName getObjectType() {
        return collectionObjectType;
    }

    @Override
    public boolean isCollectionType() {
        return true;
    }

    @Override
    public TypeName getInstantiableCollectionType() {
        if( JavaUtils.isListType( origin, elements, types) && origin.asType().toString().startsWith( List.class.getName() ) ) {
            // List -> ArrayList
            return ParameterizedTypeName.get(
                    ClassName.get(ArrayList.class),
                    collectionElementObjectType);
        } else if( JavaUtils.isSetType(origin, elements, types) && origin.asType().toString().startsWith( Set.class.getName() ) ) {
            // Set -> LinkedHashSet
            return ParameterizedTypeName.get(
                    ClassName.get(LinkedHashSet.class),
                    collectionElementObjectType);
        } else {
            // use defined Collection type -> IDE warns for error
            return collectionObjectType;
        }
    }

    @Override
    public TypeName getOriginCollectionElementObjectType() {
        return originCollectionElementObjectType;
    }

    @Override
    public TypeName getCollectionElementObjectType() {
        return collectionElementObjectType;
    }

    @Override
    public String getCursorMethod() {
        throw new UnsupportedOperationException( "ContractCollection: unknown mapping from cursor to " + collectionObjectType.toString() );
    }

    @Override
    public CodeBlock getCursorToObjectCodeLine(int index) {
        throw new UnsupportedOperationException( "ContractCollection: unknown mapping from cursor to " + collectionObjectType.toString() );
    }

    @Override
    public boolean isForeignKey() {
        return true;
    }

    /********************************************************************/

    @Override
    public boolean equals(Object o) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        ContractCollectionColumnDefinition that = ( ContractCollectionColumnDefinition ) o;

        if( !fieldName.equals(that.fieldName) ) return false;
        return collectionObjectType.equals(that.collectionObjectType);

    }

    @Override
    public int hashCode() {
        int result = fieldName.hashCode();
        result = 31 * result + collectionObjectType.hashCode();
        return result;
    }
}
