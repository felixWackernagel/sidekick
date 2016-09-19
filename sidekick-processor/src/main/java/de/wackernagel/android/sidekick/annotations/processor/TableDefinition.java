package de.wackernagel.android.sidekick.annotations.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class TableDefinition extends Definition {

    private final String tableName;
    private final String authority;

    public TableDefinition( final Types types, final Elements elements, final Messager log, final String tableName, final String authority ) {
        super(types, elements, log);
        this.tableName = tableName;
        this.authority = authority;
    }

    public String getClassName() {
        return tableName;
    }

    public String getTableName() {
        return tableName.toLowerCase();
    }

    public String getAuthority() {
        return authority;
    }
}
