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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.imgraph.common.Configuration;
import com.imgraph.common.Configuration.Key;
import com.imgraph.networking.messages.AddressVertexRepMsg;
import com.imgraph.networking.messages.AddressVertexReqMsg;
import com.imgraph.networking.messages.ClusterAddressesRep;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.MessageType;
import com.imgraph.networking.messages.SearchEndMsg;
import com.imgraph.networking.messages.SearchRepMsg;
import com.imgraph.networking.messages.SearchReqMsg;


/**
 * @author Aldemar Reynaga
 * Process running inside a traversal worker that coordinates the distributed traversal 
 */
public class TraversalManager {
	private int id;
	private TraversalState traversalState;
	private Map<String, String> clusterAddresses;
	private long startVertexId;
	private int hops;
	private MatchEvaluatorConf matchConf;
	private List<EdgeTraversalConf> traversalConfs;
	private UUID searchId;
	private boolean runningTraversal;
	private Object lock;
	private boolean criticalError;
	private boolean stopWhenFound;
	private boolean logStatistics;
	private int managerIndex;
	private ZMQ.Context context;
	private AtomicInteger searchRepCounter;
	
	private String clientIp;
	
	private Map<String, SearchClientThread> clientThreads;
	
	public TraversalManager(ZMQ.Context context, int id) {
		this.id = id;
		this.logStatistics = Configuration.getProperty(Key.LOG_STATISTICS).equals("true");
		this.context = context;
	}
	
	public void init(UUID searchId, long startVertexId, int hops, MatchEvaluatorConf matchConf,
			List<EdgeTraversalConf> traversalConfs, String clientIp, int managerIndex) throws Exception {
		
		if (logStatistics)
			this.searchRepCounter = new AtomicInteger(0);
		this.managerIndex = managerIndex;
		this.startVertexId = startVertexId;
		this.hops = hops;
		this.matchConf = matchConf;
		this.traversalConfs = traversalConfs;
		this.traversalState =  new TraversalState();
		this.searchId = searchId;
		this.runningTraversal = false;
		this.lock = new String("xyz");
		this.clientIp = clientIp;
		this.criticalError = false;
		
		if (clusterAddresses == null) {
			loadAddressesIps();
			initClientThreads();
		}
		
		
		stopWhenFound = matchConf.getEvaluation().equals(Evaluation.INCLUDE_AND_STOP);
		
	}
	
