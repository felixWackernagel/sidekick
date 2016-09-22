package de.wackernagel.android.sidekick.annotations.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
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

import static javax.tools.Diagnostic.Kind.NOTE;

@AutoService(Processor.class)
public class SidekickProcessor extends AbstractProcessor {

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

    private Map<TableDefinition, Set<ColumnDefinition>> toGenerate = new HashMap<TableDefinition, Set<ColumnDefinition>>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for( Element annotatedElement : roundEnv.getElementsAnnotatedWith( Contract.class ) ) {
            final TypeElement annotatedClass = (TypeElement) annotatedElement;
            final TableDefinition tableDefinition = new TableDefinition(
                    typeUtils,
                    elementUtils,
                    log,
                    JavaUtils.getPackageName(elementUtils, annotatedClass),
                    annotatedClass.getSimpleName().toString(),
                    annotatedClass.getAnnotation(Contract.class).authority() );
            log.printMessage(NOTE, tableDefinition.toString() );

            final Set<Element> fields = JavaUtils.getAnnotatedFields(annotatedClass, Column.class);
            final Set<ColumnDefinition> columnDefinitions = filterFields(fields);
            toGenerate.put( tableDefinition, columnDefinitions );
        }

        for( Map.Entry<TableDefinition, Set<ColumnDefinition>> entry : toGenerate.entrySet() ) {
            final TableDefinition tableDefinition = entry.getKey();
            final Set<ColumnDefinition> columnDefinitions = entry.getValue();

            final ContractGenerator generatedContractClass = new ContractGenerator( tableDefinition, columnDefinitions );
            if( generatedContractClass.writeClass( tableDefinition.getPackageName(), processingEnv.getFiler() ) ) {
                log.printMessage(NOTE, "Contract for " + tableDefinition.getPackageName() + "." + tableDefinition.getClassName() + " generated." );
            }

            final ModelGenerator generatedModelClass = new ModelGenerator( tableDefinition, columnDefinitions );
            if( generatedModelClass.writeClass(tableDefinition.getPackageName(), processingEnv.getFiler()) ) {
                log.printMessage(NOTE, "Model for " + tableDefinition.getPackageName() + "." + tableDefinition.getClassName() + " generated." );
            }
        }
        return true;
    }

    private Set<ColumnDefinition> filterFields( Set<Element> fields ) {
        final Set<ColumnDefinition> annotatedFields = new LinkedHashSet<>();
        annotatedFields.add( ColumnDefinition.primaryField( typeUtils, elementUtils, log ) );

        for( Element field : fields ) {
            final TypeName type = JavaUtils.getType( field.asType() );
            if( type.isPrimitive() ) {
                // boolean, byte, double, float, int, long, short
                annotatedFields.add( new ColumnDefinition( field, type, true, false, false, typeUtils, elementUtils, log ) );
            } else if( type.toString().equals( "byte[]" ) ) {
                // byte[]
                annotatedFields.add( new ColumnDefinition( field, type, false, true, false, typeUtils, elementUtils, log ) );
            } else if( String.class.getName().equals(type.toString() ) ) {
                // String
                annotatedFields.add( new ColumnDefinition(field, type, false, false, true, typeUtils, elementUtils, log ) );
            } else if( field.asType() instanceof DeclaredType && typeUtils.asElement( field.asType() ).getAnnotation( Contract.class ) != null ) {
                // Class with @Contract()
                annotatedFields.add( new ColumnDefinition(field, type, false, false, false, typeUtils, elementUtils, log ) );
            } else if( JavaUtils.isCollectionType(field, elementUtils, typeUtils) ) {
                // Collection
                final Set<TypeMirror> generics = JavaUtils.getGenericTypes(field);
                if( generics.isEmpty() ) {
                    log.printMessage(Diagnostic.Kind.NOTE, "Skip FIELD of type " + type.toString() + " because no generic type found." );
                    continue;
                }
                final Element collectionType = typeUtils.asElement( generics.iterator().next() );
                if( collectionType.getAnnotation( Contract.class ) != null ) {
                    // Set<@Contract>, List<@Contract>
                    annotatedFields.add(new ColumnDefinition(field, ParameterizedTypeName.get(field.asType()), false, false, false, true, typeUtils, elementUtils, log));
                } else {
                    // TODO Collection<Primitive>
                }
            } else {
                log.printMessage(Diagnostic.Kind.NOTE, "Skip unsupported FIELD of type " + type.toString() );
            }
        }
        return annotatedFields;
    }

}
