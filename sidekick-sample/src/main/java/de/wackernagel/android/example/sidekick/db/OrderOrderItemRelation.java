package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = Constants.AUTHORITY )
public class OrderOrderItemRelation {

    @Column
    int position;

}
