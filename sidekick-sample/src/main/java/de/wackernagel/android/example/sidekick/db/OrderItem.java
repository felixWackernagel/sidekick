package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.example.sidekick.provider.ArticleProvider;
import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.NotNull;

@Contract( authority = ArticleProvider.AUTHORITY )
class OrderItem {

    String name;

    @NotNull
    Order order;

}
