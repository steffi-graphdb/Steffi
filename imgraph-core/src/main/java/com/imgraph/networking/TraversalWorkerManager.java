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
package com.imgraph.networking;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.imgraph.networking.messages.InitTraversalMsg;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.SearchRepMsg;
import com.imgraph.networking.messages.TraversalReqMsg;

/**
 * @author Aldemar Reynaga
 * Manages the pool of traversal workers of the traversal manager server
 */
public class TraversalWorkerManager {
	private Map<UUID, Integer> workerAllocations;
	private Queue<PendingTraversal> pendingTraversals;
	private Queue<Integer> freeWorkers;
	private ZMQ.Socket backendTraversal[];
	private TraversalWorker traversalWorkers[];
	

	private class PendingTraversal{
		private byte[] idMessage;
		private TraversalReqMsg message;

		public PendingTraversal(byte[] idMessage, TraversalReqMsg message) {
			this.idMessage = idMessage;
			this.message = message;
		}

		public byte[] getIdMessage() {
			return idMessage;
		}

		public TraversalReqMsg getMessage() {
			return message;
		}







	}


	public TraversalWorkerManager(){
		pendingTraversals = new LinkedList<PendingTraversal>();
		freeWorkers = new LinkedList<Integer>();
		workerAllocations = new HashMap<UUID, Integer>();

	}

	public void init(Context context, Poller poller, int numWorkers) {
		backendTraversal = new ZMQ.Socket[numWorkers];
		traversalWorkers = new TraversalWorker[numWorkers];

		for (int j=0; j<numWorkers; j++) {
			backendTraversal[j] = context.socket(ZMQ.DEALER);
			backendTraversal[j].bind("inproc://backend_traversal_" + j);
			traversalWorkers[j] = new TraversalWorker(context, j);
			(new Thread(traversalWorkers[j])).start();
			poller.register(backendTraversal[j], ZMQ.Poller.POLLIN);
			freeWorkers.add(j);
		}
	}

	public void stop() {
		for (int k=0; k<traversalWorkers.length; k++) {
			traversalWorkers[k].stop();
			backendTraversal[k].close();
		}
	}

	
	public void notifyTraversalWorker(SearchRepMsg searchRepMsg){
		
		Integer worker = workerAllocations.get(searchRepMsg.getSearchId());
		
		if (worker != null) {
			traversalWorkers[worker].processRepMessage(searchRepMsg);
		} 
		
	}
	
	

	private void sendTraversalMsg(int worker, byte[] idMessage, TraversalReqMsg message) throws IOException {
		InitTraversalMsg initTraversalMsg = new InitTraversalMsg();
		initTraversalMsg.setSearchId(UUID.randomUUID());
		initTraversalMsg.setTraversalReqMsg(message);

		workerAllocations.put(initTraversalMsg.getSearchId(), worker);

		backendTraversal[worker].send(idMessage, ZMQ.SNDMORE);
		backendTraversal[worker].send(new byte[0], ZMQ.SNDMORE);
		backendTraversal[worker].send(Message.convertMessageToBytes(initTraversalMsg), 0); 
	}

	public void initTraversal(byte[] idMessage, TraversalReqMsg msg) throws IOException {
		Integer worker = getWorker(idMessage, msg);

		if (worker != null) {
			sendTraversalMsg(worker, idMessage, msg);
		}

	}

	
	

	public void freeTraversalWorker(int freeId) throws IOException {

		UUID searchId = null;

		for (Entry<UUID, Integer> entry : workerAllocations.entrySet()) {
			if (entry.getValue() == freeId) {
				searchId = entry.getKey();
				break;
			}
		}

		this.freeWorkers.add(workerAllocations.get(searchId));
		this.workerAllocations.remove(searchId);

		if (!pendingTraversals.isEmpty()) {
			int freeWorker = freeWorkers.poll();	
			PendingTraversal pendingTraversal = pendingTraversals.poll();
			sendTraversalMsg(freeWorker, pendingTraversal.getIdMessage(), 
					pendingTraversal.getMessage());


		} 
	}

	public void sendToFrontend(int index, Socket frontend) {
		byte [] message;
		boolean more;

		while (true) {
			// receive message
			message = backendTraversal[index].recv(0);
			more = backendTraversal[index].hasReceiveMore();

			frontend.send(message, more ? ZMQ.SNDMORE : 0);
			if(!more){
				break;
			}
		}
		
		
		
	}


	private Integer getWorker(byte[] idMessage, TraversalReqMsg msg) {
		Integer valRet = null;
		if (freeWorkers.isEmpty()) {
			PendingTraversal pendingTraversal = new PendingTraversal(idMessage, msg);
			pendingTraversals.add(pendingTraversal);
		} else {
			valRet = freeWorkers.poll();
		}
		return valRet;
	}



}
