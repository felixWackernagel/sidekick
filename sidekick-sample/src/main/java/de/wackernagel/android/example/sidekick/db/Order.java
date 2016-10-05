package de.wackernagel.android.example.sidekick.db;

import java.util.List;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.ForeignKey;

@Contract( authority = "com.example.provider" )
public class Order extends Base {

    @Column
    String name;

    @ForeignKey( onUpdate = ForeignKey.Action.CASCADE )
    @Column
    List<OrderItem> items;

}