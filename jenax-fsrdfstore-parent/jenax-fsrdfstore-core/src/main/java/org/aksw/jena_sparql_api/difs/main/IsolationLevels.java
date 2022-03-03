package org.aksw.jena_sparql_api.difs.main;

public class IsolationLevels {
	/**
	 * READ_UNCOMMITTED allows a transaction to read uncommitted data, i.e. data of another
	 * transaction that eventually rolls back */
	public static IsolationLevel READ_UNCOMMITTED;
	
	/**
	 * READ_COMMITTED allows a transaction to only read committed data. However, other transactions
	 * may meanwhile commit changes to read data multiple times which may lead to
	 * repeatedely reading the same data return different results
	 */
	public static IsolationLevel READ_COMMITTED;
	
	/**
	 * READ_COMMITTED with the guarantee that reading the same data yields the same result.
	 */
	public static IsolationLevel REPEATABLE_READ;
	
	
	public static IsolationLevel SERIALIZABLE;
}
