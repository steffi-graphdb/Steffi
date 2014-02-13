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
