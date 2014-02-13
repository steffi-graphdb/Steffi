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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.steffi.storage.CellSequence;
import com.steffi.storage.CellTransaction;
import com.steffi.storage.CellTransactionThread;
import com.steffi.storage.EdgeAddressesUpdater;
import com.steffi.storage.StorageTools;

/**
 * @author Aldemar Reynaga
 * Defines a vertex based on the basic cell structure
 */
public class SteffiVertex extends Cell {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7593264602525689457L;
	
	
	private SteffiIndexedEdges[] edgesAddresses;
	
	
	private List<SteffiEdge> edges;
	
	public SteffiVertex(Long id, String name, boolean transactionSupport) {
		super(id, name);
		
		cellType = CellType.VERTEX;
	
		
		edges = new ArrayList<SteffiEdge>();
	
		
		edgesAddresses = new SteffiIndexedEdges[SteffiGraph.getInstance().getNumberOfMembers()];
		
		if (transactionSupport)
			CellTransactionThread.get().createCell(this);
	}
	
	public SteffiVertex(Long id, String name) {
		this(id, name, true);
	}
	
	
	
	protected SteffiVertex(Long id,
			String cellType,
			String name, 
			SteffiGraph parentGraph,
			List<SteffiEdge> edges) {
		super(id, name);
		this.cellType = CellType.VERTEX;
	
		this.edges = edges;
		
	}
	
	
	public SteffiIndexedEdges[] getEdgeAddresses() {
		return edgesAddresses;
	}
	
	
	@Override
	public void trimToSize() {
		super.trimToSize();
		((ArrayList)edges).trimToSize();
		for (SteffiIndexedEdges em : edgesAddresses)
			if (em != null)
				em.trimToSize();
	}
	
	private SteffiIndexedEdges getEdgeMap(String address) {
		int addressIndex = SteffiGraph.getInstance().getMemberIndex(address);
		return edgesAddresses[addressIndex];
	}
	
	public void addEdgeAddress(SteffiEdge edge, String address) {
		SteffiIndexedEdges adrEdges;
		
		int addressIndex = SteffiGraph.getInstance().getMemberIndex(address);
		
		adrEdges = edgesAddresses[addressIndex];
		
		if (adrEdges == null) {
			
			adrEdges = new SteffiMapEdges();
			edgesAddresses[addressIndex] = adrEdges;
		}
		adrEdges.addEdge(edge);
		
	}
	
	public void removeEdgeAddress(SteffiEdge edge, String address) {
		int addressIndex = SteffiGraph.getInstance().getMemberIndex(address);
		edgesAddresses[addressIndex].remove(edge);
	}
	

	public SteffiEdge addEdge(SteffiVertex vertex, boolean isDirected, String name) {
		SteffiGraph.getInstance().validateEdgeName(name);
		long edgeCellId = CellSequence.getNewCellId();
		
		SteffiEdge relOut = new ExtSteffiEdge(this.getId(), vertex.getId(), (isDirected)?EdgeType.OUT:EdgeType.UNDIRECTED, 
				name, edgeCellId);
		SteffiEdge relIn = new ExtSteffiEdge(vertex.getId(), this.getId(), (isDirected)?EdgeType.IN:EdgeType.UNDIRECTED, 
				name, edgeCellId);
		
		
		this.edges.add(relOut);
		EdgeAddressesUpdater.updateEdgeAddress(this, relOut);
		vertex.edges.add(relIn);
		EdgeAddressesUpdater.updateEdgeAddress(vertex, relIn);
		
		
		CellTransactionThread.get().addEdge(this, relOut);
		CellTransactionThread.get().addEdge(vertex, relIn);
		
		
		return relOut;
	}
	
	public void addImgEdge(SteffiEdge imgEdge) {
		edges.add(imgEdge);
		
	}
	
	public SteffiEdge addPartialEdge(long vertexId, EdgeType edgeType, String name) {
		return addPartialEdge(vertexId, edgeType, name, true);
	}
	
	public SteffiEdge addPartialEdge(long vertexId, EdgeType edgeType, String name,
			boolean transactionSupport) {
		SteffiGraph.getInstance().validateEdgeName(name);
		long edgeCellId = CellSequence.getNewCellId();
		SteffiEdge edge = new ExtSteffiEdge(this.getId(), vertexId, edgeType, 
				name, edgeCellId);
		
		
		this.edges.add(edge);
		EdgeAddressesUpdater.updateEdgeAddress(this, edge);
		
		if (transactionSupport)
			CellTransactionThread.get().addEdge(this, edge);
		
		return edge;
	}
	
	public void markNeighborFlags(int addressIndex, long destId, String name) {
		
		for (SteffiEdge extEdge : edgesAddresses[addressIndex].getEdgesByName(name))
			if (extEdge.getDestCellId() == destId)
				((ExtSteffiEdge)extEdge).setNeighborFlag((byte) 1);
		
	}
	
	public SteffiEdge getEdge(long edgeId) {
		
		for (SteffiEdge edge : edges)
			if (edge.getId() == edgeId)
				return edge;
		
		return null;
	}
	
	
	public SteffiEdge getEdge(long destId, EdgeType edgeType, String name) {
		return getEdge(destId, edgeType, name, null);
	}
	
	private SteffiEdge searchEdgeByDestCellId(Collection<SteffiEdge> edgeCollection, long destId) {
		if (edgeCollection != null) {
			for (SteffiEdge edge : edgeCollection)
				if (edge.getDestCellId() == destId )
					return edge;
		}
		return null;
	}
	
