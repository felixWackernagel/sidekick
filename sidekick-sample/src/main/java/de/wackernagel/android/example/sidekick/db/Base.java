package de.wackernagel.android.example.sidekick.db;

import java.util.Date;

import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.NotNull;

public class Base {

    @Default( value = "CURRENT_TIMESTAMP")
    @NotNull
    Date created;

    @Default( value = "CURRENT_TIMESTAMP")
    @NotNull
    Date changed;

}
