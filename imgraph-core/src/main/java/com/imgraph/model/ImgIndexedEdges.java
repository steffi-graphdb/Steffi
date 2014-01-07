package com.imgraph.model;

import java.util.List;



/**
 * @author Aldemar Reynaga
 * Defines the required functionalities of the model of containing a group of indexed edges that will be stored inside a vertex 
 */
public interface ImgIndexedEdges {

	public abstract void addEdge(ImgEdge edge);

	public abstract List<ImgEdge> getEdgesByTypeName(EdgeType edgeType,
			String name);

	public abstract List<ImgEdge> getEdgesByType(EdgeType edgeType);

	public abstract List<ImgEdge> getEdgesByName(String name);

	public abstract List<ImgEdge> getAllEdges();

	public abstract void clear();

	public abstract void remove(ImgEdge edge);

	public abstract boolean isEmpty();

	public abstract boolean hasMoreThanOneEdge();

	public abstract void trimToSize();

	
	

}