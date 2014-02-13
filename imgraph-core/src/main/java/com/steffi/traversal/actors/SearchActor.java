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
package com.steffi.traversal.actors;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import akka.actor.UntypedActor;

import com.steffi.common.ImgLogger;
import com.steffi.model.EdgeType;
import com.steffi.model.ExtSteffiEdge;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiIndexedEdges;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.SearchWorkerManager;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.SearchRepMsg;
import com.steffi.networking.messages.SearchReqMsg;
import com.steffi.storage.Local2HopNeighbors;
import com.steffi.traversal.EdgeTraversalConf;
import com.steffi.traversal.Evaluation;
import com.steffi.traversal.Evaluator;
import com.steffi.traversal.MatchEvaluator;
import com.steffi.traversal.MatchEvaluatorConf;
import com.steffi.traversal.ReducedVertexPath;
import com.steffi.traversal.TraversalResults;
import com.steffi.traversal.TraversalResultsImpl;

/**
 * @author Aldemar Reynaga
 * Akka actor started when a search request message arrives to the data server. The class contains
 * the logic to execute the modified BFS algorithm 
 */
public class SearchActor extends UntypedActor{
	private TLongIntMap explored;
	private List<Evaluator> evaluators;
	private List<EdgeTraversalConf> edgeTraversalConfs;
	private int hops;
	private int localAddressIndex;
	private int numberOfMembers;
	private UUID searchId;
	private boolean stop;
	private MatchEvaluatorConf matchConf;
	private SearchWorkerManager searchWorkerManager;
	private int managerIndex;
	
	private Queue<ReducedVertexPath> frontier;
	private TLongIntMap frontierHash;
	private TraversalResults traversalResults;
	
	private long logVertices;
	private long logEdges;
	
	
	
	
	
	private List<ReducedVertexPath>[] addressVerticesInfo;
	
	private TLongSet explored2HNeighbors;
	private List<UUID> sentMsgIds;
	private TLongSet local2HNeighbors;
	
	
	
	public SearchActor(SearchWorkerManager searchWorkerManager, SearchReqMsg initialMsg,
			String localAddress) {
		this.searchWorkerManager = searchWorkerManager;
		this.hops = initialMsg.getMaxHops();
		this.matchConf = initialMsg.getMatchConf();
		Evaluator [] evaluators = {new MatchEvaluator(matchConf)};
		this.evaluators = Arrays.asList(evaluators);
		this.edgeTraversalConfs = initialMsg.getTraversalConfs();
		this.explored = new TLongIntHashMap();
		
		this.localAddressIndex = SteffiGraph.getInstance().getMemberIndex(localAddress);
		this.numberOfMembers = SteffiGraph.getInstance().getNumberOfMembers();
		this.sentMsgIds = new ArrayList<UUID>();
		this.traversalResults = new TraversalResultsImpl();
		this.searchId = initialMsg.getSearchId();
		this.logVertices = 0;
		this.logEdges = 0;
		this.managerIndex = initialMsg.getManagerIndex();
	}
	
	public long getLogEdges() {
		return logEdges;
	}
	
	public long getLogVertices() {
		return logVertices;
	}
	
	private boolean isNotExplored(long cellId, int depth) {

		int curDepth = explored.get(cellId);
		if (curDepth == 0)
			return true;
			
		return depth < curDepth;
	}

	private boolean isNotInFrontier(long cellId, int depth) {
		
		int curDepth = frontierHash.get(cellId);
		
		if (curDepth == 0)
			return true;

		if (depth < curDepth) {
			frontierHash.remove(cellId);
			
			Iterator<ReducedVertexPath> iterator = frontier.iterator();
			
			while (iterator.hasNext()) {
				ReducedVertexPath vertexPath = iterator.next();
				if (vertexPath.getVertexId() == cellId) {
					iterator.remove();
					break;
				}
			}
			
			
			return true;
		}
		
		return false;


	}


