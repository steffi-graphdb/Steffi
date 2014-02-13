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
package com.steffi.model;

import java.io.Serializable;



/**
 * @author Aldemar Reynaga
 * Models an edge including a bit flag that indicates if there is a virtual edge
 */
public class ExtSteffiEdge extends SteffiEdge implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6036619926344459982L;
	private byte neighborFlag;
	
	
	
	
	public ExtSteffiEdge(long sourceCellId,
			long destCellId,
			EdgeType edgeType,
			String name,
			Long edgeCellId) {
		super(sourceCellId, destCellId, edgeType, name, edgeCellId);
		this.neighborFlag = 0;
	}

	public ExtSteffiEdge(SteffiEdge edge) {
		super(edge.getSourceCellId(), edge.getDestCellId(), edge.getEdgeType(),
				edge.getName(), edge.getId());
		this.neighborFlag = 0;
	}
	
	public ExtSteffiEdge(long id, String name) {
		super(id, name);
	}
	
	
	public byte getNeighborFlag() {
		return neighborFlag;
	}
	public void setNeighborFlag(byte neighborFlag) {
		this.neighborFlag = neighborFlag;
	}
	
	
	
	
	@Override
	public Cell clone() {
		ExtSteffiEdge clon = (ExtSteffiEdge) super.clone();
		clon.neighborFlag = this.neighborFlag;
		return clon;
	}

	@Override
	public String toString() {
		return "[" + super.toString() + " , " + neighborFlag + "]"; 
	}

	
	
}
