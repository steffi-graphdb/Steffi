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
package com.steffi.networking;

import gnu.trove.procedure.TLongProcedure;

import java.io.IOException;

import org.zeromq.ZMQ.Socket;

import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiIndexedEdges;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.messages.AddressVertexRepMsg;
import com.steffi.networking.messages.AddressVertexReqMsg;
import com.steffi.networking.messages.ClusterAddressesRep;
import com.steffi.networking.messages.IdentifiableMessage;
import com.steffi.networking.messages.LocalNeighborsRepMsg;
import com.steffi.networking.messages.LocalNeighborsReqMsg;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.networking.messages.Update2HNReqMsg;
import com.steffi.networking.messages.WriteFileReqMsg;
import com.steffi.storage.CacheContainer;
import com.steffi.storage.SteffipFileTools;
import com.steffi.storage.Local2HopNeighborProcessor;
import com.steffi.storage.Local2HopNeighborUpdater;
import com.steffi.storage.StorageTools;

/**
 * @author Aldemar Reynaga
 * Contains the functions called on the arrival of command request messages
 */
public abstract class CommandProcessor {
	
	
	
	public static void processLocal2HRequest(Socket socket, final LocalNeighborsReqMsg reqMsg) throws IOException {
		final LocalNeighborsRepMsg repMsg = new LocalNeighborsRepMsg();
		
		reqMsg.getVertexIds().forEach(new TLongProcedure() {
			
			@Override
			public boolean execute(long vertexId) {
				SteffiGraph graph = SteffiGraph.getInstance();  
				SteffiIndexedEdges edgeMap = ((SteffiVertex)graph.retrieveRawCell(vertexId)).getEdgeMapByAddress(reqMsg.getLocalAddress());
				
				if (edgeMap != null && edgeMap.hasMoreThanOneEdge())
					repMsg.getVertexEdgeMap().put(vertexId, edgeMap);
				return true;
			}
		});
		
		socket.send(Message.convertMessageToBytes(repMsg), 0);
	}
	
	
	
	
	public static void processWriteFileRequest(Socket socket, WriteFileReqMsg reqMsg) throws IOException {
		Message writeResponse = SteffipFileTools.processWriteRequest(reqMsg);
		socket.send(Message.convertMessageToBytes(writeResponse), 0);
	}
	
	public static void processLocal2HopRequest(Socket socket) throws IOException {
		Local2HopNeighborProcessor local2HopProc = new Local2HopNeighborProcessor();
		
		int updResponse = local2HopProc.updateLocal2HopNeighbors();
		Message upd1HNResponse = new Message(MessageType.UPD_2HOP_NEIGHBORS_REP);
		upd1HNResponse.setBody((updResponse==1)?"OK":"ERROR");
		
		socket.send(Message.convertMessageToBytes(upd1HNResponse), 0);
		
	}

	
	public static void processClusterAddressRequest(Socket socket) throws IOException {
		ClusterAddressesRep addressRep = new ClusterAddressesRep();
		addressRep.setAddressesIp(StorageTools.getAddressesIps());
		socket.send(Message.convertMessageToBytes(addressRep), 0);
	}
	
	public static void processAddressVertexRequest(Socket socket, AddressVertexReqMsg reqMsg) throws IOException {
		AddressVertexRepMsg response = new AddressVertexRepMsg();
		
		for (Long cellId : reqMsg.getCellIds())
			response.getCellAddresses().put(cellId, StorageTools.getCellAddress(cellId));
		
		socket.send(Message.convertMessageToBytes(response), 0);
	}
	
	public static void processCellNumberRequest(Socket socket) throws IOException {
		Message response = new Message(MessageType.NUMBER_OF_CELLS_REP);
		response.setBody(String.valueOf(CacheContainer.getCellCache().getAdvancedCache().getDataContainer().size()));
		socket.send(Message.convertMessageToBytes(response), 0);
	}
	
	public static void processUpdate2HNRequest(Socket socket, Update2HNReqMsg update2HNReqMsg) throws IOException {
		Local2HopNeighborUpdater.processUpdateRequest(update2HNReqMsg);
		IdentifiableMessage response = new IdentifiableMessage(MessageType.UPD_2HN_TRANSACTION_REP);
		response.setBody("OK");
		response.setId(update2HNReqMsg.getId());
		socket.send(Message.convertMessageToBytes(response), 0);
	}
	
}
