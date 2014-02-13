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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiVertex;
import com.steffi.traversal.Path;

/**
 * @author Aldemar Reynaga
 * Blueprints implementation for the Path
 */
public class SteffiGraphDBPath  {

	private Path path;
	private SteffiGraphDBGraph graph;
	
	public SteffiGraphDBPath(SteffiGraphDBGraph graph, Path path) {
		this.path = path;
		this.graph = graph;
	}
	
	
	public Iterable<SteffiGraphDBVertex> getVertexes() {
		
		return new Iterable<SteffiGraphDBVertex>() {
			
			@Override
			public Iterator<SteffiGraphDBVertex> iterator() {
				List<SteffiGraphDBVertex> vertices = new LinkedList<SteffiGraphDBVertex>();
				
				
				for (SteffiVertex imgVertex : path.getVertexes())
					vertices.add(new SteffiGraphDBVertex(graph, imgVertex));
				
				return vertices.iterator();
			}
		};
		
		
	}

	
	public Iterable<SteffiGraphDBEdge> getEdges() {
		
		return new Iterable<SteffiGraphDBEdge>() {
			
			@Override
			public Iterator<SteffiGraphDBEdge> iterator() {
				List<SteffiGraphDBEdge> edges = new LinkedList<SteffiGraphDBEdge>();
				for (SteffiEdge imgEdge : path.getEdges())
					edges.add(new SteffiGraphDBEdge(imgEdge, graph));
				return edges.iterator();
			}
		};
	}

	
	public Iterable<Object> getPath() {
		
		return new Iterable<Object>() {
			
			@Override
			public Iterator<Object> iterator() {
				List<Object> pathElements = new LinkedList<Object>();
				
				for (Object element : path.getPath()) {
					if (element instanceof SteffiVertex)
						pathElements.add(new SteffiGraphDBVertex(graph, (SteffiVertex) element));
					else if (element instanceof SteffiEdge)
						pathElements.add(new SteffiGraphDBEdge((SteffiEdge) element, graph));
				}
				
				return pathElements.iterator();
			}
		};
		
		
		
		
	}


	@Override
	public String toString() {
		
		return path.toString();
	}
	
	

}
