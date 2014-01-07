package com.imgraph.storage;

import com.imgraph.model.ImgGraph;

/**
 * @author Aldemar Reynaga
 * Autogenerator of cell ids
 */
public class CellSequence {
	
	private long sequence;
	private long baseValue;
	
	private static class SingletonHolder { 
        public static final CellSequence instance = new CellSequence();
	}
	
	private CellSequence() {
		
		int localhostIndex = ImgGraph.getInstance().getLocalAddressIndex();
		sequence = 0;
		baseValue = ((((long)1) << 14) | ((long)localhostIndex)) << 48;  
	}
	
	
	private long getNextValue() {
		sequence++;
		return baseValue | sequence;
	}
	
	
	public static synchronized long getNewCellId() {
		return SingletonHolder.instance.getNextValue();
	}
	
	
}
