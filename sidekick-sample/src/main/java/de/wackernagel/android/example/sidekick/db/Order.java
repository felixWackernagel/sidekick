package de.wackernagel.android.example.sidekick.db;

import java.util.List;

import de.wackernagel.android.example.sidekick.provider.ArticleProvider;
import de.wackernagel.android.sidekick.annotations.Check;
import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.ForeignKey;

@Contract( authority = ArticleProvider.AUTHORITY )
class Order extends Base {

    @Check( "LENGTH(name) > 0" )
    String name;

    @ForeignKey( onUpdate = ForeignKey.Action.CASCADE )
    List<OrderItem> items;

}