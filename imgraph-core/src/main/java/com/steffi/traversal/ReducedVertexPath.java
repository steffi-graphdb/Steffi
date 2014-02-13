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
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiVertex;

/**
 * @author Aldemar Reynaga
 * A linked list of vertices and edges representing a path between two vertices  
 */
public class ReducedVertexPath implements Serializable {
	
	
	
	private static final long serialVersionUID = 7806444872906433479L;
	
	private transient SteffiVertex vertex;
	private ReducedVertexPath parentVertexPath;
	private VertexPathElement element;
	
	
	public ReducedVertexPath(long startVertexId) {
		this.element = new VertexPathElement(null, null, startVertexId, 0);
	}
	
	public ReducedVertexPath(ReducedVertexPath parentVertexPath, EdgeType edgeType,
			String edgeName, long destCellId, Integer depth) {
		this.parentVertexPath = parentVertexPath;
		this.element = new VertexPathElement(edgeType, edgeName, destCellId, depth);
	}
	
	
	public SteffiEdge getParentEdge() {
		if (parentVertexPath != null)
			return parentVertexPath.getVertex().getEdge(element.getDestCellId(), element.getEdgeType(),
				element.getEdgeName()); 
		return null;
	}
	
	
	public long getVertexId() {
		return element.getDestCellId();
	}
	
	public int getDepth() {
		return element.getDepth();
	}
	
	public SteffiVertex getVertex(){
		if (vertex==null)
			vertex = (SteffiVertex)SteffiGraph.getInstance().retrieveRawCell(getVertexId());
			//vertex = (ImgVertex) CacheContainer.getCellCache().get(getVertexId());
		return vertex;
	}
	
	public void setVertex(SteffiVertex vertex) {
		this.vertex = vertex;
	}

	public ReducedVertexPath getParentVertexPath() {
		return parentVertexPath;
	}

	public void setParentVertexPath(ReducedVertexPath parentVertexPath) {
		this.parentVertexPath = parentVertexPath;
	}
	
	@Override
	public String toString() {
		String string = "";
		ReducedVertexPath reducedVertexPath = this;
		do {
			if (reducedVertexPath.element.getEdgeType() != null)
				string += ("[" + reducedVertexPath.element.getEdgeType().toString() + "," +
						reducedVertexPath.element.getEdgeName() + "," + 
						reducedVertexPath.element.getDestCellId() + "] ");
			else
				string += ("[,," + reducedVertexPath.element.getDestCellId() + "] ");
			
			reducedVertexPath = reducedVertexPath.getParentVertexPath();
		} while (reducedVertexPath != null);
		
		return string;
	}
	
}
