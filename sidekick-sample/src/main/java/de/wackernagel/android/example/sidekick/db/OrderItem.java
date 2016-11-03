package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.sidekick.annotations.Contract;

@Contract( authority = Constants.AUTHORITY )
class OrderItem {
    String name;
    Order order;
}
