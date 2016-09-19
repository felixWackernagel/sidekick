package de.wackernagel.android.sidekick.annotations.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
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

public class JavaUtils {

    public static String getPackageName( final Elements elementUtils, final TypeElement clazz ) {
        final PackageElement pkg = elementUtils.getPackageOf( clazz );
        if (pkg.isUnnamed()) {
            return "";
        }
        return pkg.getQualifiedName().toString();
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

    public static Set<Element> getAnnotatedFields(TypeElement clazz, Class<? extends Annotation> annotation) {
        final Set<Element> annotatedFields = new LinkedHashSet<>();
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getKind() == ElementKind.FIELD && element.getAnnotation( annotation ) != null ) {
                annotatedFields.add( element );
            }
        }
        return annotatedFields;
    }

    public static Set<TypeMirror> getGenericTypes(Element field ) {
        final Set<TypeMirror> annotatedFields = new LinkedHashSet<>();
        if( field.asType() instanceof DeclaredType ) {
            for( TypeMirror gen : ((DeclaredType) field.asType()).getTypeArguments() ) {
                annotatedFields.add( gen );
            }
        }
        return annotatedFields;
    }
}
