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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;



/**
 * @author Aldemar Reynaga
 * Implementation of the TraversalResults interface using a group of ReducedVertexPath instances
 */
public class TraversalResultsImpl implements TraversalResults {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9002084374012588274L;
	private List<ReducedVertexPath> vertexPaths;
	private long time;
	
	public TraversalResultsImpl() {
		vertexPaths = new ArrayList<ReducedVertexPath>();
	}
	
	
	
	public TraversalResultsImpl(List<ReducedVertexPath> vertexPaths, long time) {
		super();
		this.vertexPaths = vertexPaths;
		this.time = time;
	}



	@Override
	public void clear() {
		vertexPaths.clear();
	}
	
	public List<Path> getPaths() {
		
		List<Path> paths = new LinkedList<Path>();
		
		for (ReducedVertexPath vertexPath : vertexPaths) 
			paths.add(new PathImpl(vertexPath));
		
		
		return paths;
	}


	@Override
	public void addVertexPath(ReducedVertexPath vertexPath) {
		this.vertexPaths.add(vertexPath);
	}

	@Override
	public void addManyVertexPaths(Collection<ReducedVertexPath> vertexPaths) {
		this.vertexPaths.addAll(vertexPaths);
	}

	@Override
	public Collection<ReducedVertexPath> getVertexPaths() {
		return this.vertexPaths;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public String toString() {
		String string = null;
		
		string += ("[time=" + time + ", vertexPaths=" + vertexPaths + "]");
		
		return string;
	}
	
	
	

}
