package com.imgraph.traversal;

import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;

/**
 * @author Aldemar Reynaga
 * Interface for the Path results of taversals
 */
public interface Path {
	public Iterable<ImgVertex> getVertexes();
	public Iterable<ImgEdge> getEdges();
	public Iterable<Object> getPath();
	public String toString();
}
