/*******************************************************************************
 * Copyright (c) 2014 EURA NOVA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Aldemar Reynaga - initial API and implementation
 *     Salim Jouili - initial API and implementation
 ******************************************************************************/
package com.steffi.storage;

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
