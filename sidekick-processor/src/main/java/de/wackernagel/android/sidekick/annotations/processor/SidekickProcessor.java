package de.wackernagel.android.sidekick.annotations.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
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

import de.wackernagel.android.sidekick.annotations.ConflictClause;
import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.Unique;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ContractCollectionColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ContractColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.PrimaryColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.PrimitiveCollectionColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.PrimitiveColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition;
import de.wackernagel.android.sidekick.annotations.processor.generators.ContractGenerator;
import de.wackernagel.android.sidekick.annotations.processor.generators.ModelGenerator;

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

    // TODO escape all tables and columns with ""
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        if( annotations.isEmpty() ) {
            return true;
        }
        logger.printMessage(NOTE, "Process Sidekick Annotations (" + asString(annotations) + ")");

        // collect data
        for( Element annotatedElement : environment.getElementsAnnotatedWith(Contract.class) ) {
            final TypeElement annotatedClass = (TypeElement) annotatedElement;
            final de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition tableDefinition = new TableDefinition(
                    JavaUtils.getPackageName(elementUtils, annotatedClass),
                    annotatedClass.getSimpleName().toString(),
                    annotatedClass.getAnnotation(Contract.class).authority(),
                    annotatedClass.getAnnotation(Contract.class).model() );

            final Set<Element> fields = JavaUtils.getMemberFields(annotatedClass, elementUtils, typeUtils);
            final Set<ColumnDefinition> columnDefinitions = convertToDefinitions(fields);
            toGenerate.put(tableDefinition, columnDefinitions);
        }

        // analyse relations
        for( Map.Entry<TableDefinition, Set<ColumnDefinition>> entry : toGenerate.entrySet() ) {
            for( ColumnDefinition columnDefinition : entry.getValue() ) {
                if( columnDefinition.isCollectionType() ) {
                    if( columnDefinition instanceof PrimitiveCollectionColumnDefinition ) {
                        createOneManyPrimitiveRelation(entry.getKey(), columnDefinition);
                    } else if( !isOneManyRelation(columnDefinition.getCollectionElementObjectType(), entry.getKey().getObjectType(true, true)) ) {
                        createManyManyContractRelation(entry, columnDefinition);
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

            final ContractGenerator generatedContractClass = new ContractGenerator( tableDefinition, columnDefinitions, logger );
            if( generatedContractClass.writeClass( tableDefinition.getPackageName(), processingEnv.getFiler() ) ) {
                logger.printMessage(NOTE, "Sidekick: " + tableDefinition.getPackageName() + "." + tableDefinition.getClassName() + "Contract generated.");
            }

            if( tableDefinition.generateModel() ) {
                final ModelGenerator generatedModelClass = new ModelGenerator( tableDefinition, columnDefinitions );
                if( generatedModelClass.writeClass(tableDefinition.getPackageName(), processingEnv.getFiler()) ) {
                    logger.printMessage(NOTE, "Sidekick: " + tableDefinition.getPackageName() + "." + tableDefinition.getClassName() + "Model generated.");
                }
            }
        }

        // all Contract annotations processed
        return true;
    }

    private void createManyManyContractRelation(Map.Entry<TableDefinition, Set<ColumnDefinition>> entry, ColumnDefinition columnDefinition) {
        final String tableOne = entry.getKey().getClassName();
        final String tableTwo = JavaUtils.getSimpleName(columnDefinition.getOriginCollectionElementObjectType());
        final TableDefinition manyManyTableRelation = getOrCreateTableRelation(
                tableOne, tableTwo, entry.getKey().getPackageName(), entry.getKey().getTableAuthority(), entry.getKey().generateModel()
        );

        Set<ColumnDefinition> columns;
        if( relations.containsKey( manyManyTableRelation ) ) {
            columns = relations.get( manyManyTableRelation );
        } else {
            columns = new LinkedHashSet<>();
        }

        // primary key
        columns.add(new PrimaryColumnDefinition());
        // table one reference
        columns.add(new ContractColumnDefinition(
                        ClassName.bestGuess(entry.getKey().getObjectType(true, false)))
        );
        // table two reference
        columns.add(new ContractColumnDefinition(
                        columnDefinition.getOriginCollectionElementObjectType())
        );
        relations.put(manyManyTableRelation, columns);
    }

    private void createOneManyPrimitiveRelation(TableDefinition parentTable, ColumnDefinition column) {
        final TableDefinition oneManyPrimitiveRelation = new TableDefinition(
                parentTable.getPackageName(),
                parentTable.getClassName().concat(JavaUtils.toTitleCase(column.getFieldName())),
                parentTable.getTableAuthority(),
                parentTable.generateModel()
        );
        final Set<ColumnDefinition> columns = new LinkedHashSet<>();
        final Unique parentAndOrder = createUnique( 1, ConflictClause.NONE );
        // id
        columns.add( new PrimaryColumnDefinition() );
        // parent foreign key
        columns.add( new ContractColumnDefinition(
                ClassName.bestGuess(parentTable.getObjectType(true, false) ) ) {
                         @Override
                         public Unique unique() {
                             return parentAndOrder;
                         }
                     }
        );
        // position in list
        columns.add(new PrimitiveColumnDefinition(
                        TypeName.INT,
                        "position") {
                        @Override
                        public Unique unique() {
                            return parentAndOrder;
                        }
                    }
        );
        // value
        columns.add(new PrimitiveColumnDefinition(
                        column.getCollectionElementObjectType(),
                        column.getFieldName())
        );
        relations.put(oneManyPrimitiveRelation, columns);
    }

    private Unique createUnique( final int group, final ConflictClause conflictClause ) {
        return new Unique() {
            @Override
            public int group() {
                return group;
            }

            @Override
            public ConflictClause onConflict() {
                return conflictClause;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Unique.class;
            }
        };
    }

    private TableDefinition getOrCreateTableRelation( String tableOne, String tableTwo, String packageName, String tableAuthority, boolean generateModel ) {
        final TableDefinition tableDefinition = new TableDefinition(
                packageName,
                tableOne.concat( tableTwo ).concat("Relation"),
                tableAuthority,
                generateModel );

        if( toGenerate.containsKey( tableDefinition ) || relations.containsKey( tableDefinition ) ) {
            return tableDefinition;
        }

        final TableDefinition tableAlternativeDefinition = new TableDefinition(
                packageName,
                tableTwo.concat( tableOne ).concat("Relation"),
                tableAuthority,
                generateModel );

        if( toGenerate.containsKey( tableAlternativeDefinition ) || relations.containsKey( tableAlternativeDefinition ) ) {
            return tableAlternativeDefinition;
        } else {
            return tableDefinition;
        }
    }

    private boolean isOneManyRelation(final TypeName tableModelType, final String columnModelType) {
        for( final Map.Entry<de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition, Set<ColumnDefinition>> entry : toGenerate.entrySet() ) {
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
                } else if( JavaUtils.isPrimitiveOfContentProvider( JavaUtils.getType( collectionElementType.asType() ) ) ) {
                    // Set<Primitive>, List<Primitive>
                    annotatedFields.add( new PrimitiveCollectionColumnDefinition(field, ParameterizedTypeName.get(field.asType()), typeUtils, elementUtils));
                } else {
                    logger.printMessage(Diagnostic.Kind.ERROR,
                            "The type '" + collectionElementType.getSimpleName().toString() + "' is not support in collections!" );
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
        final StringBuilder joiner = new StringBuilder();
        for( TypeElement typeElement : typeElements ) {
            if( joiner.length() > 0 ) {
                joiner.append( "," );
            }
            joiner.append( typeElement.getSimpleName().toString() );
        }
        return joiner.toString();
    }
}
