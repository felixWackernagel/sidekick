package de.wackernagel.android.example.sidekick;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.ConflictClause;
import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.NotNull;

@Contract( authority = "com.example.provider" )
public class OrderItem {

    @Column
    String name;

    @Column
    Order order;

}
