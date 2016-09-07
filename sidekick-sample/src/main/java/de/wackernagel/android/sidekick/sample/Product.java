package de.wackernagel.android.sidekick.sample;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = "com.example.provider" )
public abstract class Product {
    abstract String getName();

    abstract String getDeliveryAddress();
}
