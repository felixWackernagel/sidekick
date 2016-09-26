package de.wackernagel.android.sidekick.annotations.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
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

    private final Map<TableDefinition, Set<ColumnDefinition>> toGenerate = new HashMap<>();
    private final Map<TableDefinition, Set<ColumnDefinition>> relations = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log.printMessage( NOTE, "Sidekick Annotation Processor" );

        // collect data
        for( Element annotatedElement : roundEnv.getElementsAnnotatedWith(Contract.class) ) {
            final TypeElement annotatedClass = (TypeElement) annotatedElement;
            final TableDefinition tableDefinition = new TableDefinition(
                    typeUtils,
                    elementUtils,
                    log,
                    JavaUtils.getPackageName(elementUtils, annotatedClass),
                    annotatedClass.getSimpleName().toString(),
                    annotatedClass.getAnnotation(Contract.class).authority() );

            final Set<Element> fields = JavaUtils.getAnnotatedFields(annotatedClass, Column.class);
            final Set<ColumnDefinition> columnDefinitions = filterFields(fields);
            toGenerate.put(tableDefinition, columnDefinitions);
        }

        // analyse relations
        for( Map.Entry<TableDefinition, Set<ColumnDefinition>> entry : toGenerate.entrySet() ) {
            for( ColumnDefinition columnDefinition : entry.getValue() ) {
                if( columnDefinition.isCollectionType() ) {
                    if( !isOneManyRelation(columnDefinition.getCollectionElementModelType(), entry.getKey().getObjectType(true)) ) {
                        final String tableOne = entry.getKey().getClassName();
                        final String tableTwo = JavaUtils.getSimpleName( columnDefinition.getCollectionElementType() );
                        final TableDefinition tableDefinition = getOrCreateTableRelation(
                                tableOne, tableTwo, entry.getKey().getPackageName(), entry.getKey().getTableAuthority()
                        );

                        Set<ColumnDefinition> columnDefinitions;
                        if( relations.containsKey( tableDefinition ) ) {
                            columnDefinitions = relations.get( tableDefinition );
                        } else {
                            columnDefinitions = new LinkedHashSet<>();
                        }

                        // primary key
                        columnDefinitions.add( ColumnDefinition.primaryField( typeUtils, elementUtils, log ) );

                        // table one reference
                        columnDefinitions.add( new ColumnDefinition(
                                null, ClassName.bestGuess( entry.getKey().getObjectType(false) ), false, false, false, typeUtils, elementUtils, log ) );

                        // table two reference
                        columnDefinitions.add( new ColumnDefinition(
                            null, columnDefinition.getCollectionElementType(), false, false, false, typeUtils, elementUtils, log ) );

                        relations.put( tableDefinition, columnDefinitions );
                        log.printMessage( NOTE, "Many-Many TableContract: " + tableDefinition.toString() );
                    } else {
                        log.printMessage( NOTE, "Found One-Many Relation" );
                    }
                }
            }
        }

        // integrate relations into definitions
        for( Map.Entry<TableDefinition, Set<ColumnDefinition>> relation : relations.entrySet() ) {
            if( toGenerate.containsKey( relation.getKey() ) ) {
                toGenerate.get( relation.getKey() ).addAll( relation.getValue() );
            } else {
                toGenerate.put( relation.getKey(), relation.getValue() );
            }
        }

        // generate model and contract
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

    private TableDefinition getOrCreateTableRelation( String tableOne, String tableTwo, String packageName, String tableAuthority ) {
        final TableDefinition tableDefinition = new TableDefinition(
                typeUtils,
                elementUtils,
                log,
                packageName,
                tableOne.concat( tableTwo ).concat("Relation"),
                tableAuthority );

        if( toGenerate.containsKey( tableDefinition ) || relations.containsKey( tableDefinition ) ) {
            return tableDefinition;
        }

        final TableDefinition tableAlternativeDefinition = new TableDefinition(
                typeUtils,
                elementUtils,
                log,
                packageName,
                tableTwo.concat( tableOne ).concat("Relation"),
                tableAuthority );

        if( toGenerate.containsKey( tableAlternativeDefinition ) || relations.containsKey( tableAlternativeDefinition ) ) {
            return tableAlternativeDefinition;
        } else {
            return tableDefinition;
        }
    }

    private boolean isOneManyRelation(final TypeName tableModelType, final String columnModelType) {
        for( final Map.Entry<TableDefinition, Set<ColumnDefinition>> entry : toGenerate.entrySet() ) {
            if( entry.getKey().getObjectType( true ).equals( tableModelType.toString() ) ) {
                for( ColumnDefinition columnDefinition : entry.getValue() ) {
                    if( columnDefinition.getObjectType().toString().equals( columnModelType ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                final Element collectionElementType = typeUtils.asElement( generics.iterator().next() );
                if( collectionElementType.getAnnotation( Contract.class ) != null ) {
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
