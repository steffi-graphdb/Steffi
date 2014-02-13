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
package com.steffi.traversal;

import java.io.Serializable;

import com.steffi.model.EdgeType;


/**
 * @author Aldemar Reynaga
 * Configuration of edges for the traversal
 */
public class EdgeTraversalConf implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1894624012493181977L;
	private String name;
	private EdgeType edgeType;
	
	
	
	public EdgeTraversalConf(String name, EdgeType edgeType) {
		this.name = name;
		this.edgeType = edgeType;
	}
	
	
	
	public EdgeTraversalConf(String name) {
		this.name = name;
		this.edgeType = EdgeType.UNDIRECTED;
		
		
	}
	
	
	
	
	
	@Override
	public String toString() {
		return "EdgeTraversalConf [name=" + name + ", edgeType=" + edgeType
				+ "]";
	}


	public String getName() {
		return name;
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}
	
		
}
