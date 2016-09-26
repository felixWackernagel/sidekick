package de.wackernagel.android.sidekick.annotations.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * A TableDefinition contains information about the model and SQLite table class.
 */
public class TableDefinition extends Definition {

    private final String packageName;
    private final String className;
    private final String authority;

    public TableDefinition( final Types types, final Elements elements, final Messager log, final String packageName, final String className, final String authority ) {
        super(types, elements, log);
        this.packageName = packageName;
        this.className = className;
        this.authority = authority;
    }

    /**
     * @return package name or empty String of class with Contract annotation
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return class name of class with Contract annotation
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return fully name of generating class with package, class and model suffix
     */
    public String getObjectType( boolean withModel ) {
        String result = className;
        if( packageName != null && packageName.length() > 0 ) {
            result = packageName + "." + className;
        }
        if( withModel ) {
            return result.concat( "Model" );
        }
        return result;
    }

    /**
     * @return name of sqlite table
     */
    public String getTableName() {
        return formatNameForSQL(className);
    }

    /**
     * @return authority from Contract annotation
     */
    public String getTableAuthority() {
        return authority;
    }

    @Override
    public String toString() {
        return "Definition of " + packageName + "." + className + " (" + authority + ")";
    }

    @Override
    public boolean equals(Object o) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        TableDefinition that = ( TableDefinition ) o;

        if( packageName != null ? !packageName.equals(that.packageName) : that.packageName != null )
            return false;
        if( !className.equals(that.className) ) return false;
        return authority.equals(that.authority);

    }

    @Override
    public int hashCode() {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + className.hashCode();
        result = 31 * result + authority.hashCode();
        return result;
    }
}