	public SteffiEdge getEdge(long destId, EdgeType edgeType, String name, Integer addressIndex) {
		
		if (addressIndex != null) {
			return searchEdgeByDestCellId(edgesAddresses[addressIndex].getEdgesByTypeName(edgeType, name), destId);
		}
		
		for (SteffiIndexedEdges edgeMap : edgesAddresses) {
			if (edgeMap != null) {
				SteffiEdge searchedEdge = searchEdgeByDestCellId(edgeMap.getEdgesByTypeName(edgeType, name), destId);
				
				if (searchedEdge != null)
					return searchedEdge;
				
			}
		}
		
		
		return null;
	}
	
	
	
	public void remove() {
		
		
		SteffiEdge[] edgesToRemove = new SteffiEdge[edges.size()]; 
		
		for (int i=0; i<edges.size(); i++)
			edgesToRemove[i] = edges.get(i);
		
		for (SteffiEdge edge : edgesToRemove) 
			removeEdge(edge);
		
		
		CellTransactionThread.get().removeCell(this);
		
		
		
	}
	
	public void removeEdge(SteffiEdge edge) {
		
		
		if (!SteffiEdge.removeEdgeFromCollection(edge, this.edges))
			throw new RuntimeException("The edge with id " + edge.getId() + " does not exist in vertex " + 
							this.getId());
			
		this.removeEdgeAddress(edge, StorageTools.getCellAddress(edge.getDestCellId()));
		
		SteffiVertex destVertex = (SteffiVertex) SteffiGraph.getInstance().retrieveCell(edge.getDestCellId());
		
		int addressIndex = SteffiGraph.getInstance().getMemberIndex(StorageTools.getCellAddress(edge.getSourceCellId()));
		SteffiEdge invertedEdge = destVertex.getEdge(edge.getSourceCellId(), EdgeType.invertType(edge.getEdgeType()), getName(), 
				addressIndex);
		
		SteffiEdge.removeEdgeFromCollection(invertedEdge, destVertex.edges);
		destVertex.removeEdgeAddress(invertedEdge, StorageTools.getCellAddress(invertedEdge.getSourceCellId()));
		
		CellTransaction cellTransaction = CellTransactionThread.get();
		
		cellTransaction.removeEdge(this, edge);
		cellTransaction.removeEdge(destVertex, invertedEdge);
	}
	
	
	public List<SteffiEdge> getEdges() {
		return edges;
	}
	
	
	public Iterable<SteffiEdge> getEdges(EdgeType edgeType) {
		return getEdges(edgeType, new String[0]);
	}
	
	
	public List<SteffiEdge> getEdgesByType(EdgeType edgeType) {
		List<SteffiEdge> unionEdges = new ArrayList<SteffiEdge>();
		Collection<SteffiEdge> tempEdges = null;
		for (SteffiIndexedEdges em : edgesAddresses) {
			if (em != null) {
				tempEdges = em.getEdgesByType(edgeType); 
				if (tempEdges != null)
					unionEdges.addAll(tempEdges);
			}
		}
		return unionEdges;
	}
	
	public List<SteffiEdge> getEdgesByTypeName(EdgeType edgeType, String name) {
		List<SteffiEdge> unionEdges = new ArrayList<SteffiEdge>();
		Collection<SteffiEdge> tempEdges = null;
		for (SteffiIndexedEdges em : edgesAddresses) {
			if (em != null) {
				tempEdges = em.getEdgesByTypeName(edgeType, name);
				if (tempEdges != null)
					unionEdges.addAll(tempEdges);
			}
		}
			
		return unionEdges;
	}
	
	
	public Iterable<SteffiEdge> getEdges(final EdgeType edgeType, final String labels[]) {
		
		return new Iterable<SteffiEdge>() {
			
			@Override
			public Iterator<SteffiEdge> iterator() {
				if (labels.length == 0) {
					return getEdgesByType(edgeType).iterator();
				} else {
					List<SteffiEdge> list = new LinkedList<SteffiEdge>();
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
		SteffiGraph graph = SteffiGraph.getInstance();
		if (!edges.isEmpty()) {
			string += "\n\tEDGES: ";
			
			for (SteffiEdge edge : edges) {
				string += "\n\t " + edge.toString();
			}
			
	
			for (Entry<String, Integer> memberEntry : graph.getMemberIndexes().entrySet()) {
				
				string += "\n\t" + memberEntry.getKey() + ":";
				
				if (edgesAddresses[memberEntry.getValue()] != null) {
				
					for (SteffiEdge edge : edgesAddresses[memberEntry.getValue()].getAllEdges())
						string += "\n\t\t" + edge.toString();
				}
			}
			
		}
		
		return string;
	}


	
	public List<SteffiEdge> getEdgesByAddress(String address) {
	
		return getEdgeMap(address).getAllEdges();
	}
	
	public SteffiIndexedEdges getEdgeMapByAddress(String address) {
		return getEdgeMap(address);
	}
	
	public Cell clone() {
		SteffiVertex clon =  (SteffiVertex) super.clone();
		
		for (SteffiEdge edge : this.edges) { 
			SteffiEdge clonedEdge = (SteffiEdge) edge.clone();
			clon.edges.add(clonedEdge);
			EdgeAddressesUpdater.updateEdgeAddress(clon, clonedEdge);
		}
		
		return clon;
	}
	
	
	
	
	
	
	
	
}
