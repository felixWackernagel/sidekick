package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

public abstract class TableContract implements Contract, SQLiteTable {
    abstract public String getTable();
}
