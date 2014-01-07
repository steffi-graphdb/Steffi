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
