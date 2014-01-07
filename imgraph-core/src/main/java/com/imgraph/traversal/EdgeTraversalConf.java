package com.imgraph.traversal;

import java.io.Serializable;

import com.imgraph.model.EdgeType;


/**
 * @author Aldemar Reynaga
 * Configuration of edges for the traversal
 */
public class EdgeTraversalConf implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1894624012493181977L;
	private String name;
	private EdgeType edgeType;
	
	
	
	public EdgeTraversalConf(String name, EdgeType edgeType) {
		this.name = name;
		this.edgeType = edgeType;
	}
	
	
	
	public EdgeTraversalConf(String name) {
		this.name = name;
		this.edgeType = EdgeType.UNDIRECTED;
		
		
	}
	
	
	
	
	
	@Override
	public String toString() {
		return "EdgeTraversalConf [name=" + name + ", edgeType=" + edgeType
				+ "]";
	}


	public String getName() {
		return name;
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}
	
		
}
