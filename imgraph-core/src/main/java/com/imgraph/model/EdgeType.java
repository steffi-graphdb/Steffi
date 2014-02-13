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
package com.imgraph.model;

import java.util.HashMap;
import java.util.Map;




public enum EdgeType {
	
	IN(1),
	OUT(2),
	UNDIRECTED(3),
	HYPEREDGE(4);
	
	private static final Map<Integer, EdgeType> codeMap;
	static {
		codeMap = new HashMap<Integer, EdgeType>();
		codeMap.put(1, IN);
		codeMap.put(2, OUT);
		codeMap.put(3, UNDIRECTED);
		codeMap.put(4, HYPEREDGE);
	}
	
			
	private int code;
	
	public int getCode() {
		return code;
	}
	
	EdgeType(int code) {
		this.code = code;
	}
	
	public static EdgeType getEdgeType(int code) {
		return codeMap.get(code);
	}
	
	public static EdgeType invertType(EdgeType edgeType) {
		switch(edgeType) {
		case IN:
			return OUT;
			
		case OUT:
			return IN;
			
		default:
			return edgeType;
			
		}
	}
}
