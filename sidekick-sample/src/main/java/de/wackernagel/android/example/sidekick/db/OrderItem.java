package de.wackernagel.android.example.sidekick.db;

import java.util.List;

import de.wackernagel.android.sidekick.annotations.Column;
import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = Constants.AUTHORITY )
public class OrderItem {

    @Column
    String name;

}
