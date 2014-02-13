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
package com.imgraph.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.imgraph.storage.CellSequence;
import com.imgraph.storage.CellTransaction;
import com.imgraph.storage.CellTransactionThread;
import com.imgraph.storage.EdgeAddressesUpdater;
import com.imgraph.storage.StorageTools;

/**
 * @author Aldemar Reynaga
 * Defines a vertex based on the basic cell structure
 */
public class ImgVertex extends Cell {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7593264602525689457L;
	
	
	private ImgIndexedEdges[] edgesAddresses;
	
	
	private List<ImgEdge> edges;
	
	public ImgVertex(Long id, String name, boolean transactionSupport) {
		super(id, name);
		
		cellType = CellType.VERTEX;
	
		
		edges = new ArrayList<ImgEdge>();
	
		
		edgesAddresses = new ImgIndexedEdges[ImgGraph.getInstance().getNumberOfMembers()];
		
		if (transactionSupport)
			CellTransactionThread.get().createCell(this);
	}
	
	public ImgVertex(Long id, String name) {
		this(id, name, true);
	}
	
	
	
	protected ImgVertex(Long id,
			String cellType,
			String name, 
			ImgGraph parentGraph,
			List<ImgEdge> edges) {
		super(id, name);
		this.cellType = CellType.VERTEX;
	
		this.edges = edges;
		
	}
	
	
	public ImgIndexedEdges[] getEdgeAddresses() {
		return edgesAddresses;
	}
	
	
	@Override
	public void trimToSize() {
		super.trimToSize();
		((ArrayList)edges).trimToSize();
		for (ImgIndexedEdges em : edgesAddresses)
			if (em != null)
				em.trimToSize();
	}
	
	private ImgIndexedEdges getEdgeMap(String address) {
		int addressIndex = ImgGraph.getInstance().getMemberIndex(address);
		return edgesAddresses[addressIndex];
	}
	
	public void addEdgeAddress(ImgEdge edge, String address) {
		ImgIndexedEdges adrEdges;
		
		int addressIndex = ImgGraph.getInstance().getMemberIndex(address);
		
		adrEdges = edgesAddresses[addressIndex];
		
		if (adrEdges == null) {
			
			adrEdges = new ImgMapEdges();
			edgesAddresses[addressIndex] = adrEdges;
		}
		adrEdges.addEdge(edge);
		
	}
	
	public void removeEdgeAddress(ImgEdge edge, String address) {
		int addressIndex = ImgGraph.getInstance().getMemberIndex(address);
		edgesAddresses[addressIndex].remove(edge);
	}
	

	public ImgEdge addEdge(ImgVertex vertex, boolean isDirected, String name) {
		ImgGraph.getInstance().validateEdgeName(name);
		long edgeCellId = CellSequence.getNewCellId();
		
		ImgEdge relOut = new ExtImgEdge(this.getId(), vertex.getId(), (isDirected)?EdgeType.OUT:EdgeType.UNDIRECTED, 
				name, edgeCellId);
		ImgEdge relIn = new ExtImgEdge(vertex.getId(), this.getId(), (isDirected)?EdgeType.IN:EdgeType.UNDIRECTED, 
				name, edgeCellId);
		
		
		this.edges.add(relOut);
		EdgeAddressesUpdater.updateEdgeAddress(this, relOut);
		vertex.edges.add(relIn);
		EdgeAddressesUpdater.updateEdgeAddress(vertex, relIn);
		
		
		CellTransactionThread.get().addEdge(this, relOut);
		CellTransactionThread.get().addEdge(vertex, relIn);
		
		
		return relOut;
	}
	
	public void addImgEdge(ImgEdge imgEdge) {
		edges.add(imgEdge);
		
	}
	
	public ImgEdge addPartialEdge(long vertexId, EdgeType edgeType, String name) {
		return addPartialEdge(vertexId, edgeType, name, true);
	}
	
	public ImgEdge addPartialEdge(long vertexId, EdgeType edgeType, String name,
			boolean transactionSupport) {
		ImgGraph.getInstance().validateEdgeName(name);
		long edgeCellId = CellSequence.getNewCellId();
		ImgEdge edge = new ExtImgEdge(this.getId(), vertexId, edgeType, 
				name, edgeCellId);
		
		
		this.edges.add(edge);
		EdgeAddressesUpdater.updateEdgeAddress(this, edge);
		
		if (transactionSupport)
			CellTransactionThread.get().addEdge(this, edge);
		
		return edge;
	}
	
	public void markNeighborFlags(int addressIndex, long destId, String name) {
		
		for (ImgEdge extEdge : edgesAddresses[addressIndex].getEdgesByName(name))
			if (extEdge.getDestCellId() == destId)
				((ExtImgEdge)extEdge).setNeighborFlag((byte) 1);
		
	}
	
	public ImgEdge getEdge(long edgeId) {
		
		for (ImgEdge edge : edges)
			if (edge.getId() == edgeId)
				return edge;
		
		return null;
	}
	
	
	public ImgEdge getEdge(long destId, EdgeType edgeType, String name) {
		return getEdge(destId, edgeType, name, null);
	}
	
	private ImgEdge searchEdgeByDestCellId(Collection<ImgEdge> edgeCollection, long destId) {
		if (edgeCollection != null) {
			for (ImgEdge edge : edgeCollection)
				if (edge.getDestCellId() == destId )
					return edge;
		}
		return null;
	}
	
