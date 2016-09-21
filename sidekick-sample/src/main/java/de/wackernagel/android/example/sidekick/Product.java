package de.wackernagel.android.example.sidekick;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = "com.example.provider" )
public class Product {

    @Column
    String name;

    @Column
    Address shippingAddress;

}
