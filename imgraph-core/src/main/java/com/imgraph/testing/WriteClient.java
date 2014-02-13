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
package com.imgraph.testing;

/**
 * @author Aldemar Reynaga
 * Process defined to execute parallel write tests
 */
public class WriteClient implements Runnable {

private final String fileName;
	
	public WriteClient(String fileName) {
		super();
		this.fileName = fileName;
	}


	@Override
	public void run() {
		try {
			TestTools.testWrites(fileName, null);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
}
