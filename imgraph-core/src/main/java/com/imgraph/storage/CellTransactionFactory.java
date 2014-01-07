package com.imgraph.storage;

/**
 * @author Aldemar Reynaga
 * Starts a new transaction using a thread-local storage mechanism
 */
public class CellTransactionFactory {
	
	public static CellTransaction beginTransaction() {
		CellTransaction cellTransaction = new CellTransaction();
		
		CellTransactionThread.set(cellTransaction);
		
		return cellTransaction;
	}

	
}
