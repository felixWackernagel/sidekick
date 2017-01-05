package de.wackernagel.android.example.sidekick.db;

import de.wackernagel.android.sidekick.annotations.Contract;
import de.wackernagel.android.sidekick.annotations.Default;
import de.wackernagel.android.sidekick.annotations.NotNull;

@Contract(authority = "com.android.example")
class Foo {
    @NotNull
    @Default("example")
    String bar;
}
