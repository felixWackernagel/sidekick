package de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract;

public interface JoinContract extends Contract {

	String[] getTables();

	String getJoinStatement();

}
