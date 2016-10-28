package de.wackernagel.android.sidekick.annotations.processor.definitions;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;

import de.wackernagel.android.sidekick.annotations.Unique;
import de.wackernagel.android.sidekick.annotations.processor.BaseDefinition;

public class PrimaryColumnDefinition extends BaseDefinition {

    private final String fieldName = "id";
    private final TypeName objectType = TypeName.LONG;

    /**
     * Constructor for Many-To-Many Relations.
     */
    public PrimaryColumnDefinition() {
        this(null);
    }

    /**
     * Constructor for already defined member field (long id)
     * @param origin java member field
     */
    public PrimaryColumnDefinition( final Element origin ) {
        super(origin);
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getColumnName() {
        return "_id";
    }

    @Override
    public String getConstantFieldName() {
        return "COLUMN_ID";
    }

    @Override
    public TypeName getObjectType() {
        return objectType;
    }

    @Override
    public String getSQLiteType() {
        return "INTEGER";
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }

    @Override
    public String getCursorMethod() {
        return "Long";
    }

    @Override
    public boolean isObjectTypeNotPrimitive() {
        return false;
    }

    /**
     * Primary keys are unique but unique isn't primary.
     *
     * @return always null
     */
    @Override
    public Unique unique() {
        return null;
    }

    /********************************************************************/

    @Override
    public boolean equals(Object o) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        PrimaryColumnDefinition that = ( PrimaryColumnDefinition ) o;

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
