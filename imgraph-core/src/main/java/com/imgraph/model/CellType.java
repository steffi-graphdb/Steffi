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

/**
 * @author Aldemar Reynaga
 * Cell subclasses types. The hyperedge variant was not yet implemented in the current version
 */
public enum CellType  {
	VERTEX((byte)1),
	EDGE((byte)2),
	HYPEREDGE((byte)3); 
	
	
	private static final Map<Byte, CellType> codeMap;
	static {
		codeMap = new HashMap<Byte, CellType>();
		codeMap.put((byte) 1, VERTEX);
		codeMap.put((byte) 2, EDGE);
		codeMap.put((byte) 3, HYPEREDGE);
	}
	
	
	private byte code;
	
	CellType(byte code) {
		this.code = code;
	}
	
	public byte getCode() {
		return code;
	}
	
	public static CellType getCellType(byte code) {
		return codeMap.get(code);
	}
}
