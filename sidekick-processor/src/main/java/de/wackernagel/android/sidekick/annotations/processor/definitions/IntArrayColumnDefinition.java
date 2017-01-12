package de.wackernagel.android.sidekick.annotations.processor.definitions;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;

import de.wackernagel.android.sidekick.annotations.processor.JavaUtils;

public class IntArrayColumnDefinition extends BaseDefinition {

    private final TypeName objectType;
    private final String fieldName;
    private final String columnName;
    private final String constantFieldName;

    public IntArrayColumnDefinition(Element element, TypeName type) {
        super(element);
        objectType = type;
        fieldName = element != null ? element.getSimpleName().toString() : JavaUtils.toVariableCase(JavaUtils.getSimpleName(type));
        columnName = formatNameForSQL(fieldName);
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
        return objectType;
    }

    @Override
    public String getSQLiteType() {
        return "BLOB";
    }

    @Override
    public boolean isObjectTypeNotPrimitive() {
        return true;
    }

    @Override
    public String getCursorMethod() {
        return "Blob";
    }

    @Override
    public CodeBlock getCursorToObjectCodeLine(int index) {
        return CodeBlock.of( "$T.toIntArray( cursor.get$L( $L ) )",
                ClassName.get( "de.wackernagel.android.sidekick.converters", "IntArrayConverter" ),
                getCursorMethod(),
                index );
    }
}
