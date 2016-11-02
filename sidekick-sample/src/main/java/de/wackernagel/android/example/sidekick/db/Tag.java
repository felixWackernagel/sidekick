package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.NotNull;

@Contract( authority = Constants.AUTHORITY )
public class Tag {
    @NotNull
    String name;
}
