package de.wackernagel.android.sidekick.annotations.processor.definitions;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;

import de.wackernagel.android.sidekick.annotations.processor.JavaUtils;

public class ContractColumnDefinition extends BaseDefinition {

    private final TypeName objectType;
    private final String fieldName;
    private final String columnName;
    private final String constantFieldName;

    /**
     * Used for many to many relation
     *
     * @param type of referencing table
     */
    public ContractColumnDefinition(TypeName type) {
        this( null, type );
    }

    /**
     * Used for one to one / one to many relation
     *
     * @param element of definition
     * @param type of referencing table
     */
    public ContractColumnDefinition(Element element, TypeName type) {
        super(element);
        objectType = type;
        fieldName = element != null ? element.getSimpleName().toString() : JavaUtils.toVariableCase(JavaUtils.getSimpleName(type));
        columnName = formatNameForSQL(fieldName).concat("_id");
        constantFieldName = "COLUMN" + (columnName.startsWith("_") ? "" : "_") + columnName.toUpperCase();
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
        return ClassName.bestGuess(objectType.toString() + "Model");
    }

    @Override
    public String getSQLiteType() {
        return "INTEGER";
    }

    @Override
    public boolean isContractObjectType() {
        return true;
    }

    @Override
    public boolean isObjectTypeNotPrimitive() {
        return false; // most of the time is long
    }

    @Override
    public boolean isForeignKey() {
        return true;
    }

    @Override
    public String getCursorMethod() {
        return "Long";
    }

    @Override
    public CodeBlock getCursorToObjectCodeLine(int index ) {
        return CodeBlock.of( "cursor.get$L( $L )", getCursorMethod(), index );
    }

    /********************************************************************/

    @Override
    public boolean equals(Object o) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        ContractColumnDefinition that = ( ContractColumnDefinition ) o;

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
