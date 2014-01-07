package com.imgraph.traversal;

import java.io.Serializable;

import com.imgraph.model.EdgeType;

/**
 * @author Aldemar Reynaga
 * Element of the ReducedVertexPath linked list 
 */
public class VertexPathElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8535164074892304269L;
	private EdgeType edgeType;
	private String edgeName;
	private long destCellId;
	private int depth;
	
	public VertexPathElement() {
		
	}
	
	public VertexPathElement(EdgeType edgeType, String edgeName,
			long destCellId, int depth) {
		
		this.edgeType = edgeType;
		this.edgeName = edgeName;
		this.destCellId = destCellId;
		this.depth = depth;
	}
	public EdgeType getEdgeType() {
		return edgeType;
	}
	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}
	public String getEdgeName() {
		return edgeName;
	}
	public void setEdgeName(String edgeName) {
		this.edgeName = edgeName;
	}
	public long getDestCellId() {
		return destCellId;
	}
	public void setDestCellId(long destCellId) {
		this.destCellId = destCellId;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	
	
	
}
