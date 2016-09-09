package de.wackernagel.android.sidekick.sample;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = "com.example.provider" )
public class Product {

    @Column
    String name;

    @Column
    String deliveryAddress;
}
