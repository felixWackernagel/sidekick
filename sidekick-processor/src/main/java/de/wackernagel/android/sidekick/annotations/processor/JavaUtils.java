package de.wackernagel.android.sidekick.annotations.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
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
        if( field instanceof PrimitiveType || field instanceof ArrayType ) {
            return TypeName.get( field );
        } else if( field instanceof DeclaredType ) {
            return ClassName.get(( TypeElement ) (( DeclaredType ) field).asElement() );
        } else {
            throw new IllegalArgumentException( "Unknow field type: " + field.toString() );
        }
    }

    public static String getSimpleName( final TypeName classType ) {
        if( classType == null ) {
            return null;
        }

        final String name = classType.toString();
        final int index = name.lastIndexOf( '.' );
        if( index >= 0 ) {
            return name.substring( index + 1 );
        }
        return name;
    }

    public static String toVariableCase( final String name ) {
        if( name == null || name.length() == 0 ) {
            return name;
        }
        return Character.toLowerCase( name.charAt( 0 ) ) + name.substring( 1 );
    }

    public static boolean isCollectionType( final Element field, final Elements elementUtils, final Types typeUtils ) {
        return typeUtils.isAssignable(  // a is subtype of b
                field.asType(), // i.e. List<?>
                typeUtils.getDeclaredType( // Collection<?>
                        elementUtils.getTypeElement( Collection.class.getName() ), // type
                        typeUtils.getWildcardType(null, null) ) ); // wildcard for generics
    }

    public static boolean isListType( final Element field, final Elements elementUtils, final Types typeUtils ) {
        return typeUtils.isAssignable(  // a is subtype of b
                field.asType(), // i.e. List<?>
                typeUtils.getDeclaredType( // List<?>
                        elementUtils.getTypeElement( List.class.getName() ), // type
                        typeUtils.getWildcardType(null, null) ) ); // wildcard for generics
    }

    public static boolean isSetType( final Element field, final Elements elementUtils, final Types typeUtils ) {
        return typeUtils.isAssignable(  // a is subtype of b
                field.asType(), // i.e. List<?>
                typeUtils.getDeclaredType( // Set<?>
                        elementUtils.getTypeElement( Set.class.getName() ), // type
                        typeUtils.getWildcardType(null, null) ) ); // wildcard for generics
    }

    public static Set<Element> getAnnotatedFields(final TypeElement clazz, final Class<? extends Annotation> annotation, final Elements elements,  final Types types, final Messager log) {
        final Set<Element> annotatedFields = new LinkedHashSet<>();
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getKind() == ElementKind.FIELD /*&& element.getAnnotation( annotation ) != null*/ ) {
                annotatedFields.add( element );
            }
        }
        if( types.isSameType(elements.getTypeElement(Object.class.getName()).asType(), clazz.getSuperclass() ) ) {
            return annotatedFields;
        } else {
            final Set<Element> inheritingFields = getAnnotatedFields( elements.getTypeElement( clazz.getSuperclass().toString() ), annotation, elements, types, log );
            if( !inheritingFields.isEmpty() ) {
                annotatedFields.addAll( inheritingFields );
            }
            return annotatedFields;
        }
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

    public static boolean isPrimitiveOfContentProvider( final TypeName typeName ) {
        final String type = typeName.toString();
        final List<String> primitives = Arrays.asList(
            short.class.getName(),
            long.class.getName(),
            double.class.getName(),
            int.class.getName(),
            String.class.getName(),
            boolean.class.getName(),
            float.class.getName(),
            byte.class.getName(),
            "byte[]",

            Short.class.getName(),
            Long.class.getName(),
            Double.class.getName(),
            Integer.class.getName(),
            Boolean.class.getName(),
            Float.class.getName(),
            Byte.class.getName()
        );
        return primitives.contains( type );
    }
}
