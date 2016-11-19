package de.wackernagel.android.sidekick.annotations.processor.definitions;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import de.wackernagel.android.sidekick.annotations.ForeignKey;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;

public interface ColumnDefinition {

    String getFieldName();
    String getColumnName();
    String getConstantFieldName();
    TypeName getObjectType();
    boolean isContractObjectType();
    String getSQLiteType();
    boolean isObjectTypeNotPrimitive();
    boolean isCollectionType();
    TypeName getInstantiableCollectionType();
    TypeName getOriginCollectionElementObjectType();
    TypeName getCollectionElementObjectType();
    NotNull notNull();
    boolean isNotNull();
    boolean isPrimaryKey();
    String defaultValue();
    boolean isBoolean();
    boolean isString();
    boolean isByte();
    boolean isForeignKey();
    ForeignKey foreignKey();
    Unique unique();
    boolean skipSQLite();
    String getCursorMethod();
    CodeBlock getCursorToObjectCodeLine( int index );

}
