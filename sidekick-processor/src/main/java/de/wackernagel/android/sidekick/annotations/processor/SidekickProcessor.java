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
import java.util.StringJoiner;

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

import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ContractCollectionColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ContractColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.PrimaryColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.PrimitiveColumnDefinition;

import static javax.tools.Diagnostic.Kind.NOTE;

@AutoService(Processor.class)
public class SidekickProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Messager logger;

    private final Map<TableDefinition, Set<ColumnDefinition>> toGenerate = new HashMap<>();
    private final Map<TableDefinition, Set<ColumnDefinition>> relations = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment environment) {
        super.init(environment);
        elementUtils = environment.getElementUtils();
        typeUtils = environment.getTypeUtils();
        logger = environment.getMessager();
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        if( annotations.isEmpty() ) {
            return true;
        }
        logger.printMessage(NOTE, "Process Sidekick Annotations (" + asString(annotations) + ")");

        // collect data
        for( Element annotatedElement : environment.getElementsAnnotatedWith(Contract.class) ) {
            final TypeElement annotatedClass = (TypeElement) annotatedElement;
            final TableDefinition tableDefinition = new TableDefinition(
                    JavaUtils.getPackageName(elementUtils, annotatedClass),
                    annotatedClass.getSimpleName().toString(),
                    annotatedClass.getAnnotation(Contract.class).authority() );

            final Set<Element> fields = JavaUtils.getMemberFields(annotatedClass, elementUtils, typeUtils);
            final Set<ColumnDefinition> columnDefinitions = convertToDefinitions(fields);
            toGenerate.put(tableDefinition, columnDefinitions);
        }

        // analyse relations
        for( Map.Entry<TableDefinition, Set<ColumnDefinition>> entry : toGenerate.entrySet() ) {
            for( ColumnDefinition columnDefinition : entry.getValue() ) {
                if( columnDefinition.isCollectionType() ) {
                    if( !isOneManyRelation(columnDefinition.getCollectionElementObjectType(), entry.getKey().getObjectType(true, true)) ) {
                        final String tableOne = entry.getKey().getClassName();
                        final String tableTwo = JavaUtils.getSimpleName( columnDefinition.getOriginCollectionElementObjectType() );
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
                        columnDefinitions.add( new PrimaryColumnDefinition() );

                        // table one reference
                        columnDefinitions.add( new ContractColumnDefinition(
                                ClassName.bestGuess(entry.getKey().getObjectType(true, false))) );

                        // table two reference
                        columnDefinitions.add( new ContractColumnDefinition(
                                columnDefinition.getOriginCollectionElementObjectType()));

                        tableDefinition.setManyToManyRelation( true );
                        relations.put(tableDefinition, columnDefinitions);
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
                logger.printMessage(NOTE, "Contract for " + tableDefinition.getPackageName() + "." + tableDefinition.getClassName() + " generated.");
            }

            // skip auto generated many-many relation models
            if( tableDefinition.isManyToManyRelation() && columnDefinitions.size() == 3 ) {
                continue;
            }

            final ModelGenerator generatedModelClass = new ModelGenerator( tableDefinition, columnDefinitions );
            if( generatedModelClass.writeClass(tableDefinition.getPackageName(), processingEnv.getFiler()) ) {
                logger.printMessage(NOTE, "Model for " + tableDefinition.getPackageName() + "." + tableDefinition.getClassName() + " generated.");
            }
        }

        // all Contract annotations processed
        return true;
    }

    private TableDefinition getOrCreateTableRelation( String tableOne, String tableTwo, String packageName, String tableAuthority ) {
        final TableDefinition tableDefinition = new TableDefinition(
                packageName,
                tableOne.concat( tableTwo ).concat("Relation"),
                tableAuthority );

        if( toGenerate.containsKey( tableDefinition ) || relations.containsKey( tableDefinition ) ) {
            return tableDefinition;
        }

        final TableDefinition tableAlternativeDefinition = new TableDefinition(
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
            if( entry.getKey().getObjectType( true, true ).equals(tableModelType.toString()) ) {
                for( ColumnDefinition columnDefinition : entry.getValue() ) {
                    if( columnDefinition.getObjectType().toString().equals( columnModelType ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<ColumnDefinition> convertToDefinitions(Set<Element> fields) {
        final Set<ColumnDefinition> annotatedFields = new LinkedHashSet<>();

        final Element idField = findElement( TypeName.LONG, "id", fields );
        annotatedFields.add( new PrimaryColumnDefinition( idField ) );

        for( Element field : fields ) {
            final TypeName type = JavaUtils.getType( field.asType() );
            if( JavaUtils.isPrimitiveOfContentProvider(type) ) {
                // boolean, byte, double, float, int, long, short, String, byte[], Boolean, Byte, Double, Float, Integer, Long, Short
                annotatedFields.add( new PrimitiveColumnDefinition( field, type ) );
            } else if( JavaUtils.isCollectionType(field, elementUtils, typeUtils) ) {
                // Collection
                final Set<TypeMirror> generics = JavaUtils.getGenericTypes(field);
                if( generics.isEmpty() ) {
                    logger.printMessage(Diagnostic.Kind.NOTE, "Skip FIELD of type " + type.toString() + " because no generic type found.");
                    continue;
                }
                final Element collectionElementType = typeUtils.asElement( generics.iterator().next() );
                if( collectionElementType.getAnnotation( Contract.class ) != null ) {
                    // Set<@Contract>, List<@Contract>
                    annotatedFields.add( new ContractCollectionColumnDefinition(field, ParameterizedTypeName.get(field.asType()), typeUtils, elementUtils));
                } else {
                    // TODO Collection<Primitive>
                }
            } else if( field.asType() instanceof DeclaredType && typeUtils.asElement( field.asType() ).getAnnotation( Contract.class ) != null ) {
                // Class with @Contract()
                annotatedFields.add( new ContractColumnDefinition(field, type) );
            } else {
                logger.printMessage(Diagnostic.Kind.NOTE, "Skip unsupported FIELD of type " + type.toString());
            }
        }
        return annotatedFields;
    }

    private Element findElement( final TypeName type, final String fieldName, final Set<Element> fields ) {
        if( type == null || fieldName == null || fieldName.length() == 0 || fields.isEmpty() ) {
            return null;
        }
        Element field = null;
        for( Element fieldElement : fields ) {
            if( fieldElement.getSimpleName().toString().equals( fieldName ) &&
                    JavaUtils.getType( fieldElement.asType() ).equals( type ) ) {
                field = fieldElement;
                break;
            }
        }
        if( field != null ) {
            fields.remove( field );
        }
        return field;
    }

    private static String asString( Set<? extends TypeElement> typeElements ) {
        final StringJoiner joiner = new StringJoiner( "," );
        for( TypeElement typeElement : typeElements ) {
            joiner.add(typeElement.getSimpleName().toString());
        }
        return joiner.toString();
    }
}
