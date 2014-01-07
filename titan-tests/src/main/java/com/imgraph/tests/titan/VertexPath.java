package com.imgraph.tests.titan;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class VertexPath {

	private final Vertex vertex;
	private final Edge sourceEdge;
	
	private final VertexPath parentVertexPath;
	private final int depth;
	
	public Vertex getVertex() {
		return vertex;
	}
	
	public Edge getSourceEdge() {
		return sourceEdge;
	}
	
	public VertexPath getParentVertexPath() {
		return parentVertexPath;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public VertexPath(Vertex vertex, Edge sourceEdge,
			VertexPath parentVertexPath, int depth) {
		this.vertex = vertex;
		this.sourceEdge = sourceEdge;
		this.parentVertexPath = parentVertexPath;
		this.depth = depth;
	}
	
	public VertexPath(Vertex vertex) {
		this.vertex = vertex;
		this.sourceEdge = null;
		this.parentVertexPath = null;
		this.depth = 0;
	}
	

}
