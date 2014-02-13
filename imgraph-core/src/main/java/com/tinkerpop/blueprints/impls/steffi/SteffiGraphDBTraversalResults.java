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
package com.tinkerpop.blueprints.impls.steffi;


import java.util.LinkedList;
import java.util.List;

import com.steffi.traversal.Path;
import com.steffi.traversal.TraversalResults;

/**
 * @author Aldemar Reynaga
 * Wrapper for the traversal results
 */
public class SteffiGraphDBTraversalResults {

	private TraversalResults traversalResults;
	private SteffiGraphDBGraph graph;

	public SteffiGraphDBTraversalResults(SteffiGraphDBGraph graph, TraversalResults traversalResults) {
		this.traversalResults = traversalResults; 
		this.graph = graph;
	}
	
	
	public List<SteffiGraphDBPath> getPaths() {
		
		List<SteffiGraphDBPath> paths = new LinkedList<SteffiGraphDBPath>();
		
		for (Path path : traversalResults.getPaths()) 
			paths.add(new SteffiGraphDBPath(graph, path));
		
		
		return paths;
		
		
		
	}

	
	public long getTime() {
		return traversalResults.getTime();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Time: " + traversalResults.getTime() + "ms\n");
		
		if (traversalResults.getPaths().isEmpty())
			sb.append("No traversal results found");
		else
			for (Path path : traversalResults.getPaths())
				sb.append("\t").append(path).append("\n");
		
		return sb.toString();
					
	}
		
}
