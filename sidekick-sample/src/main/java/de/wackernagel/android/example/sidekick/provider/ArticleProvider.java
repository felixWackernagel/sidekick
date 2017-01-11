package de.wackernagel.android.example.sidekick.provider;

import de.wackernagel.android.example.sidekick.db.OrderModel;
import de.wackernagel.android.example.sidekick.db.TagModel;
import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.ContractContentProvider;

public class ArticleProvider extends ContractContentProvider {

    public static final String AUTHORITY = "de.wackernagel.android.example.sidekick";

    public ArticleProvider() {
        super( AUTHORITY, "simple.db", 3);
        addContract( new ArticleContract() );
        addContract( new OrderModel.Contract() );
        addContract( new TagModel.Contract() );
    }
}
