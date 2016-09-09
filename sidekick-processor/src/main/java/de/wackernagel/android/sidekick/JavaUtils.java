package de.wackernagel.android.sidekick;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import de.wackernagel.android.sidekick.annotations.Column;

public class JavaUtils {

    public static String getPackageName( final Elements elementUtils, final TypeElement clazz ) {
        final PackageElement pkg = elementUtils.getPackageOf( clazz );
        if (pkg.isUnnamed()) {
            return null;
        }
        return pkg.getQualifiedName().toString();
    }

    public static Set<ColumnField> getAnnotatedColumnFields(final TypeElement clazz, Types typeUtils, Elements elementUtils) {
        final Set<ColumnField> annotatedFields = new LinkedHashSet<>();
        annotatedFields.add( new ColumnField( "COLUMN_ID", "id", "_id", long.class ) );
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getKind() == ElementKind.FIELD && element.getAnnotation( Column.class ) != null ) {
                annotatedFields.add( new ColumnField( element, typeUtils, elementUtils ) );
            }
        }
        return annotatedFields;
    }

    public static TypeName getType( TypeMirror field ) {
        if( field instanceof PrimitiveType ) {
            return TypeName.get( field );
        } else if( field instanceof DeclaredType ) {
            return ClassName.get(( TypeElement ) (( DeclaredType ) field).asElement() );
        } else if( field instanceof ArrayType ) {
            return TypeName.get( field );
        } else {
            throw new IllegalArgumentException( "Unknow field type: " + field.toString() );
        }
    }

    public static boolean isCollectionType( final Element field, final Elements elementUtils, final Types typeUtils ) {
        return typeUtils.isAssignable(  // a is subtype of b
                field.asType(), // i.e. List<?>
                typeUtils.getDeclaredType( // Collection<?>
                        elementUtils.getTypeElement( Collection.class.getName() ), // type
                        typeUtils.getWildcardType(null, null) ) ); // wildcard for generics
    }
}
