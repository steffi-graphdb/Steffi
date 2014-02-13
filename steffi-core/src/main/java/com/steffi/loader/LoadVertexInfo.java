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
package com.steffi.loader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Aldemar Reynaga
 * Represents a vertex in the graph to be loaded, contains the vertex Id and its edges
 */
public class LoadVertexInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1452491836311252046L;
	private long vertexId;
	private List<Long> inEdges;
	private List<Long> outEdges;
	private List<Long> undirectedEdges;
	
	@Override
	public String toString() {
		return "LoadVertexInfo [vertexId=" + vertexId + ", inEdges=" + inEdges
				+ ", outEdges=" + outEdges + "]";
	}

	public LoadVertexInfo() {
		
	}
	
	public LoadVertexInfo(long vertexId) {
		inEdges = new ArrayList<Long>();
		outEdges = new ArrayList<Long>();
		undirectedEdges = new ArrayList<Long>();
		this.vertexId = vertexId;
	}
	
	public void setVertexId(long vertexId) {
		this.vertexId = vertexId;
	}
	
	public void clearEdges() {
		inEdges.clear();
		outEdges.clear();
		undirectedEdges.clear();
	}
	
	public long getVertexId() {
		return vertexId;
	}
	
	
	public List<Long> getInEdges() {
		return inEdges;
	}

	public List<Long> getOutEdges() {
		return outEdges;
	}

	public void addInEdge(long edgeId) {
		this.inEdges.add(edgeId);
	}
	
	public void addOutEdge(long edgeId) {
		this.outEdges.add(edgeId);
	}
	
	public void addUndirectedEdge(long edgeId) {
		this.undirectedEdges.add(edgeId);
	}
	

	public List<Long> getUndirectedEdges() {
		return undirectedEdges;
	}
	
}