	private SearchReqMsg prepareReqMessage(UUID searchMsgId, List<ReducedVertexPath> vertexPaths) {
		SearchReqMsg searchReqMsg = new SearchReqMsg();
		
		searchReqMsg.setMatchConf(matchConf);
		searchReqMsg.setMaxHops(hops);
		searchReqMsg.setSearchId(searchId);
		searchReqMsg.setTraversalConfs(traversalConfs);
		searchReqMsg.setSearchMsgId(searchMsgId);
		//searchReqMsg.setLastExplored(new ArrayList<VertexIdDepth>());
		searchReqMsg.setVertexPaths(vertexPaths);
		searchReqMsg.setManagerIndex(managerIndex);
		
		
		return searchReqMsg;
	}
	
		
	private void initClientThreads() {
		this.clientThreads = new ConcurrentHashMap<String, SearchClientThread>();
		SearchClientThread clientThread = null;
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			clientThread = new SearchClientThread(context, entry.getValue(), entry.getKey(), "tm_" + 
					entry.getKey() + "_" + this.id);
			clientThreads.put(entry.getKey(), clientThread);
			new Thread(clientThread).start();
		}
	}
	
	private void sendSearchReqMessage(UUID searchMsgId, String address, List<ReducedVertexPath> vertexPaths) throws Exception {
		SearchClientThread clientThread = clientThreads.get(address);
		clientThread.sendMessage(prepareReqMessage(searchMsgId, vertexPaths));
	}
	
	
	
	public void closeClientThreads() {
		if (clientThreads != null)
			for (SearchClientThread ct : clientThreads.values())
				ct.stop();
	}
	
	public void sendEndSearchMsgs(UUID searchId) throws Exception {
		SearchEndMsg searchEndMsg = null;
		for (SearchClientThread ct : clientThreads.values()) {
			searchEndMsg = new SearchEndMsg();
			searchEndMsg.setSearchId(searchId);
			
			ct.sendMessage(searchEndMsg);
		}
	}
	
	
	
	
	private String getStartAddress() throws Exception {
		String address = null;

		Socket requester = context.socket(ZMQ.REQ);
		
		try {
			requester.setIdentity(UUID.randomUUID().toString().getBytes());
			requester.connect("tcp://" + clientIp + ":" + Configuration.getProperty(Configuration.Key.NODE_PORT));
			
			AddressVertexReqMsg message = new AddressVertexReqMsg();
			
			message.setCellIds(Arrays.asList(startVertexId));
			
			requester.send(Message.convertMessageToBytes(message), 0);
			
			byte[] reply = requester.recv(0);
			
			AddressVertexRepMsg response = (AddressVertexRepMsg) Message.readFromBytes(reply);
			address = response.getCellAddresses().get(startVertexId);
			
		} finally {
			requester.close();

		}
		return address;
	}
	
	
	private void loadAddressesIps() throws Exception {

		Socket requester = context.socket(ZMQ.REQ);
		
		try {
			requester.setIdentity(UUID.randomUUID().toString().getBytes());
			requester.connect("tcp://" + clientIp + ":" + Configuration.getProperty(Configuration.Key.NODE_PORT));
			
			
			Message message = new Message(MessageType.CLUSTER_ADDRESSES_REQ);
			
			requester.send(Message.convertMessageToBytes(message), 0);
			
			
			byte[] reply = requester.recv(0);
			
			ClusterAddressesRep adRep = (ClusterAddressesRep) Message.readFromBytes(reply);
			clusterAddresses = adRep.getAddressesIp();
			
		} finally {
			requester.close();

		}
		
	}
	
	
	public TraversalResults traverse() throws Exception {
		
		
		
		String startAddress = getStartAddress();
		List<ReducedVertexPath> vertexPaths = new ArrayList<ReducedVertexPath>();
		
		ReducedVertexPath vertexPath = new ReducedVertexPath(startVertexId);
		vertexPaths.add(vertexPath);
		
		UUID searchMsgId = UUID.randomUUID();
		
		traversalState.addPendingMessage(searchMsgId);
		sendSearchReqMessage(searchMsgId, startAddress,  vertexPaths); //Initial traversal task
		runningTraversal = true;
		
		synchronized (lock) {
			while(runningTraversal) {
				
				try {
					lock.wait();
					
					
				} catch (InterruptedException ie){
					break;
				}
				
			}
		}
		

		
		if (criticalError)
			throw new Exception("Critical error on the traversal");
		
		sendEndSearchMsgs(searchId);
		
		return traversalState.getResults();
	}
	
	
	public boolean isTraversalRunning() {
		return runningTraversal;
	}
	
	private void checkTraversalEnd() {
		
		
		if (stopWhenFound && 
				!traversalState.results.getVertexPaths().isEmpty() ) {
			synchronized (lock) {
				runningTraversal = false;
				if (logStatistics)
					System.out.println("Search responses processed: " + searchRepCounter.get());
				lock.notifyAll();
			}
		
			
			
		} else {
		
			if (traversalState.isCompleted()) {
				
				
				synchronized (lock) {
					runningTraversal = false;
					if (logStatistics)
						System.out.println("Search responses processed: " + searchRepCounter.get());
					
					lock.notifyAll();
				}
				
			}
		}
		
	}
	
	public void registerSearchRepMsg(SearchRepMsg message) {

		
		if (logStatistics)
			searchRepCounter.incrementAndGet();
		synchronized (traversalState) {
			if (runningTraversal) {
				
				if (!message.isOk()) {
					
					criticalError = true;
					System.out.println("Critical error on traversal!");
					synchronized (lock) {
						runningTraversal = false;
		
						lock.notifyAll();
					}
					
				} else {
					traversalState.addMessageResponse(message.getSearchMsgId());
					traversalState.addPendingMessages(message.getSentSearchMsgIds());
					traversalState.getResults().addManyVertexPaths(message.getTraversalResults().getVertexPaths());
					checkTraversalEnd();
				}
				
				
			}
		}
		
		
		
	}

	
	private class TraversalState {
		private TraversalResults results;
		
		private Set<UUID> pendingMessages;
		private Set<UUID> messageResponses;
		
		
		public TraversalState() {
			this.results = new TraversalResultsImpl();
			pendingMessages = new HashSet<UUID>();
			messageResponses = new HashSet<UUID>();
		}
		
		
		
		public void addPendingMessages(List<UUID> uuids) {
			for (UUID uuid : uuids)
				addPendingMessage(uuid);
		}
		
		
		
		public void addPendingMessage(UUID uuid) {
			if (!messageResponses.remove(uuid)) {
				pendingMessages.add(uuid);
			}
		}
		
		public void addMessageResponse(UUID uuid) {
			if (!pendingMessages.remove(uuid)) {
				messageResponses.add(uuid);
			}
		}
		
		public boolean isCompleted() {
			return (messageResponses.isEmpty() && pendingMessages.isEmpty());
		}

		public TraversalResults getResults() {
			return results;
		}

				
		@Override
		public String toString() {
			return "PEND: " + pendingMessages.size() + " RESP: " + messageResponses.size();
		}
		
	}
		
}
