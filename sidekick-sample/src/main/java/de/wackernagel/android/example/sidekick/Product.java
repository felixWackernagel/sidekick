package de.wackernagel.android.example.sidekick;

import java.util.List;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.NotNull;
import de.wackernagel.android.sidekick.annotations.Unique;

@Contract( authority = "com.example.provider" )
public class Product {
    @Column
    boolean favorite;

    @Column
    @NotNull
    String name;

    @Column
    Address shippingAddress;

    @Column
    List<Address> deliveryAddress;
}
