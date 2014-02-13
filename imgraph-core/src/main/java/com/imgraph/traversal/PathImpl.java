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
package com.imgraph.traversal;

import java.util.Iterator;
import java.util.LinkedList;

import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;

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
	public Iterable<ImgVertex> getVertexes() {
		return new Iterable<ImgVertex>() {
			
			@Override
			public Iterator<ImgVertex> iterator() {
				
				LinkedList<ImgVertex> vertexes = new LinkedList<ImgVertex>();
				
				for (int i=0; i<path.size(); i+=2) 
					vertexes.add((ImgVertex) path.get(i));
				
				return vertexes.iterator();
			}
		};
	}

	@Override
	public Iterable<ImgEdge> getEdges() {
		return new Iterable<ImgEdge>() {
			
			@Override
			public Iterator<ImgEdge> iterator() {
				LinkedList<ImgEdge> edges = new LinkedList<ImgEdge>();
				
				for (int i=1; i<path.size(); i+=2) 
					edges.add((ImgEdge) path.get(i));
				
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
			if (item instanceof ImgVertex)
				string += "[" + ((ImgVertex) item).getId() + "]";
			else if (item instanceof ImgEdge) {
				String edgeName = (((ImgEdge) item).getName()==null)?"":((ImgEdge) item).getName();
				if (((ImgEdge) item).getEdgeType().equals(EdgeType.IN))
					string += "<" + edgeName + "-";
				else if (((ImgEdge) item).getEdgeType().equals(EdgeType.OUT))
					string += "-" + edgeName + ">";
				else 
					string += "-" + edgeName+ "-";
			}
		}
		
		return string;
	}
	
	

}
