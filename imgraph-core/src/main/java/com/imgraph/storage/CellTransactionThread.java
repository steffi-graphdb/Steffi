package com.imgraph.storage;

/**
 * @author Aldemar Reynaga
 * Thread local storage of the transaction main object
 */
public class CellTransactionThread {
	
	public static final ThreadLocal<CellTransaction> userThreadLocal = new ThreadLocal<CellTransaction>();

	public static void set(CellTransaction transaction) {
		userThreadLocal.set(transaction);
	}

	public static void unset() {
		userThreadLocal.remove();
	}

	
	public static CellTransaction get() {
		CellTransaction cellTransaction = userThreadLocal.get();
		if (cellTransaction == null)
			throw new TransactionRequiredException();
		return cellTransaction;
	}
	
	public static boolean isTransactionSet() {
		return (userThreadLocal.get() != null);
	}
	
}
