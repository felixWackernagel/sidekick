package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.AbstractContentProvider;
import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.ContentProviderProcessor;

public abstract class ContractContentProvider extends AbstractContentProvider {

    // PROCESSOR
    private final @NonNull String authority;
    private final List<Contract> contracts;

    // DATABASE
    private final List<SQLiteTable> sqLiteTables;
    private final @NonNull String databaseName;
    private final @IntRange(from = 1) int databaseVersion;
    private final @Nullable SQLiteDatabase.CursorFactory cursorFactory;

    public ContractContentProvider(
            @NonNull final String authority,
            @NonNull final String databaseName,
            @IntRange(from = 1) final int databaseVersion ) {
        this( authority, databaseName, databaseVersion, null );
    }

    public ContractContentProvider(
            @NonNull final String authority,
            @NonNull final String databaseName,
            @IntRange(from = 1) final int databaseVersion,
            @Nullable final SQLiteDatabase.CursorFactory cursorFactory ) {
        this.authority = authority;
        this.databaseName = databaseName;
        this.databaseVersion = databaseVersion;
        this.cursorFactory = cursorFactory;
        this.contracts = new ArrayList<>();
        this.sqLiteTables = new ArrayList<>();
    }

    /**
     * The {@link SQLiteTable} is used for the {@link SQLiteTableOpenHelper}.
     *
     * @param sqLiteTable table of the database
     */
    public void addSQLiteTable( final SQLiteTable sqLiteTable ) {
        sqLiteTables.add( sqLiteTable );
    }

    /**
     * If the {@link Contract} implements the {@link SQLiteTable} interface then
     * it calls internal {@link #addSQLiteTable(SQLiteTable)}.
     *
     * @param contract object to define a processor
     */
    public void addContract( final Contract contract ) {
        if( contract instanceof SQLiteTable ) {
            addSQLiteTable((SQLiteTable) contract);
        }
        contracts.add( contract );
    }

    @NonNull
    @Override
    public ContentProviderProcessor[] onCreateContentProviderProcessors() {
        final ContentProviderProcessor[] processors = new ContentProviderProcessor[ contracts.size() ];
        final int count = contracts.size();
        for( int position = 0; position < count; position++ ) {
            processors[position] = onResolveProcessor(contracts.get(position));
        }
        return processors;
    }

    @NonNull
    @Override
    public SQLiteOpenHelper onCreateSQLiteOpenHelper() {
        return new SQLiteTableOpenHelper(
                getContext(),
                databaseName,
                cursorFactory,
                databaseVersion,
                sqLiteTables);
    }

    @NonNull
    public ContentProviderProcessor onResolveProcessor( Contract contract ) {
        if( contract instanceof TableContract ) {
            return new TableContractContentProviderProcessor( (TableContract) contract, authority );
        } else if( contract instanceof JoinContract ) {
            return new JoinContractContentProviderProcessor( (JoinContract) contract, authority );
        } else {
            throw new IllegalStateException( "ContentProviderProcessor can't be resolved from '" + contract.getClass().getSimpleName() + "'." );
        }
    }

    @NonNull
    public List<SQLiteTable> getSQLiteTables() {
        return sqLiteTables;
    }

    @NonNull
    public List<Contract> getContracts() {
        return contracts;
    }

    @NonNull
    public String getAuthority() {
        return authority;
    }

    @NonNull
    public String getDatabaseName() {
        return databaseName;
    }

    @IntRange(from = 1)
    public int getDatabaseVersion() {
        return databaseVersion;
    }

    @Nullable
    public SQLiteDatabase.CursorFactory getCursorFactory() {
        return cursorFactory;
    }
}
