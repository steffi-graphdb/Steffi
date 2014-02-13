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
package com.imgraph.traversal;

import java.io.Serializable;

import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;

/**
 * @author Aldemar Reynaga
 * A linked list of vertices and edges representing a path between two vertices  
 */
public class ReducedVertexPath implements Serializable {
	
	
	
	private static final long serialVersionUID = 7806444872906433479L;
	
	private transient ImgVertex vertex;
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
	
	
	public ImgEdge getParentEdge() {
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
	
	public ImgVertex getVertex(){
		if (vertex==null)
			vertex = (ImgVertex)ImgGraph.getInstance().retrieveRawCell(getVertexId());
			//vertex = (ImgVertex) CacheContainer.getCellCache().get(getVertexId());
		return vertex;
	}
	
	public void setVertex(ImgVertex vertex) {
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
