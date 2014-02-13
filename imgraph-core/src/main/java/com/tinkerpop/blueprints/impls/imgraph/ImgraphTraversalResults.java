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
package com.tinkerpop.blueprints.impls.imgraph;


import java.util.LinkedList;
import java.util.List;

import com.imgraph.traversal.Path;
import com.imgraph.traversal.TraversalResults;

/**
 * @author Aldemar Reynaga
 * Wrapper for the traversal results
 */
public class ImgraphTraversalResults {

	private TraversalResults traversalResults;
	private ImgraphGraph graph;

	public ImgraphTraversalResults(ImgraphGraph graph, TraversalResults traversalResults) {
		this.traversalResults = traversalResults; 
		this.graph = graph;
	}
	
	
	public List<ImgraphPath> getPaths() {
		
		List<ImgraphPath> paths = new LinkedList<ImgraphPath>();
		
		for (Path path : traversalResults.getPaths()) 
			paths.add(new ImgraphPath(graph, path));
		
		
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