	public ImgEdge getEdge(long destId, EdgeType edgeType, String name, Integer addressIndex) {
		
		if (addressIndex != null) {
			return searchEdgeByDestCellId(edgesAddresses[addressIndex].getEdgesByTypeName(edgeType, name), destId);
		}
		
		for (ImgIndexedEdges edgeMap : edgesAddresses) {
			if (edgeMap != null) {
				ImgEdge searchedEdge = searchEdgeByDestCellId(edgeMap.getEdgesByTypeName(edgeType, name), destId);
				
				if (searchedEdge != null)
					return searchedEdge;
				
			}
		}
		
		
		return null;
	}
	
	
	
	public void remove() {
		
		
		ImgEdge[] edgesToRemove = new ImgEdge[edges.size()]; 
		
		for (int i=0; i<edges.size(); i++)
			edgesToRemove[i] = edges.get(i);
		
		for (ImgEdge edge : edgesToRemove) 
			removeEdge(edge);
		
		
		CellTransactionThread.get().removeCell(this);
		
		
		
	}
	
	public void removeEdge(ImgEdge edge) {
		
		
		if (!ImgEdge.removeEdgeFromCollection(edge, this.edges))
			throw new RuntimeException("The edge with id " + edge.getId() + " does not exist in vertex " + 
							this.getId());
			
		this.removeEdgeAddress(edge, StorageTools.getCellAddress(edge.getDestCellId()));
		
		ImgVertex destVertex = (ImgVertex) ImgGraph.getInstance().retrieveCell(edge.getDestCellId());
		
		int addressIndex = ImgGraph.getInstance().getMemberIndex(StorageTools.getCellAddress(edge.getSourceCellId()));
		ImgEdge invertedEdge = destVertex.getEdge(edge.getSourceCellId(), EdgeType.invertType(edge.getEdgeType()), getName(), 
				addressIndex);
		
		ImgEdge.removeEdgeFromCollection(invertedEdge, destVertex.edges);
		destVertex.removeEdgeAddress(invertedEdge, StorageTools.getCellAddress(invertedEdge.getSourceCellId()));
		
		CellTransaction cellTransaction = CellTransactionThread.get();
		
		cellTransaction.removeEdge(this, edge);
		cellTransaction.removeEdge(destVertex, invertedEdge);
	}
	
	
	public List<ImgEdge> getEdges() {
		return edges;
	}
	
	
	public Iterable<ImgEdge> getEdges(EdgeType edgeType) {
		return getEdges(edgeType, new String[0]);
	}
	
	
	public List<ImgEdge> getEdgesByType(EdgeType edgeType) {
		List<ImgEdge> unionEdges = new ArrayList<ImgEdge>();
		Collection<ImgEdge> tempEdges = null;
		for (ImgIndexedEdges em : edgesAddresses) {
			if (em != null) {
				tempEdges = em.getEdgesByType(edgeType); 
				if (tempEdges != null)
					unionEdges.addAll(tempEdges);
			}
		}
		return unionEdges;
	}
	
	public List<ImgEdge> getEdgesByTypeName(EdgeType edgeType, String name) {
		List<ImgEdge> unionEdges = new ArrayList<ImgEdge>();
		Collection<ImgEdge> tempEdges = null;
		for (ImgIndexedEdges em : edgesAddresses) {
			if (em != null) {
				tempEdges = em.getEdgesByTypeName(edgeType, name);
				if (tempEdges != null)
					unionEdges.addAll(tempEdges);
			}
		}
			
		return unionEdges;
	}
	
	
	public Iterable<ImgEdge> getEdges(final EdgeType edgeType, final String labels[]) {
		
		return new Iterable<ImgEdge>() {
			
			@Override
			public Iterator<ImgEdge> iterator() {
				if (labels.length == 0) {
					return getEdgesByType(edgeType).iterator();
				} else {
					List<ImgEdge> list = new LinkedList<ImgEdge>();
					for (String label : labels) 
						list.addAll(getEdgesByTypeName(edgeType, label));
					return list.iterator();
				}
			}
		};
	}
	
	
	public String toString() {
		String string = "";
		
		string = super.toString();
		ImgGraph graph = ImgGraph.getInstance();
		if (!edges.isEmpty()) {
			string += "\n\tEDGES: ";
			
			for (ImgEdge edge : edges) {
				string += "\n\t " + edge.toString();
			}
			
	
			for (Entry<String, Integer> memberEntry : graph.getMemberIndexes().entrySet()) {
				
				string += "\n\t" + memberEntry.getKey() + ":";
				
				if (edgesAddresses[memberEntry.getValue()] != null) {
				
					for (ImgEdge edge : edgesAddresses[memberEntry.getValue()].getAllEdges())
						string += "\n\t\t" + edge.toString();
				}
			}
			
		}
		
		return string;
	}


	
	public List<ImgEdge> getEdgesByAddress(String address) {
	
		return getEdgeMap(address).getAllEdges();
	}
	
	public ImgIndexedEdges getEdgeMapByAddress(String address) {
		return getEdgeMap(address);
	}
	
	public Cell clone() {
		ImgVertex clon =  (ImgVertex) super.clone();
		
		for (ImgEdge edge : this.edges) { 
			ImgEdge clonedEdge = (ImgEdge) edge.clone();
			clon.edges.add(clonedEdge);
			EdgeAddressesUpdater.updateEdgeAddress(clon, clonedEdge);
		}
		
		return clon;
	}
	
	
	
	
	
	
	
	
}
