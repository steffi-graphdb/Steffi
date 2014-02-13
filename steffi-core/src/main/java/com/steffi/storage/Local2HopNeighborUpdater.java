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
package com.steffi.storage;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.steffi.loader.ResponseProcessor;
import com.steffi.model.ExtSteffiEdge;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiIndexedEdges;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.Update2HNReqMsg;

/**
 * @author Aldemar Reynaga
 * Calculates the virtual edges for the vertices affected in a transaction
 */
public class Local2HopNeighborUpdater implements ResponseProcessor {
	
	private int pendingMessages;
	private boolean allMessagesSent;
	private boolean processing;
	private Object lock;
	private Map<Integer,TLongObjectMap<SteffiIndexedEdges>> added2HNRequest ;
	private Map<Integer, List<SteffiEdge>> removed2HNRequest;
	
	public Local2HopNeighborUpdater() {
		lock = new String(this.getClass().getName());
	}
	
	private synchronized void updatePendingMessages(int update) {
		this.pendingMessages += update;
	}
	
	@Override
	public void processResponse(Message message) {
		if (message.getBody().equals("OK")) {
			updatePendingMessages(-1);
			verifyProcessCompleted();
		} else {
			throw new RuntimeException("Error processing an update request");
		}
		
	}
	
	private void verifyProcessCompleted() {
		if (allMessagesSent && pendingMessages==0) {
			synchronized (lock) {
				processing = false;
				lock.notifyAll();
			}
		} 
	}
	
	
	private void sendUpdate2HNRequests() throws Exception {
		pendingMessages = 0;
		allMessagesSent = false;
		processing = true;
		Map<Integer, Update2HNReqMsg> messages =  new HashMap<Integer, Update2HNReqMsg>();
		
		
		for (Entry<Integer, TLongObjectMap<SteffiIndexedEdges>> entry : added2HNRequest.entrySet()) {
			Update2HNReqMsg updateMsg = new Update2HNReqMsg();
			updateMsg.setLocal2HNUpd(entry.getValue());
			updateMsg.setId(UUID.randomUUID());
			messages.put(entry.getKey(), updateMsg);
		}
		
		for (Entry<Integer, List<SteffiEdge>> entry : removed2HNRequest.entrySet()) {
			Update2HNReqMsg updateMsg = messages.get(entry.getKey());
			if (updateMsg == null) {
				updateMsg = new Update2HNReqMsg();
				updateMsg.setId(UUID.randomUUID());
				messages.put(entry.getKey(), updateMsg);
			}
			updateMsg.setRemovedEdges(entry.getValue());
			
		}
		
		for (Entry<Integer, Update2HNReqMsg> entry : messages.entrySet()) {
			
			SteffiGraph.getInstance().sendMessageToNode(entry.getKey(), entry.getValue(), this);
			updatePendingMessages(1);
		}
		
		allMessagesSent = true;
		
		verifyProcessCompleted();
		
		synchronized (lock) {
			while (processing) {
				lock.wait();
			}
		}
		
	}
	
	private void update2HNForEdge(SteffiIndexedEdges edgeMap, int vertexAddressIndex, int destVertexAddressIndex,
			Map<Long, CellOperations> cellOperations) {
		for (SteffiEdge edge : edgeMap.getAllEdges()) {
			if (!local2HNContainsId(vertexAddressIndex, edge.getDestCellId())) {
				CellOperations cellOperation = cellOperations.get(edge.getDestCellId());
				
				
				SteffiVertex vertex = (SteffiVertex) cellOperation.getCell();
				SteffiIndexedEdges [] edgeAddresses = vertex.getEdgeAddresses(); 
				
				SteffiIndexedEdges destCellEdges = edgeAddresses[vertexAddressIndex]; 
				if (destCellEdges.hasMoreThanOneEdge()) 
					getAdded2HNReq(vertexAddressIndex).put(edge.getDestCellId(), destCellEdges);
			}
		}
		
	}
	
	private boolean local2HNContainsId(int vertexAddressIndex, long cellId) {
		TLongObjectMap<SteffiIndexedEdges> local2HN = added2HNRequest.get(vertexAddressIndex);
		if (local2HN == null || !local2HN.containsKey(cellId))
			return false;
		return true;
	}
	
	private TLongObjectMap<SteffiIndexedEdges> getAdded2HNReq(int vertexAddressIndex) {
		TLongObjectMap<SteffiIndexedEdges> local2HN = added2HNRequest.get(vertexAddressIndex);
		if (local2HN == null) {
			local2HN = new TLongObjectHashMap<SteffiIndexedEdges>();
			added2HNRequest.put(vertexAddressIndex, local2HN);
		}
		return local2HN;
	}
	
