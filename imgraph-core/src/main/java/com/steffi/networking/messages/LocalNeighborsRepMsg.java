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
package com.steffi.networking.messages;

import java.util.HashMap;
import java.util.Map;

import com.steffi.model.SteffiIndexedEdges;

public class LocalNeighborsRepMsg extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3673369718973597416L;
	private Map<Long, SteffiIndexedEdges> vertexEdgeMap;
	
	public LocalNeighborsRepMsg() {
		super(MessageType.LOCAL_NEIGHBORS_REP);
		vertexEdgeMap = new HashMap<Long, SteffiIndexedEdges>();
	}

	public Map<Long, SteffiIndexedEdges> getVertexEdgeMap() {
		return vertexEdgeMap;
	}

	public void setVertexEdgeMap(Map<Long, SteffiIndexedEdges> vertexEdgeMap) {
		this.vertexEdgeMap = vertexEdgeMap;
	}

		

		
}