	private boolean isNotExploredEdge(SteffiEdge edge, int depth) {
		return (
				isNotInFrontier(edge.getDestCellId(), depth)
				&& 
				isNotExplored(edge.getDestCellId(), depth)
				);
	}
	
	private void evalVertex(ReducedVertexPath vertexPath) {
		stop = false;


		for (Evaluator evaluator : evaluators) {
			Evaluation eval = evaluator.evaluate(vertexPath); 

			switch (eval) {
			case EXCLUDE_AND_CONTINUE:
				break;
			case EXCLUDE_AND_STOP:
				stop = true;
				break;
			case INCLUDE_AND_CONTINUE:
				traversalResults.addVertexPath(vertexPath);
				break;
			case INCLUDE_AND_STOP:
				traversalResults.addVertexPath(vertexPath);
				stop = true;
				break;
			}
		}
		
	}
	
	private void process2HopEdge(long curEdge, ReducedVertexPath intVertexPath, SteffiEdge edge,
			int edgeDepth) {
		if (curEdge != edge.getDestCellId() 
				&& isNotExploredEdge(edge, edgeDepth)) {

			
			ReducedVertexPath localVertexPath = new ReducedVertexPath(intVertexPath, 
					EdgeType.invertType(edge.getEdgeType()), edge.getName(), edge.getSourceCellId(), edgeDepth);
			
			logVertices++;
			evalVertex(localVertexPath);
			if (stop) 
				return;

			
			frontier.add(localVertexPath);
			frontierHash.put(edge.getDestCellId(), edgeDepth);

		}
		local2HNeighbors.add(edge.getDestCellId());
	}


