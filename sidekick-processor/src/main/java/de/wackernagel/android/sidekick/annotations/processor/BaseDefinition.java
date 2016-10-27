package de.wackernagel.android.sidekick.annotations.processor;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;

import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.ForeignKey;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ColumnDefinition;

public abstract class BaseDefinition implements ColumnDefinition {

    protected final Element origin;

    /**
     * @param origin element (nullable)
     */
    protected BaseDefinition( final Element origin ) {
        this.origin = origin;
    }

    /* ** SQLITE ************************************************************************* */

    @Override
    public boolean skipSQLite() {
        return false;
    }

    /* ** ANNOTATIONS ********************************************************************** */

    @Override
    public Unique unique() {
        return origin != null ? origin.getAnnotation( Unique.class ) : null;
    }

    @Override
    public String defaultValue() {
        Default value = origin != null ? origin.getAnnotation( Default.class ) : null;
        if( value == null ) {
            return null;
        } else {
            return value.value();
        }
    }

    @Override
    public NotNull notNull() {
        return origin != null ? origin.getAnnotation(NotNull.class) : null;
    }

    @Override
    public boolean isNotNull() {
        return notNull() != null;
    }

    @Override
    public boolean isForeignKey() {
        return false;
    }

    @Override
    public ForeignKey foreignKey() {
        return origin != null ? origin.getAnnotation(ForeignKey.class) : null;
    }

    /* ** TYPES ********************************************************************** */

    @Override
    public boolean isPrimaryKey() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isByte() {
        return false;
    }

    @Override
    public boolean isContractObjectType() {
        return false;
    }

    @Override
    public boolean isCollectionType() {
        return false;
    }

    /* ** Collections ************************************************************************* */

    @Override
    public TypeName getInstantiableCollectionType() {
        return null;
    }

    @Override
    public TypeName getOriginCollectionElementObjectType() {
        return null;
    }

    @Override
    public TypeName getCollectionElementObjectType() {
        return null;
    }

    /* ** HELPER ************************************************************************* */

    /**
     * Format names like 'orderItem' to 'order_item'.
     */
    public static String formatNameForSQL( final String originName ) {
        final StringBuilder sb = new StringBuilder(originName);
        final int length = sb.length();
        int offset = 0;
        for (int index = 0; index < length; index++) {
            if (index > 0 && Character.isUpperCase(originName.charAt(index))) {
                sb.insert(index + offset, "_");
                offset++;
            }
        }
        return sb.toString().toLowerCase();
    }
}