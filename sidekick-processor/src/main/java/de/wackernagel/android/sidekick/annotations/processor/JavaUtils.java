package de.wackernagel.android.sidekick.annotations.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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
        if( field instanceof PrimitiveType || field instanceof ArrayType ) {
            return TypeName.get( field );
        } else if( field instanceof DeclaredType ) {
            return ClassName.get(( TypeElement ) (( DeclaredType ) field).asElement() );
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

    /**
     * @param clazz to inspect
     * @param elements utils
     * @param types utils
     * @return list of all member field of class and his inheriting classes
     */
    public static Set<Element> getMemberFields(final TypeElement clazz, final Elements elements, final Types types) {
        final Set<Element> memberFields = new LinkedHashSet<>();
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getKind() == ElementKind.FIELD ) {
                memberFields.add(element);
            }
        }

        final TypeMirror objectClass = elements.getTypeElement( Object.class.getName() ).asType();
        if( types.isSameType( objectClass, clazz.getSuperclass() ) ) {
            return memberFields;
        } else {
            final TypeElement superClass = elements.getTypeElement( clazz.getSuperclass().toString() );
            final Set<Element> superClassMemberFields = getMemberFields( superClass, elements, types);
            if( !superClassMemberFields.isEmpty() ) {
                memberFields.addAll(superClassMemberFields);
            }
            return memberFields;
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

    /* FORMATTING ************************************************************** */

    public static String getSimpleName( final TypeName classType ) {
        if( classType == null ) {
            return null;
        }

        final String fullyClassName = classType.toString();

        final int index = fullyClassName.lastIndexOf('.');
        if( index >= 0 ) {
            return fullyClassName.substring( index + 1 );
        }
        return fullyClassName;
    }

    public static String toVariableCase( final String str ) {
        if( str == null || str.length() == 0 ) {
            return str;
        }
        final String firstLetter = Character.toString(
                Character.toLowerCase( str.charAt( 0 ) ) );

        if( str.length() > 1 ) {
            return firstLetter.concat( str.substring( 1 ) );
        }
        return firstLetter;
    }

    public static String toTitleCase( final String str ) {
        if( str == null || str.length() == 0 ) {
            return  str;
        }
        final String firstLetter = Character.toString(
                Character.toTitleCase(
                        str.charAt( 0 ) ) );

        if( str.length() > 1 ) {
            return firstLetter.concat( str.substring( 1 ) );
        }
        return firstLetter;
    }
}