	private void processRemoteEdge(ExtSteffiEdge extEdge, ReducedVertexPath parentVertexPath, int edgeDepth, 
			List<ReducedVertexPath> vertexPaths) {

		if (isNotExploredEdge(extEdge, edgeDepth)) {
			logEdges++;
			
			if (extEdge.getNeighborFlag() == 1 && (parentVertexPath.getDepth() + 2) <= hops) {
				ReducedVertexPath intVertexPath = new ReducedVertexPath(parentVertexPath, 
						extEdge.getEdgeType(), extEdge.getName(), extEdge.getDestCellId(), edgeDepth);

				SteffiIndexedEdges edgeMap = Local2HopNeighbors.getNeighbors(extEdge.getDestCellId());

				int subEdgeDepth = intVertexPath.getDepth()+1;

				if (edgeTraversalConfs.isEmpty()) {
					for (SteffiEdge edge : edgeMap.getAllEdges()) {
						process2HopEdge(extEdge.getDestCellId(), intVertexPath, edge, subEdgeDepth);
						if (stop)
							return;
					}
				} else {
					for (EdgeTraversalConf traversalConf : edgeTraversalConfs) {
						Collection<SteffiEdge> edges = edgeMap.getEdgesByTypeName(EdgeType.invertType(traversalConf.getEdgeType()), 
								traversalConf.getName());
						if (edges != null) {
							for (SteffiEdge edge : edges) {
								//Body
								process2HopEdge(extEdge.getDestCellId(), intVertexPath, edge, subEdgeDepth);
								if (stop)
									return;
							}
						}

					}
				}
			}
			
	
			if (explored2HNeighbors == null || !explored2HNeighbors.contains(extEdge.getDestCellId())) {
				vertexPaths.add(new ReducedVertexPath(parentVertexPath, 
						extEdge.getEdgeType(), extEdge.getName(), extEdge.getDestCellId(), edgeDepth));
			}

			explored.put(extEdge.getDestCellId(), edgeDepth);
			
		}
	}

	

	
	private void addEdgesToVertexPaths(int addressIndex, ReducedVertexPath parentVertexPath, 
			SteffiIndexedEdges edgeMap ) {

		
		
		List<ReducedVertexPath> vertexPaths = new LinkedList<ReducedVertexPath>();
		
		int edgeDepth = parentVertexPath.getDepth()+1;

		if (edgeTraversalConfs.isEmpty()) {
			for (SteffiEdge edge : edgeMap.getAllEdges()) {
				//Body
				processRemoteEdge((ExtSteffiEdge) edge, parentVertexPath, edgeDepth, vertexPaths);
				if (stop)
					return;
			}
		} else {
			for (EdgeTraversalConf traversalConf : edgeTraversalConfs) {
				Collection<SteffiEdge> edges = edgeMap.getEdgesByTypeName(traversalConf.getEdgeType(), 
						traversalConf.getName());
				if (edges != null) {
					for (SteffiEdge edge : edges) {
						//Body
						processRemoteEdge((ExtSteffiEdge) edge, parentVertexPath, edgeDepth, vertexPaths);
						if (stop)
							return;
					}
				}

			}
		}

		if (!vertexPaths.isEmpty()) {
			
			List<ReducedVertexPath> curVertexPaths = addressVerticesInfo[addressIndex];
			
			if (curVertexPaths== null) {
				addressVerticesInfo[addressIndex] = vertexPaths;
			} else {
				curVertexPaths.addAll(vertexPaths);
			}
			
		}
	}
	
	
	private void prepareExternalVertexPaths(ReducedVertexPath parentVertexPath) {

		for (int i=0; i<numberOfMembers; i++) {
			if (i != localAddressIndex && parentVertexPath.getVertex().getEdgeAddresses()[i] != null) {
				addEdgesToVertexPaths(i, parentVertexPath, parentVertexPath.getVertex().getEdgeAddresses()[i]);
				if(stop)
					return;
			}
		}
	}
	private void informSearchManagers() throws Exception {

		
		Message[] messages = new Message[numberOfMembers];
		SearchReqMsg searchReqMsg = null;
		boolean existMessages = false;
		
		for (int i=0; i<numberOfMembers; i++) {
			List<ReducedVertexPath> vertexPaths =  addressVerticesInfo[i];
			if (vertexPaths != null && !vertexPaths.isEmpty()) {
				existMessages = true;
				searchReqMsg = new SearchReqMsg();
				searchReqMsg.setMatchConf(matchConf);
				searchReqMsg.setMaxHops(hops);
				searchReqMsg.setVertexPaths(vertexPaths);
				searchReqMsg.setExplored2HNeighbors(local2HNeighbors);

				searchReqMsg.setSearchId(searchId);
				searchReqMsg.setSearchMsgId(UUID.randomUUID());
				searchReqMsg.setTraversalConfs(edgeTraversalConfs);
				searchReqMsg.setManagerIndex(managerIndex);
				
				sentMsgIds.add(searchReqMsg.getSearchMsgId());
	
				messages[i] = searchReqMsg;
			}

		}

		if (existMessages)
			searchWorkerManager.informSearcher(messages);
	}


	private void processEdge(SteffiEdge edge, ReducedVertexPath vertexPath, int depth) {
		ReducedVertexPath childVertexPath = null;

		if (isNotExploredEdge(edge, depth)) {
			
			logEdges++;

			SteffiVertex childVertex = (SteffiVertex) SteffiGraph.getInstance().retrieveRawCell(edge.getDestCellId());

			childVertexPath = new ReducedVertexPath(vertexPath, edge.getEdgeType(), 
					edge.getName(), edge.getDestCellId(), depth);

			childVertexPath.setVertex(childVertex);

			logVertices++;
			evalVertex(childVertexPath);
			if (stop)
				return;
			

			frontier.add(childVertexPath);
			frontierHash.put(childVertexPath.getVertexId(), depth);


		}
		
	}
	
	

