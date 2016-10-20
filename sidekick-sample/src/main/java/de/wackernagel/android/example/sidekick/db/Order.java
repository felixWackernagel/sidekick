package de.wackernagel.android.example.sidekick.db;

import java.util.List;

import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.ForeignKey;

@Contract( authority = Constants.AUTHORITY )
public class Order extends Base {

    String name;

    @ForeignKey( onUpdate = ForeignKey.Action.CASCADE )
    List<OrderItem> items;

}