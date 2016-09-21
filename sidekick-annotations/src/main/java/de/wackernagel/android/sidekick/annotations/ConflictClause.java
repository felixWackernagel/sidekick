package de.wackernagel.android.sidekick.annotations;

public enum ConflictClause {

    NONE,
    ROLLBACK,
    ABORT,
    FAIL,
    IGNORE,
    REPLACE;

}
