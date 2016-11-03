package de.wackernagel.android.sidekick.annotations.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import de.wackernagel.android.sidekick.annotations.processor.definitions.*;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class ModelGenerator {

    private TypeSpec generatedModel;

    private static final ClassName nullable = ClassName.get( "android.support.annotation", "Nullable" );
    private static final ClassName nonNull = ClassName.get( "android.support.annotation", "NonNull" );
    private static final ClassName objects = ClassName.get( "de.wackernagel.android.sidekick.compats", "ObjectsCompat" );
    private static final ClassName cursor = ClassName.get( "android.database", "Cursor" );

    public ModelGenerator(final de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition tableDefinition, final Set<ColumnDefinition> columnDefinitions) {
        final TypeSpec.Builder classBuilder = classBuilder(tableDefinition.getObjectType(false, true))
                .addModifiers(PUBLIC)
                .addJavadoc("Generated by Sidekick (" + new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.ENGLISH).format(new Date()) + ")\n");

        objectCreator(classBuilder, tableDefinition, columnDefinitions);
        memberField(classBuilder, columnDefinitions);
        constructor(classBuilder, columnDefinitions);
        getter(classBuilder, columnDefinitions);
        toString(classBuilder, columnDefinitions, tableDefinition.getObjectType(false, true));
        equals(classBuilder, columnDefinitions, tableDefinition);
        hashCode(classBuilder, columnDefinitions);
        builder(classBuilder, tableDefinition, columnDefinitions);

        generatedModel = classBuilder.build();
    }

    private void builder(TypeSpec.Builder classBuilder, de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition tableDefinition, Set<ColumnDefinition> columnDefinitions) {
        final ClassName builderName = ClassName.get(tableDefinition.getPackageName(), "Builder");
        final ClassName contentValues = ClassName.get( "android.content", "ContentValues" );
        final ClassName contract = ClassName.get(tableDefinition.getPackageName(), tableDefinition.getClassName() + "Contract");

        TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderName)
                .addModifiers(PUBLIC, STATIC)
                .addField(contentValues, "values", PRIVATE, FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addStatement("this.values = new $T()", contentValues)
                        .addModifiers(PUBLIC)
                        .build());

        for( ColumnDefinition column : columnDefinitions ) {
            if( column.isPrimaryKey() || column.isCollectionType() ) {
                continue;
            }
            final String memberName = column.getFieldName() + (column.isContractObjectType() ? "Id" : "");
            final String methodName = Character.toTitleCase(memberName.charAt(0)) + memberName.substring( 1 );
            final TypeName type = column.isContractObjectType() ? TypeName.LONG : column.getObjectType();
            final ParameterSpec.Builder param = ParameterSpec.builder(type, memberName, FINAL);
            if( column.isObjectTypeNotPrimitive() ) {
                if( column.isNotNull() ) {
                    param.addAnnotation(nonNull);
                } else {
                    param.addAnnotation(nullable);
                }
            }
            builderClass.addMethod(MethodSpec.methodBuilder("set" + methodName)
                    .returns(TypeName.VOID)
                    .addModifiers(PUBLIC)
                    .addParameter(param.build())
                    .addStatement("values.put( $T.$N, $N )", contract, column.getConstantFieldName(), memberName)
                    .build());
        }
        builderClass.addMethod(MethodSpec.methodBuilder("build")
                .returns(contentValues)
                .addAnnotation(nonNull)
                .addStatement("return values", contentValues)
                .addModifiers(PUBLIC)
                .build());
        classBuilder.addType(builderClass.build());
    }

    private void objectCreator(TypeSpec.Builder classBuilder, de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition tableDefinition, Set<ColumnDefinition> columnDefinitions) {
        final MethodSpec.Builder createFromCursor = MethodSpec.methodBuilder( "createFromCursor" )
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns( ClassName.get(tableDefinition.getPackageName(), tableDefinition.getObjectType(false, true)))
                .addParameter(cursor, "cursor", Modifier.FINAL);

        final  CodeBlock.Builder impl = CodeBlock.builder();
        impl.add("return new " + tableDefinition.getObjectType(false, true) + "(\n");
        int index = 0;
        for( ColumnDefinition column : columnDefinitions ) {
            if( column.isCollectionType() )
                continue;

            if( index > 0 )
                impl.add( ",\n");
            impl.add("\t");
            if( column.isBoolean() )
                impl.add( "1 == " );
            if( column.isByte() ) {
                impl.add("(byte) ");
            }
            impl.add("cursor.get");
            impl.add(column.getCursorMethod());
            impl.add("( " + index + " )");
            index++;
        }
        impl.add("\n);\n");

        final TypeName objectCreator = ParameterizedTypeName.get(
                ClassName.get("de.wackernagel.android.sidekick.frameworks.objectcursor", "ObjectCreator"),
                ClassName.get(tableDefinition.getPackageName(), tableDefinition.getObjectType(false, true)));

        final TypeSpec factory = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(objectCreator)
                .addMethod(createFromCursor.addCode(impl.build()).build())
                .build();

        final FieldSpec.Builder fieldBuilder = FieldSpec.builder( objectCreator, "FACTORY", PUBLIC, STATIC, FINAL );
        fieldBuilder.initializer("$L", factory);
        classBuilder.addField(fieldBuilder.build());
    }

    private void hashCode( final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> columns ) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .returns(int.class)
                .addModifiers(Modifier.PUBLIC);

        method.addCode("return $T.hash( ", objects);
        boolean first = true;
        for( ColumnDefinition column : columns ) {
            if( !first ) {
                method.addCode( ", " );
            }
            method.addCode( "$N", column.getFieldName() + (column.isContractObjectType() ? "Id" : "") );
            first = false;
        }
        method.addCode(" );\n");

        classBuilder.addMethod( method.build() );
    }

    private static void equals( final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> columns, final de.wackernagel.android.sidekick.annotations.processor.definitions.TableDefinition table ) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("equals")
                .addParameter(Object.class, "obj")
                .addAnnotation( Override.class )
                .returns(boolean.class)
                .addModifiers(Modifier.PUBLIC);

        final String modelClass = table.getObjectType(false, true);
        method.addStatement( "if( this == obj ) return true")
                .addStatement("if( obj == null || getClass() != obj.getClass() ) return false")
                .addStatement("$L other = ($L) obj", modelClass, modelClass);

        boolean first = true;
        for( ColumnDefinition column : columns ) {
            final String name = Character.toTitleCase( column.getFieldName().charAt( 0 ) ) + column.getFieldName().substring( 1 );
            final String prefix = column.isBoolean() ? "is" : "get";
            final String getter = prefix + name + (column.isContractObjectType() ? "Id" : "");
            final String member = column.getFieldName() + ( column.isContractObjectType() ? "Id" : "" );
            method.addCode((first ? "return " : "\n\t&& ") + "$T.equals( $L, other.$L() )", objects, member, getter);
            first = false;
        }
        method.addCode(";\n");

        classBuilder.addMethod( method.build() );
    }

    private static void toString(final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> fields, final String className) {
        final MethodSpec.Builder method = MethodSpec.methodBuilder("toString")
                .addAnnotation( Override.class )
                .returns( String.class )
                .addModifiers( Modifier.PUBLIC );

        final StringBuilder statement = new StringBuilder();
        final List<String> args = new ArrayList<>();

        statement.append( "return $S +" );
        args.add( className + "(" );

        int index = 0;
        for( ColumnDefinition columnDefinition : fields ) {
            statement.append("\n$S + $L +");
            args.add( (index > 0 ? ", " : "" ) + columnDefinition.getFieldName() + (columnDefinition.isContractObjectType() ? "Id" : "") + "=" );
            args.add( columnDefinition.getFieldName() + (columnDefinition.isContractObjectType() ? "Id" : "") );
            index++;
        }
        statement.append("$S");
        args.add( ")" );

        method.addStatement(statement.toString(), args.toArray());
        classBuilder.addMethod(method.build());
    }

    private static void getter(final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> fields) {
        for( ColumnDefinition columnDefinition : fields ) {
            final String memberName = columnDefinition.getFieldName() + (columnDefinition.isContractObjectType() ? "Id" : "");
            final String name = Character.toTitleCase( memberName.charAt( 0 ) ) + memberName.substring( 1 );

            final String prefix = columnDefinition.isBoolean() ? "is" : "get";
            final MethodSpec.Builder getMethod = MethodSpec.methodBuilder( prefix + name)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(columnDefinition.isContractObjectType() ? TypeName.LONG : columnDefinition.getObjectType())
                    .addStatement("return $N", memberName );

            if( columnDefinition.isCollectionType() ) {
                // collections have no setter and were init. in constructor
                getMethod.addAnnotation(nonNull);
            } else if( columnDefinition.isObjectTypeNotPrimitive() ) {
                if( columnDefinition.isNotNull() ) {
                    getMethod.addAnnotation(nonNull);
                } else {
                    getMethod.addAnnotation(nullable);
                }
            }

            classBuilder.addMethod(getMethod.build());
        }
    }

    private static void constructor(final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> fields) {
        final MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers( Modifier.PUBLIC );
        for( ColumnDefinition columnDefinition : fields ) {
            if( columnDefinition.isCollectionType() ) {
                constructor.addStatement( "this.$N = new $T()", columnDefinition.getFieldName(), columnDefinition.getInstantiableCollectionType() );
                continue;
            }

            final String memberName = columnDefinition.getFieldName() + (columnDefinition.isContractObjectType() ? "Id" : "");
            final ParameterSpec.Builder parameter = ParameterSpec.builder(
                    columnDefinition.isContractObjectType() ? TypeName.LONG : columnDefinition.getObjectType(),
                    memberName,
                    Modifier.FINAL );

            if( columnDefinition.isObjectTypeNotPrimitive() ) {
                if( columnDefinition.isNotNull() ) {
                    parameter.addAnnotation( nonNull );
                } else {
                    parameter.addAnnotation( nullable );
                }
            }

            constructor.addParameter(parameter.build())
                    .addStatement("this.$N = $N", memberName, memberName);
        }
        classBuilder.addMethod( constructor.build() );
    }

    private static void memberField( final TypeSpec.Builder classBuilder, final Set<ColumnDefinition> fields) {
        for( ColumnDefinition column : fields ) {
            classBuilder.addField( FieldSpec.builder(
                    column.isContractObjectType() ? TypeName.LONG : column.getObjectType(),
                    column.getFieldName() + (column.isContractObjectType() ? "Id" : "" ),
                    Modifier.PRIVATE, FINAL).build());
        }
    }

    public boolean writeClass( final String packageName, final Filer filer) {
        try {
            final JavaFile javaFile = JavaFile.builder(packageName, generatedModel).build();
            javaFile.writeTo( filer );
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
