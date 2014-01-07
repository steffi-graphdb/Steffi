package com.imgraph.traversal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;




/**
 * @author Aldemar Reynaga
 * Interface for traversal results
 */
public interface TraversalResults extends Serializable {
	public List<Path> getPaths();
	
	public void addVertexPath(ReducedVertexPath vertexPath);
	
	public void addManyVertexPaths(Collection<ReducedVertexPath> vertexPaths);
	
	public void clear();
	
	public long getTime();
	
	public void setTime(long time);
	
	public Collection<ReducedVertexPath> getVertexPaths();
	

}
