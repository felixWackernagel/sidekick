package de.wackernagel.android.sidekick.annotations.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeName;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@AutoService(Processor.class)
public class ContractProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Messager log;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        log = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new HashSet<>();
        annotations.add( Contract.class.getCanonicalName() );
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for( Element annotatedElement : roundEnv.getElementsAnnotatedWith( Contract.class ) ) {
            final TypeElement annotatedClass = (TypeElement) annotatedElement;
            final String authority = annotatedClass.getAnnotation(Contract.class).authority();
            final String className = annotatedClass.getSimpleName().toString();
            final String packageName = JavaUtils.getPackageName(elementUtils, annotatedClass);

            if (packageName == null) {
                log.printMessage(Diagnostic.Kind.WARNING, "No package for " + className + ". SKIP");
                continue;
            }

            final Set<Element> fields = JavaUtils.getAnnotatedFields(annotatedClass, Column.class);
            final Set<ColumnField> memberFields = filterFields(fields);

            final ContractGenerator generatedContractClass = new ContractGenerator( className, memberFields, authority );
            generatedContractClass.writeClass( packageName, className, log, processingEnv.getFiler() );

            final ModelGenerator generatedModelClass = new ModelGenerator( className, memberFields );
            generatedModelClass.writeClass(packageName, className, log, processingEnv.getFiler());
        }
        return true;
    }

    private Set<ColumnField> filterFields( Set<Element> fields ) {
        final Set<ColumnField> annotatedFields = new LinkedHashSet<>();
        annotatedFields.add( ColumnField.primaryField() );

        for( Element field : fields ) {
            final TypeName type = JavaUtils.getType( field.asType() );
            log.printMessage(Diagnostic.Kind.NOTE, "Filter FIELD of type " + type.toString() );
            if( type.isPrimitive() ) {
                // boolean, byte, double, float, int, long, short
                annotatedFields.add( new ColumnField( field, type, true, false, false, typeUtils, elementUtils ) );
            } else if( type.toString().equals( "byte[]" ) ) {
                // byte[]
                annotatedFields.add( new ColumnField( field, type, false, true, false, typeUtils, elementUtils ) );
            } else if( String.class.getName().equals(type.toString() ) ) {
                // String
                annotatedFields.add( new ColumnField(field, type, false, false, true, typeUtils, elementUtils ) );
            } else if( field.asType() instanceof DeclaredType && typeUtils.asElement( field.asType() ).getAnnotation( Contract.class ) != null ) {
                // Class with @Contract()
                annotatedFields.add( new ColumnField(field, type, false, false, false, typeUtils, elementUtils ) );
            } else if( JavaUtils.isCollectionType(field, elementUtils, typeUtils) ) {
                // Collection
                final Set<TypeMirror> generic = JavaUtils.getGenericTypes(field, elementUtils, typeUtils);
                if( generic.size() == 1 &&
                    typeUtils.asElement( generic.iterator().next() ).getAnnotation( Contract.class ) != null ) {
                    // Set<@Contract>, List<@Contract>
                } else {
                    log.printMessage(Diagnostic.Kind.NOTE, "Skip FIELD because collection has more then 1 generic type or type is no Contract." );
                }
            } else {
                log.printMessage(Diagnostic.Kind.NOTE, "Skip FIELD because type is unsupported." );
            }
        }
        return annotatedFields;
    }

}