	@SuppressWarnings("unchecked")
	private void search(List<ReducedVertexPath> vertexPaths) throws Exception {


		 
		ReducedVertexPath vertexPath = null;

		frontier = new LinkedList<ReducedVertexPath>();
		frontierHash = new TLongIntHashMap();
		
		addressVerticesInfo = (List<ReducedVertexPath>[]) new List[numberOfMembers]; 

		local2HNeighbors = new TLongHashSet();

		
		int currentDepth = vertexPaths.get(0).getDepth();

		for (ReducedVertexPath startVertexPath : vertexPaths) {
			
			if (isNotExplored(startVertexPath.getVertexId(), startVertexPath.getDepth())) {
			
				logVertices++;
				evalVertex(startVertexPath);
				if (stop)
					return;
				
				frontier.add(startVertexPath);
				frontierHash.put(startVertexPath.getVertexId(), startVertexPath.getDepth());
			}
		}
		
		while (true) {
			
			if (frontier.isEmpty()) {
				informSearchManagers();
				return;
			}

			vertexPath = frontier.poll();
			frontierHash.remove(vertexPath.getVertexId());

			if (vertexPath.getDepth() > currentDepth) {
				informSearchManagers ();
				
				currentDepth = vertexPath.getDepth();
				
				for (int i=0; i<addressVerticesInfo.length; i++)
					addressVerticesInfo[i] = new ArrayList<ReducedVertexPath>();
				

				local2HNeighbors = new TLongHashSet();
				
			}


			explored.put(vertexPath.getVertexId(), vertexPath.getDepth());


			int edgeDepth = vertexPath.getDepth()+1;

			if (edgeDepth <= hops) {

				SteffiIndexedEdges edgeMap = vertexPath.getVertex().getEdgeAddresses()[localAddressIndex];

				if (edgeMap != null) {
					if (edgeTraversalConfs.isEmpty()) {
						for (SteffiEdge edge : edgeMap.getAllEdges()) {
							processEdge(edge, vertexPath, edgeDepth);
							if(stop)
								return;
						}
					} else {
						for (EdgeTraversalConf traversalConf : edgeTraversalConfs) {
							Collection<SteffiEdge> edges = edgeMap.getEdgesByTypeName(traversalConf.getEdgeType(), 
									traversalConf.getName());
							if (edges != null) {
								for (SteffiEdge edge : edges) {
									//Body
									processEdge(edge, vertexPath, edgeDepth);
									if(stop)
										return;
								}
							}

						}
					}
				}


				prepareExternalVertexPaths(vertexPath);
				if(stop)
					return;
			}


		}
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof SearchReqMsg) {
			SearchReqMsg searchReqMsg = (SearchReqMsg) message;
			boolean ok = true;
			sentMsgIds.clear();
			traversalResults.clear();
			explored2HNeighbors = searchReqMsg.getExplored2HNeighbors();
			
			try {
				searchWorkerManager.logSearch(searchId);
				search(searchReqMsg.getVertexPaths());
			} catch (Exception se) {
				ImgLogger.logError(se, "Error processing search for " + searchReqMsg.getVertexPaths()) ;
				ok = false;
			}
			
			final TraversalResults finalResults = new TraversalResultsImpl();
			finalResults.addManyVertexPaths(traversalResults.getVertexPaths());
			final List<UUID> finalSentMsgIds = new ArrayList<UUID>();
			finalSentMsgIds.addAll(sentMsgIds);
			
			final SearchRepMsg searchRepMsg = new SearchRepMsg();
			searchRepMsg.setOk(ok);
			searchRepMsg.setSearchId(searchId);
			searchRepMsg.setSearchMsgId(searchReqMsg.getSearchMsgId());
			searchRepMsg.setSentSearchMsgIds(finalSentMsgIds);
			searchRepMsg.setTraversalResults(finalResults);
			
			searchWorkerManager.sendResponseToManager(searchRepMsg, managerIndex);
			//getSender().tell(searchRepMsg, getSelf());
		} else if (message instanceof String) {
			if (((String)message).equals("GET_STATISTICS")) {
				getSender().tell(logVertices + ":" + logEdges, getSelf());
			}
		}




	}

	@Override
	public void postStop() {
		super.postStop();
		
	}




}
