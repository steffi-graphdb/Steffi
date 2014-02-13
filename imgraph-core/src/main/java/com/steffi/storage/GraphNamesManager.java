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

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.steffi.model.SteffiGraph;

/**
 * @author Aldemar Reynaga
 * Callable class used to register a new graph item name in all the data servers 
 */
public class GraphNamesManager implements Serializable, Callable<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4969351470224014844L;
	private final String name;
	
	public GraphNamesManager(String name) {
		this.name = name;
	}
	
	@Override
	public Boolean call() throws Exception {
		SteffiGraph graph = SteffiGraph.getInstance();
		
		graph.registerLocalItemName(name);
		
		return true;
	}

}
