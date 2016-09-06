package de.wackernagel.android.sidekick;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import de.wackernagel.android.sidekick.annotations.Contract;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
public class ContractProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Contract.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Contract.class)) {
            try {
                // annotation is only allowed on classes, so we can safely cast here
                final TypeElement annotatedClass = (TypeElement) annotatedElement;
                final String className = annotatedClass.getSimpleName().toString();
                final String packageName = getPackageName( processingEnv.getElementUtils(), annotatedClass );

                final Set<String> constants = new HashSet<>();
                for( Element element : annotatedClass.getEnclosedElements() ) {
                    if( element.getModifiers().contains( Modifier.ABSTRACT ) ) {
                        constants.add( element.getSimpleName().toString() );
                    }
                }

                final TypeSpec generatedClass = generateClass( className, constants );

                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generate contract for " + packageName + "." + className );

                if( packageName != null ) {
                    JavaFile javaFile = JavaFile.builder(packageName, generatedClass).build();
                    javaFile.writeTo( processingEnv.getFiler() );
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Package- and/or Classname are null.");
                }
            } catch( IOException e ) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "File wasn't generated.");
            }

        }

        return true;
    }

    private static TypeSpec generateClass( final String className, final Set<String> constants ) {
         TypeSpec.Builder b = classBuilder(className + "Contract")
                .addModifiers(PUBLIC, FINAL)
                .addJavadoc("Generated by Sidekick at " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH).format(new Date()) + "\n")
                .addField(constant("TABLE", tableName( className ) ) )
                .addField(constant("COLUMN_ID", "_id" ) );
        for( String method : constants ) {
            String name = columnName( method );
            b.addField( constant( "COLUMN_" + name.toUpperCase(), name ) );
        }
        return b.build();
    }

    private static String columnName( final String methodName ) {
        String name = methodName;
        if( name.startsWith( "get" ) ) {
            name = methodName.substring(3);
        }

        final StringBuilder sb = new StringBuilder( name );
        final int length = sb.length();
        int offset = 0;
        for( int index = 0; index < length; index++ ) {
            if( index > 0 && Character.isUpperCase( name.charAt( index ) ) ) {
                sb.insert( index + offset, "_" );
                offset++;
            }
        }
        return sb.toString().toLowerCase();
    }

    private static String tableName( final String className ) {
        final String name = className.toLowerCase();
        if( name.endsWith( "s" ) )
            return name;
        return name + "s";
    }

    private static FieldSpec constant( final String name, final String value ) {
        return FieldSpec.builder( String.class, name )
                .addModifiers( Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer( "$S", value )
                .build();
    }

    static String getPackageName( Elements elementUtils, TypeElement type) {
        PackageElement pkg = elementUtils.getPackageOf(type);
        if (pkg.isUnnamed()) {
            return null;
        }
        return pkg.getQualifiedName().toString();
    }

}
