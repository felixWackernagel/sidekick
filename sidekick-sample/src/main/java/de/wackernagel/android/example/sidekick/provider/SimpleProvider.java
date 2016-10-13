package de.wackernagel.android.example.sidekick.provider;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.ContractContentProvider;

public class SimpleProvider extends ContractContentProvider {

    public SimpleProvider() {
        super( "de.wackernagel.android.example.sidekick", "simple.db", 1);
        addContract( new ArticleContract() );
    }
}
