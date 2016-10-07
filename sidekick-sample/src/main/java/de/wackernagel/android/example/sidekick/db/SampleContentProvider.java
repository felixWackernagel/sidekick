package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.ContractContentProvider;

public class SampleContentProvider extends ContractContentProvider {

    public SampleContentProvider() {
        super( Constants.AUTHORITY , Constants.NAME, Constants.VERSION );
        addContract( new SimpleContract() );
    }
}
