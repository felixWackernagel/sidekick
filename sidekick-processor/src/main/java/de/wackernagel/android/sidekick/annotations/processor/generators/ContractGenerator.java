package de.wackernagel.android.sidekick.annotations.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.wackernagel.android.sidekick.annotations.Check;
import de.wackernagel.android.sidekick.annotations.ConflictClause;
import de.wackernagel.android.sidekick.annotations.ForeignKey;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;
import de.wackernagel.android.sidekick.annotations.processor.definitions.BaseDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.ColumnDefinition;
import de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

class ContractGenerator {

    static void generate( final TypeSpec.Builder classBuilder, final TableDefinition tableDefinition, final Set<ColumnDefinition> fields) {
        final Set<ColumnDefinition> contractFields = filter( fields );
        final ClassName contractName = ClassName.get(tableDefinition.getPackageName(), "Contract");
        final TypeSpec.Builder contractBuilder = TypeSpec.classBuilder( contractName )
                .addModifiers(PUBLIC, STATIC);

        extend(contractBuilder, tableDefinition, contractFields);
        tableConstant(contractBuilder, tableDefinition.getTableName() );
        columnConstants(contractBuilder, contractFields);
        projection(contractBuilder, contractFields);
        contentUri(contractBuilder, tableDefinition.getTableAuthority());

        classBuilder.addType( contractBuilder.build() );
    }

    private static Set<ColumnDefinition> filter( final Set<ColumnDefinition> columnDefinitions ) {
        final Set<ColumnDefinition> contractFields = new LinkedHashSet<>( columnDefinitions );
        for( final Iterator<ColumnDefinition> iterator = contractFields.iterator(); iterator.hasNext() ; ) {
            if( iterator.next().isCollectionType() ) {
                iterator.remove();
            }
        }
        return contractFields;
    }

    private static void extend( final TypeSpec.Builder classBuilder, final TableDefinition tableDefinition, final Set<ColumnDefinition> fields) {
        classBuilder.superclass(ClassName.get("de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract", "TableContract"));

        classBuilder.addMethod(
                MethodSpec.methodBuilder("getTable")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .addStatement("return $L", "TABLE")
                        .build());

        final ClassName db = ClassName.get("android.database.sqlite", "SQLiteDatabase");

        classBuilder.addMethod(
                MethodSpec.methodBuilder("onCreate")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(db, "db", FINAL)
                        .addCode(createTableStatement( tableDefinition, fields))
                        .returns(TypeName.VOID)
                        .build()
        );

        classBuilder.addMethod(
                MethodSpec.methodBuilder("onUpgrade")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(db, "db", FINAL)
                        .addParameter(int.class, "oldVersion", FINAL)
                        .addParameter(int.class, "newVersion", FINAL)
                        .returns(TypeName.VOID)
                        .build()
        );
    }

