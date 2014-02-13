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
package com.steffi.model;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * @author Aldemar Reynaga
 * Implements a group of indexed edges classifying the edges by type and label using maps 
 */
public class SteffiMapEdges implements Serializable, SteffiIndexedEdges {
	/**
	 * 
	 */
	private static final long serialVersionUID = -43000510074768611L;
	
	 
	private TIntObjectHashMap<ArrayList<SteffiEdge>> edgeMap;
	private ArrayList<SteffiEdge> edges;
	

	public SteffiMapEdges() {
	
		edgeMap = new TIntObjectHashMap<ArrayList<SteffiEdge>>();
	
		edges = new ArrayList<SteffiEdge>();
	}

	
	private int calculateTypeNameMapIndex(int edgeCode, int nameIndex) {
		if (nameIndex < 0)
			return 0;
		return edgeCode + (nameIndex+1)*4;
	}
	
	private int calculateNameMapIndex(int nameIndex) {
		if (nameIndex < 0)
			return 0;
		return (nameIndex + 1) * -1;
	}
	
	
	private void addEdgeByKey(SteffiEdge edge, int index) {
		ArrayList<SteffiEdge> edges = edgeMap.get(index);
		if (edges == null) {
			edges = new ArrayList<SteffiEdge>();
			edgeMap.put(index, edges);
		}
		edges.add(edge);
	}
	
	
	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#addEdge(com.imgraph.model.ImgEdge)
	 */
	@Override
	public void addEdge(SteffiEdge edge) {
		
		int typeCode = edge.getEdgeType().getCode();
		edges.add(edge);
	
		addEdgeByKey(edge, typeCode);

		if (edge.getName()!=null && !edge.getName().trim().equals("")) {
			int nameIndex = SteffiGraph.getInstance().getItemNameIndex(edge.getName());
			addEdgeByKey(edge, calculateNameMapIndex(nameIndex));
			addEdgeByKey(edge, calculateTypeNameMapIndex(typeCode, nameIndex));
		}

	}

	
	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#getEdgesByTypeName(com.imgraph.model.EdgeType, java.lang.String)
	 */
	@Override
	public List<SteffiEdge> getEdgesByTypeName(EdgeType edgeType, String name) {
		if (name!=null && !name.trim().equals(""))
			return edgeMap.get(calculateTypeNameMapIndex(edgeType.getCode(), 
					SteffiGraph.getInstance().getItemNameIndex(name)));
		else
			return getEdgesByType(edgeType);
	}

	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#getEdgesByType(com.imgraph.model.EdgeType)
	 */
	@Override
	public List<SteffiEdge> getEdgesByType(EdgeType edgeType) {
		return edgeMap.get(edgeType.getCode());
	}

	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#getEdgesByName(java.lang.String)
	 */
	@Override
	public List<SteffiEdge> getEdgesByName(String name) {
		if (name==null || name.trim().equals(""))
			return getAllEdges();
		return edgeMap.get(calculateNameMapIndex(SteffiGraph.getInstance().getItemNameIndex(name)));
	}
	
	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#getAllEdges()
	 */
	@Override
	public List<SteffiEdge> getAllEdges() {
		return edges;
	}



	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#clear()
	 */
	@Override
	public void clear() {
		for (List<SteffiEdge> edgeSet : edgeMap.valueCollection())
			edgeSet.clear();
		edgeMap.clear();
		edges.clear();

	}

	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#remove(com.imgraph.model.ImgEdge)
	 */
	@Override
	public void remove(SteffiEdge edge) {
		int typeCode = edge.getEdgeType().getCode();
		
		SteffiEdge.removeEdgeFromCollection(edge, edges);
		
		SteffiEdge.removeEdgeFromCollection(edge, edgeMap.get(typeCode));
		
		
		if (edge.getName() != null) {
			int nameIndex = SteffiGraph.getInstance().getItemNameIndex(edge.getName());
			
			SteffiEdge.removeEdgeFromCollection(edge, edgeMap.get(calculateNameMapIndex(nameIndex)));
			SteffiEdge.removeEdgeFromCollection(edge, edgeMap.get(calculateTypeNameMapIndex(typeCode, nameIndex)));
			
		}
				
	}

	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return edges.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#hasMoreThanOneEdge()
	 */
	@Override
	public boolean hasMoreThanOneEdge() {
		return edges.size()>1;
	}
	
	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgIndexedEdges#trimToSize()
	 */
	@Override
	public void trimToSize() {
		for (ArrayList<SteffiEdge> edges : edgeMap.valueCollection())
			edges.trimToSize();
		edges.trimToSize();
		edgeMap.compact();
	}
	

	
	

}
