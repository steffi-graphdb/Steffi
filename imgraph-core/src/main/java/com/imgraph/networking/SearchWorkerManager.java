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
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import akka.pattern.Patterns;

import com.imgraph.common.CommonTools;
import com.imgraph.common.Configuration;
import com.imgraph.model.ImgGraph;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.SearchRepMsg;
import com.imgraph.networking.messages.SearchReqMsg;
import com.imgraph.storage.CacheContainer;
import com.imgraph.storage.StorageTools;
import com.imgraph.traversal.SearchClientThread;
import com.imgraph.traversal.actors.SearchActor;

/**
 * @author Aldemar Reynaga
 * Manages a pool of search workers used for the distributed traversal
 */
public class SearchWorkerManager {
	private ZMQ.Socket backendSearcher;
	
	
	private Map<UUID, Boolean> completedSearches;
	
	private Map<UUID, Map<String, Long>> statistics;
	private boolean logStatistics;
	
	private SearchClientThread[] clientThreads;
	
	
	private Map<String, String> clusterAddresses;
	private ActorSystem actorSystem;
	private Map<UUID, ActorRef> searchActors;
	private String localAddress;
	
	private ZMQ.Socket[] managerClientSockets;
	private ZMQ.Context context;
	
	
	public void init(Context context) {
		this.context = context;
		completedSearches = new ConcurrentHashMap<UUID, Boolean>();
	
		backendSearcher = context.socket(ZMQ.DEALER);
		backendSearcher.bind("inproc://backend_searcher_v3");
		
		logStatistics = Configuration.getProperty(Configuration.Key.LOG_STATISTICS).equals("true");
		if (logStatistics)
			this.statistics = new HashMap<UUID, Map<String,Long>>();
		
		this.actorSystem = ActorSystem.create("main-System");
		
		this.searchActors = new HashMap<UUID, ActorRef>();
		this.localAddress = CacheContainer.getCellCache().
				getCacheManager().getAddress().toString();
		
		initManagerSockets();
	}
	
	private void initManagerSockets() {
		String[] traversalManagerIPs = ImgGraph.getInstance().getTraversalManagerIps();
		managerClientSockets =  new ZMQ.Socket[traversalManagerIPs.length];
		int i=0;
		for (String traversalManagerIP : traversalManagerIPs) {
			
			managerClientSockets[i] = context.socket(ZMQ.DEALER);
			
			managerClientSockets[i].setIdentity(("mngClient_" + UUID.randomUUID()).getBytes());
			managerClientSockets[i].connect("tcp://" + traversalManagerIP);
			i++;
		}
			
		
		
		
		
	}
	
	
	public synchronized void sendResponseToManager(SearchRepMsg searchRepMsg, int managerIndex) throws IOException {
		
		managerClientSockets[managerIndex].send(Message.convertMessageToBytes(searchRepMsg), 0);
	}
	
	
	private void initClientThreads() {
		ImgGraph graph = ImgGraph.getInstance();
		
		this.clientThreads = new SearchClientThread[graph.getNumberOfMembers()];
		
		SearchClientThread clientThread = null;
		String searcherId = "searcher_" + CommonTools.getLocalIP();
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			clientThread = new SearchClientThread(context, entry.getValue(), entry.getKey(), searcherId);
			clientThreads[graph.getMemberIndex(entry.getKey())] = clientThread;
		}
	}
	
	public boolean isSearchAlive(UUID searchId) {
		return !completedSearches.containsKey(searchId);
	}
	
	public void endSearch(UUID searchId) {
		completedSearches.put(searchId, true);
		ActorRef searchActor = searchActors.remove(searchId);
		String logString = "";
		if (searchActor != null) {
			
			if (logStatistics) {
				
				Future<Object> response = Patterns.ask(searchActor, "GET_STATISTICS", 2000);
				try {
					String stat [] = ((String) Await.result(response, Duration.create(2, "seconds"))).split(":");
					logString += ("Vertices: " + stat[0] + "-Edges: " + stat[1]);
					
				} catch (Exception x) {
					System.out.println("Error getting statistics... ");
					x.printStackTrace();
				}
			}
			
			actorSystem.stop(searchActor);
			
		}
		
		if (logStatistics)
		{ 
			Map<String, Long> searchStatistics =  statistics.get(searchId);
			if (searchStatistics != null) {
				logString += ("Search messages: " + searchStatistics.get("SEARCH_MSG") + 
						"-Requested searches: " + searchStatistics.get("SEARCH_REQ") +
						"-Executed searches: " + searchStatistics.get("SEARCHES"));
			}
			System.out.println("\n##" + logString);
		}
		
	}
	
	public void initializeClientThreads() {
		if (clusterAddresses == null) {
			clusterAddresses = StorageTools.getAddressesIps();
			initClientThreads();
		}
	}
	
	
	private void updateStatistics(SearchReqMsg searchReqMsg) {
		if (logStatistics) {
			Map<String, Long> searchStatistics =  statistics.get(searchReqMsg.getSearchId());
			if (searchStatistics == null) {
				searchStatistics = new HashMap<String, Long>();
				searchStatistics.put("SEARCH_MSG", 0L);
				searchStatistics.put("SEARCHES", 0L);
				searchStatistics.put("SEARCH_REQ", 0L);
				statistics.put(searchReqMsg.getSearchId(), searchStatistics);
			}
			searchStatistics.put("SEARCH_MSG", searchStatistics.get("SEARCH_MSG") + 1);
			searchStatistics.put("SEARCH_REQ", searchStatistics.get("SEARCH_REQ") + 
					searchReqMsg.getVertexPaths().size());
		}
	}
	
	
	
	
	public void sendToSearchWorker(final SearchReqMsg searchReqMsg) throws Exception {
		
		
		updateStatistics(searchReqMsg);
		
	
		if (isSearchAlive(searchReqMsg.getSearchId())) {
		
			ActorRef searchActor = searchActors.get(searchReqMsg.getSearchId());
					
			if (searchActor == null) {
				searchActor = actorSystem.actorOf(new Props(new UntypedActorFactory() {
					
					@Override
					public Actor create() throws Exception {
						return new SearchActor(SearchWorkerManager.this, searchReqMsg, localAddress);
					}
				}), "search_" + searchReqMsg.getSearchId().toString());
				searchActors.put(searchReqMsg.getSearchId(), searchActor);
			}
			
			searchActor.tell(searchReqMsg, actorSystem.lookupRoot());
		}
	}
	
	
	
	
	public void logSearch(UUID searchId) {
		if (logStatistics) {
			synchronized (this) {
				Map<String, Long> searchStatistics =  statistics.get(searchId);
				
				Long current = searchStatistics.get("SEARCHES");
				
				searchStatistics.put("SEARCHES", current+1);
			}
		}
		
	}
	
	public void informSearcher(Message[] messages) throws Exception {
		for (int i=0; i<messages.length; i++) {
			if (messages[i] != null) {
				clientThreads[i].sendMessage(messages[i]);
			}
		}
	}
	
	
	
	
	
	public void stop() {
		backendSearcher.close();
		if (clientThreads != null) {
			for (SearchClientThread clientThread : clientThreads)
				clientThread.close();
		}
		for (ZMQ.Socket socket : managerClientSockets)
			socket.close();
		
		
		for (ActorRef actor : searchActors.values())
			actorSystem.stop(actor);
		
		actorSystem.shutdown();
	}
	
}