    private static CodeBlock createTableStatement(final TableDefinition table, final Set<ColumnDefinition> fields) {
        final CodeBlock.Builder sql = CodeBlock.builder();
        sql.add("db.execSQL( \"CREATE TABLE IF NOT EXISTS " + escape( table.getTableName() ) + " (\"\n");

        final Map<Integer, Set<ColumnDefinition>> uniqueness = new HashMap<>();
        Iterator<ColumnDefinition> iterator = fields.iterator();
        while( iterator.hasNext() ) {
            final ColumnDefinition column = iterator.next();
            if( column.skipSQLite() ) {
                continue;
            }

            sql.add( "\t + \"" + escape( column.getColumnName() ) + " " + column.getSQLiteType() );

            if( column.isPrimaryKey() ) {
                sql.add(" CONSTRAINT '" + column.getColumnName() + "_pk' PRIMARY KEY AUTOINCREMENT");
            }

            final NotNull notNull = column.notNull();
            if( notNull != null ) {
                sql.add(" CONSTRAINT '" + column.getColumnName() + "_not_null' NOT NULL");
                if( notNull.onConflict() != ConflictClause.NONE ) {
                    sql.add( " ON CONFLICT ").add(notNull.onConflict().toString() );
                }
            }

            final Unique unique = column.unique();
            if( unique != null ) {
                if( unique.group() < 0 ) {
                    sql.add(" CONSTRAINT '" + column.getColumnName() + "_unique' UNIQUE");
                    if( unique.onConflict() != ConflictClause.NONE ) {
                        sql.add( " ON CONFLICT ").add(unique.onConflict().toString() );
                    }
                } else {
                    final Integer groupNumber = column.unique().group();
                    Set<ColumnDefinition> groupNames;
                    if( uniqueness.containsKey( groupNumber ) ) {
                        groupNames = uniqueness.get( groupNumber );
                    } else {
                        groupNames = new LinkedHashSet<>( 1 );
                    }
                    groupNames.add( column );
                    uniqueness.put(groupNumber, groupNames);
                }
            }

            final String defaultValue = column.defaultValue();
            if( defaultValue != null && defaultValue.length() > 0 ) {
                sql.add(" CONSTRAINT '" + column.getColumnName() + "_default' DEFAULT ").add(defaultValue);
            }

            final Check checkConstraint = column.check();
            if( checkConstraint != null && checkConstraint.value().length() > 0 ) {
                sql.add(" CONSTRAINT '" + column.getColumnName() + "_check' CHECK(" + checkConstraint.value() + ")");
            }

            if (column.isBoolean() ) {
                sql.add(" CONSTRAINT '" + column.getColumnName() + "_check_boolean' CHECK(" + escape( column.getColumnName() ) + " IN ( 0, 1 ) )");
            }

            if( column.isForeignKey() ) {
                String parentTable = BaseDefinition.formatNameForSQL( column.getObjectType() );


                sql.add(" CONSTRAINT '" + column.getColumnName() + "_fk' REFERENCES " + escape( parentTable ) + "('_id')" );

                final ForeignKey foreignKey = column.foreignKey();
                if( foreignKey != null && foreignKey.onDelete() != ForeignKey.Action.NONE ) {
                    sql.add(" ON DELETE ").add(foreignKey.onDelete().toString() );
                }
                if( foreignKey != null && foreignKey.onUpdate() != ForeignKey.Action.NONE ) {
                    sql.add(" ON UPDATE ").add(foreignKey.onUpdate().toString() );
                }
            }

            if( iterator.hasNext() || !uniqueness.isEmpty() ) {
                sql.add( ", " );
            }
            sql.add( "\"\n" );
        }

        int last = uniqueness.size() - 1;
        int index = 0;
        for( Set<ColumnDefinition> groupElements : uniqueness.values() ) {
            sql.add("\t + \"CONSTRAINT '");
            int j = 0;
            for( ColumnDefinition column : groupElements ) {
                if( j > 0 ) {
                    sql.add( "_" );
                }
                sql.add( column.getColumnName() );
                j++;
            }
            sql.add( "_unique' UNIQUE(" );
            int k = 0;
            for( ColumnDefinition column : groupElements ) {
                if( k > 0 ) {
                    sql.add( "," );
                }
                sql.add( escape( column.getColumnName() ) );
                k++;
            }
            sql.add(")");
            int e = 0;
            for( ColumnDefinition column : groupElements ) {
                if( column.unique().onConflict() != ConflictClause.NONE ) {
                    sql.add( " ON CONFLICT " ).add( column.unique().onConflict().toString() );
                    e++;
                }
                if( e > 1 ) {
                    throw new IllegalStateException(
                            "A unique constraint with more then one column has more then one conflict-clauses defined! See member field '"
                            + column.getFieldName() + "' in class '" + table.getObjectType( false, false ) + "'." );
                }
            }
            if( index != last ) {
                sql.add( ", " );
            }
            sql.add( "\"\n" );
            index++;
        }

        sql.add( "\t + $S );\n", ");" );
        return sql.build();
    }

    private static void tableConstant( final TypeSpec.Builder classBuilder, final String name) {
        classBuilder.addField( constant(String.class, "TABLE", name).build() );
    }

    private static void columnConstants( final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> columnDefinitions) {
        for( ColumnDefinition columnDefinition : columnDefinitions) {
            if( columnDefinition.isPrimaryKey() ) {
                continue;
            }

            classBuilder.addField(
                    constant(String.class, columnDefinition.getConstantFieldName(), columnDefinition.getColumnName())
                    .build() );
        }
    }

    private static void projection( final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> columns ) {
        if( columns.size() > 0 ) {
            final CodeBlock.Builder value = CodeBlock.builder();
            int index = 0;
            for( ColumnDefinition column : columns ) {
                value.add( (index == 0 ? "{\n" : "") + "\t$L" + (index+1 == columns.size() ? "\n" : ",\n"), column.getConstantFieldName());
                index++;
            }
            value.add( "}" );

            classBuilder.addField(
                    FieldSpec.builder(String[].class, "PROJECTION")
                            .addModifiers(PUBLIC, STATIC, FINAL)
                            .initializer(value.build())
                            .build());
        }
    }

    private static void contentUri(final TypeSpec.Builder classBuilder, final String authority) {
        if (authority != null && authority.length() > 0) {
            ClassName uri = ClassName.get( "android.net", "Uri");
            classBuilder.addField(
                    FieldSpec.builder(uri, "CONTENT_URI")
                            .addModifiers(PUBLIC, STATIC, FINAL)
                            .initializer("Uri.parse( \"content://" + authority + "/\" + $L )", "TABLE")
                            .build());
        }
    }

    private static FieldSpec.Builder constant(final Class<?> type, final String name, final String value) {
        return constant(type, name, "$S", value);
    }

    private static FieldSpec.Builder constant(final Class<?> type, final String name, final String pattern, final String value) {
        return FieldSpec.builder(type, name)
                .addModifiers(PUBLIC, STATIC, FINAL)
                .initializer(pattern, value);
    }

    private static String escape( final String value ) {
        final String quotation = "'";
        return quotation.concat( value ).concat( quotation );
    }
}
