package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.AbstractContentProvider;
import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.ContentProviderProcessor;

public abstract class ContractContentProvider extends AbstractContentProvider {

    private final String authority;
    private final List<TableContract> tables;
    private final List<JoinContract> joins;

    public ContractContentProvider( @NonNull final String authority ) {
        this.authority = authority;
        this.tables = new ArrayList<>();
        this.joins = new ArrayList<>();
    }

    public boolean addContract( final TableContract tableContract ) {
        return tables.add( tableContract );
    }

    public boolean addContract( final JoinContract joinContract) {
        return joins.add( joinContract );
    }

    @NonNull
    @Override
    public ContentProviderProcessor[] onCreateContentProviderProcessors() {
        final ContentProviderProcessor[] processors = new ContentProviderProcessor[ tables.size() + joins.size() ];

        int index = 0;
        for( int position = 0; position < tables.size(); position++ ) {
            processors[index] = onResolveProcessor( tables.get( position ) );
            index++;
        }
        for( int position = 0; position < joins.size(); position++ ) {
            processors[index] = onResolveProcessor( joins.get( position ) );
            index++;
        }
        return processors;
    }

    @NonNull
    public ContentProviderProcessor onResolveProcessor( Contract contract ) {
        if( contract instanceof TableContract ) {
            return new TableContractContentProviderProcessor( (TableContract) contract, authority );
        } else if( contract instanceof JoinContract ) {
            return new JoinContractContentProviderProcessor( (JoinContract) contract, authority );
        } else {
            throw new IllegalStateException( "ContentProviderProcessor can't be resolved from '" + contract.getClass().getSimpleName() + "'." );
        }
    }
}
