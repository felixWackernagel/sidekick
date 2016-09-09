package de.wackernagel.android.sidekick;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import de.wackernagel.android.sidekick.annotations.Column;

public class JavaUtils {

    public static Set<Element> getAbstractMethods( final TypeElement clazz ) {
        final Set<Element> methods = new HashSet<>();
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getKind() == ElementKind.METHOD && element.getModifiers().contains( Modifier.ABSTRACT ) ) {
                methods.add( element );
            }
        }
        return methods;
    }

    public static String getPackageName( final Elements elementUtils, final TypeElement clazz ) {
        final PackageElement pkg = elementUtils.getPackageOf( clazz );
        if (pkg.isUnnamed()) {
            return null;
        }
        return pkg.getQualifiedName().toString();
    }

    public static Set<ColumnField> getAnnotatedFields( TypeElement clazz, Class<? extends Annotation> annotation) {
        final Set<ColumnField> annotatedFields = new HashSet<>();
        annotatedFields.add( new ColumnField( "COLUMN_ID", "_id", Integer.class ));
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getKind() == ElementKind.FIELD && element.getAnnotation( annotation ) != null ) {
                annotatedFields.add( new ColumnField( element ) );
            }
        }
        return annotatedFields;
    }
}
