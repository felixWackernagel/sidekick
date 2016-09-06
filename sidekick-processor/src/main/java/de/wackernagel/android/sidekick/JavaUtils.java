package de.wackernagel.android.sidekick;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class JavaUtils {

    public static Set<Element> getAbstractMethods( final TypeElement clazz ) {
        final Set<Element> methods = new HashSet<>();
        for( Element element : clazz.getEnclosedElements() ) {
            if( element.getModifiers().contains( Modifier.ABSTRACT ) ) {
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

}
