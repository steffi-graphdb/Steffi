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

import java.util.Iterator;
import java.util.LinkedList;

import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiVertex;

/**
 * @author Aldemar Reynaga
 * Implementation of the Path interface using the ReducedVertexPath class
 */
public class PathImpl implements Path{

	
	private LinkedList<Object> path;
	
	
	public PathImpl(ReducedVertexPath vertexPath) {
		path = new LinkedList<Object>();
		ReducedVertexPath curVertexPath = vertexPath;
		
		do {
			path.addFirst(curVertexPath.getVertex());
			path.addFirst(curVertexPath.getParentEdge());
			curVertexPath = curVertexPath.getParentVertexPath();
		} while (curVertexPath != null);
		
		
	}

	@Override
	public Iterable<SteffiVertex> getVertexes() {
		return new Iterable<SteffiVertex>() {
			
			@Override
			public Iterator<SteffiVertex> iterator() {
				
				LinkedList<SteffiVertex> vertexes = new LinkedList<SteffiVertex>();
				
				for (int i=0; i<path.size(); i+=2) 
					vertexes.add((SteffiVertex) path.get(i));
				
				return vertexes.iterator();
			}
		};
	}

	@Override
	public Iterable<SteffiEdge> getEdges() {
		return new Iterable<SteffiEdge>() {
			
			@Override
			public Iterator<SteffiEdge> iterator() {
				LinkedList<SteffiEdge> edges = new LinkedList<SteffiEdge>();
				
				for (int i=1; i<path.size(); i+=2) 
					edges.add((SteffiEdge) path.get(i));
				
				return edges.iterator();
			}
		};
	}

	@Override
	public Iterable<Object> getPath() {
		return new Iterable<Object>() {
			
			@Override
			public Iterator<Object> iterator() {
				return path.iterator();
			}
		};
	}

	@Override
	public String toString() {
		String string = "";
		
		for (Object item : path) {
			if (item instanceof SteffiVertex)
				string += "[" + ((SteffiVertex) item).getId() + "]";
			else if (item instanceof SteffiEdge) {
				String edgeName = (((SteffiEdge) item).getName()==null)?"":((SteffiEdge) item).getName();
				if (((SteffiEdge) item).getEdgeType().equals(EdgeType.IN))
					string += "<" + edgeName + "-";
				else if (((SteffiEdge) item).getEdgeType().equals(EdgeType.OUT))
					string += "-" + edgeName + ">";
				else 
					string += "-" + edgeName+ "-";
			}
		}
		
		return string;
	}
	
	

}
