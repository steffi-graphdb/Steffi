package com.imgraph.traversal;

import gnu.trove.set.TLongSet;

import java.io.Serializable;



/**
 * @author Aldemar Reynaga
 * Information sent to data servers to initiate a sub traversal
 */
public class SearchVertexInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1611380072079540037L;
	private ReducedVertexPath vertexPath;
	private TLongSet explored2HNeighbors;
	
	public ReducedVertexPath getVertexPath() {
		return vertexPath;
	}
	public void setVertexPath(ReducedVertexPath vertexPath) {
		this.vertexPath = vertexPath;
	}
	public TLongSet getExplored2HNeighbors() {
		return explored2HNeighbors;
	}
	public void setExplored2HNeighbors(TLongSet explored2HNeighbors) {
		this.explored2HNeighbors = explored2HNeighbors;
	}
	public Long getVertexId() {
		return vertexPath.getVertexId();
	}
	public int getDepth() {
		return vertexPath.getDepth();
	}
		
}