	private List<SteffiEdge> getRemoved2HNReq(int vertexAddressIndex) {
		List<SteffiEdge> indexedEdges = removed2HNRequest.get(vertexAddressIndex);
		
		if (indexedEdges == null) {
			indexedEdges = new ArrayList<SteffiEdge>();
			removed2HNRequest.put(vertexAddressIndex, indexedEdges);
		}
		
		return indexedEdges;
	}
	
	
	public void update2HNList(Map<Long, CellOperations> cellOperations) throws Exception {
		SteffiGraph graph = SteffiGraph.getInstance();
		added2HNRequest =  new HashMap<Integer,TLongObjectMap<SteffiIndexedEdges>>();
		removed2HNRequest = new HashMap<Integer, List<SteffiEdge>>();
		
		int vertexAddressIndex, destVertexAddressIndex;
		
		for (CellOperations cellOp : cellOperations.values()) {
			for (CellOperationType cellOpType : cellOp.getTypes()) {
				
				switch(cellOpType) {
				case ADD_EDGE:
					vertexAddressIndex = graph.getMemberIndex(StorageTools.getCellAddress(cellOp.getCellId()));
					for (SteffiEdge edge : cellOp.getNewEdges()) {
						if (!local2HNContainsId(vertexAddressIndex, edge.getDestCellId())) {
							destVertexAddressIndex = graph.getMemberIndex(StorageTools.getCellAddress(edge.getDestCellId()));
							if (vertexAddressIndex != destVertexAddressIndex) {
								SteffiVertex destVertex = (SteffiVertex) cellOperations.get(edge.getDestCellId()).getCell();
								SteffiIndexedEdges edgeMap = destVertex.getEdgeAddresses()[vertexAddressIndex];
								if (edgeMap != null && edgeMap.hasMoreThanOneEdge()) {
									getAdded2HNReq(vertexAddressIndex).put(edge.getDestCellId(), edgeMap);
									
								}
							}
						}
					}
					break;
				case CREATE_CELL:
					SteffiVertex vertex = (SteffiVertex) cellOp.getCell();
					vertexAddressIndex = graph.getMemberIndex(StorageTools.getCellAddress(vertex.getId()));
					
					for (int i=0; i<graph.getNumberOfMembers(); i++) {
						if (i != vertexAddressIndex) {
							SteffiIndexedEdges edgeMap = vertex.getEdgeAddresses()[i];
							if (edgeMap != null)
								update2HNForEdge(edgeMap, vertexAddressIndex, i, cellOperations);
						}
					}
					break;
				case REMOVE_EDGE:
					for (SteffiEdge edge : cellOp.getRemovedEdges()) {
						ExtSteffiEdge extImgEdge = (ExtSteffiEdge) edge;
						vertexAddressIndex = graph.getMemberIndex(StorageTools.getCellAddress(cellOp.getCellId()));
						if (extImgEdge.getNeighborFlag() == 1) 
							getRemoved2HNReq(vertexAddressIndex).add(extImgEdge);
					}
					break;
				default:
					break;
				
				}
			}
			
		}
		sendUpdate2HNRequests();
	}
	
	public static void processUpdateRequest(Update2HNReqMsg updateMsg) {
		final SteffiGraph graph = SteffiGraph.getInstance();
		if (updateMsg.getLocal2HNUpd() != null) {
			updateMsg.getLocal2HNUpd().forEachEntry(new TLongObjectProcedure<SteffiIndexedEdges>() {
	
				@Override
				public boolean execute(long cellId, SteffiIndexedEdges edgeMap) {
					Local2HopNeighbors.clearNeighbors(cellId);
					int remoteAddressIndex = graph.getMemberIndex(StorageTools.getCellAddress(cellId));
					Local2HopNeighborProcessor.addInvertedEdges(remoteAddressIndex, cellId, edgeMap);
					
					
					return true;
				}
			});
		}

		if (updateMsg.getRemovedEdges() != null) {
			System.out.println("Processing removal of indexes");
			
			for (SteffiEdge removedEdge : updateMsg.getRemovedEdges()) {
				SteffiIndexedEdges indexedEdges = Local2HopNeighbors.getNeighbors(removedEdge.getDestCellId());
				
				Collection<SteffiEdge> edges = indexedEdges.getAllEdges();
				
				if (edges.size() <= 2) {
					Local2HopNeighbors.removeNeighbors(removedEdge.getDestCellId());
					for (SteffiEdge edge : edges )
						((ExtSteffiEdge)edge).setNeighborFlag((byte) 0);
				} else {
					indexedEdges.remove(removedEdge);
				}
			}
			
		}
	}
	
}
