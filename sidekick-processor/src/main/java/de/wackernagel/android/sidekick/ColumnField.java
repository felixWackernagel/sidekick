package de.wackernagel.android.sidekick;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class ColumnField {

    private String name;
    private String constantName;
    private ClassName type;

    public ColumnField( final Element field ) {
        name = columnName( field );
        constantName = "COLUMN" + ( name.startsWith( "_" ) ? "" : "_" ) + name.toUpperCase();

        DeclaredType declaredFieldType = (DeclaredType) field.asType();
        TypeElement fieldTypeElement = (TypeElement) declaredFieldType.asElement();
        type = ClassName.get(fieldTypeElement);
    }

    public ColumnField( final String constantName, final String name, final Class<?> type ) {
        this.name = name;
        this.constantName = constantName;
        this.type = ClassName.get( type );
    }

    public String getName() {
        return name;
    }

    public String getConstantName() {
        return constantName;
    }

    public ClassName getType() {
        return type;
    }

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
