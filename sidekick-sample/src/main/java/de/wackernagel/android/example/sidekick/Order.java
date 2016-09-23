package de.wackernagel.android.example.sidekick;

import java.util.List;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = "com.example.provider" )
public class Order extends Base {

    @Column
    String name;

    @Column
    List<OrderItem> orderItems;

}
