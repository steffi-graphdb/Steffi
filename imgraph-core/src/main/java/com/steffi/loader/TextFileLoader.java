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
package com.steffi.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.UUID;

import org.zeromq.ZMQ;

import com.steffi.common.BigTextFile;
import com.steffi.common.Configuration;
import com.steffi.common.ImgLogger;
import com.steffi.common.Configuration.Key;
import com.steffi.common.ImgLogger.LogLevel;
import com.steffi.model.EdgeType;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.ClientThread;
import com.steffi.networking.messages.AddressVertexRepMsg;
import com.steffi.networking.messages.AddressVertexReqMsg;
import com.steffi.networking.messages.LoadMessage;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.networking.messages.LoadMessage.LoadFileType;
import com.steffi.storage.StorageTools;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.steffi.SteffiGraphDBGraph;

/**
 * @author Aldemar Reynaga
 * Batch loader for text files using the edge list format or the adjacent list format  
 */
public class TextFileLoader implements ResponseProcessor {

	private int pendingLoadBlocks;
	
	private boolean fileReadCompleted;
	private boolean loadingInProcess;
	private Object lock;
	private Map<String, ClientThread> clientThreads;
	private Map<String, List<LoadVertexInfo>[]> addressVerticesInfo;
	private List<Long> vertexIds;
	private ZMQ.Context context;
	private ZMQ.Socket socketToMember;
	private boolean update2HNInProcess;
	private int pending2HNRequests;
	private int vertexCounter;
	private int numberOfLoaders;
	private boolean loaderIsMember;

	private int processVerticesCounter;

	
	
	public TextFileLoader() {
		
		initClientThreads(StorageTools.getAddressesIps());
		lock = new String("TEXT_LOADER");
		numberOfLoaders = Integer.parseInt(Configuration.getProperty(Key.NUM_LOADERS));
		loaderIsMember = true;
	}
	
	public TextFileLoader(Map<String, String> clusterAddresses, String clientIp) {
		initClientThreads(clusterAddresses);
		context = ZMQ.context(1);
		socketToMember = context.socket(ZMQ.REQ);
		socketToMember.connect("tcp://" + clientIp + ":" + Configuration.getProperty(Configuration.Key.NODE_PORT));
		
		lock = new String("TEXT_LOADER");
		numberOfLoaders = Integer.parseInt(Configuration.getProperty(Key.NUM_LOADERS));
		loaderIsMember = false;
	}

	
	private void initClientThreads(Map<String, String> clusterAddresses) {
		
		this.clientThreads = new HashMap<String, ClientThread>();
		ClientThread clientThread = null;
		String loaderId = UUID.randomUUID().toString();
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			clientThread = new ClientThread(entry.getValue(), entry.getValue(), "loader_" + loaderId,
					this);
			clientThreads.put(entry.getKey(), clientThread);
			new Thread(clientThread).start();
		}
	}
	
	
	private Map<Long, String> getVertexAddresses() throws IOException {
		
		AddressVertexReqMsg message = new AddressVertexReqMsg();
			
		message.setCellIds(vertexIds);
			
		socketToMember.send(Message.convertMessageToBytes(message), 0);
			
		AddressVertexRepMsg response = (AddressVertexRepMsg) Message.readFromBytes(socketToMember.recv(0));
		
		return response.getCellAddresses();
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void classifyVerticesInfo(Collection<LoadVertexInfo> verticesInfo) throws IOException {
		
		int loaderIndex;
		Map<Long, String> vertexAddresses = null;
		
		if (!loaderIsMember) 
			vertexAddresses = getVertexAddresses();
		
		for (List<LoadVertexInfo>[] vertexList : addressVerticesInfo.values()) {
			for (int i=0; i<vertexList.length; i++)
				vertexList[i] = null;
		}
			
		
		for (LoadVertexInfo vertexInfo : verticesInfo) {
			String address;
			if (loaderIsMember)
				address = StorageTools.getCellAddress(vertexInfo.getVertexId());
			else
				address = vertexAddresses.get(vertexInfo.getVertexId());
			
			List<LoadVertexInfo>[] vertexList = addressVerticesInfo.get(address);
			
			if (vertexList == null) {
				vertexList = (List<LoadVertexInfo>[]) new List[numberOfLoaders];
				addressVerticesInfo.put(address, vertexList);
			}
			loaderIndex = (int) (vertexInfo.getVertexId() % numberOfLoaders);
			if (vertexList[loaderIndex] == null)
				vertexList[loaderIndex] = new ArrayList<LoadVertexInfo>();
			vertexList[loaderIndex].add(vertexInfo);
		}
		
	}
	
	private void sendVerticesInfo(Map<Long, LoadVertexInfo> verticesInfo, LoadFileType loadFileType) throws IOException {

		classifyVerticesInfo(verticesInfo.values());
		
		for (Entry<String, List<LoadVertexInfo>[]> entry : addressVerticesInfo.entrySet()) {
			
			for (int i=0; i<entry.getValue().length; i++) {
				List<LoadVertexInfo> subList = entry.getValue()[i];
				if (subList != null) {
					LoadMessage loadMessage = new LoadMessage();
					loadMessage.setVerticesInfo(subList);
					loadMessage.setLoadFileType(loadFileType);
					loadMessage.setLoaderIndex(i);
					
					clientThreads.get(entry.getKey()).addMsgToQueue(loadMessage);
					
					updateBlockCounter(1);
				}
					
			}
			
		}
	}
	
	private synchronized void updateBlockCounter(int addition) {
		pendingLoadBlocks += addition;
	}

	private synchronized void updateVertexCounter(int addition) {
		vertexCounter += addition;
	}
	
	private void verifyCompleteLoading() {
		if (fileReadCompleted && pendingLoadBlocks == 0) {
			synchronized (lock) {
				loadingInProcess = false;
				lock.notifyAll();
			}
		}
	}

	private void processOkResponse() {
		updateBlockCounter(-1);
		
		if (fileReadCompleted) {
			if (pendingLoadBlocks % 1000 == 0) {
				System.out.println("Pending load blocks: " + pendingLoadBlocks + "...");
				System.out.flush();
			}
		}
		verifyCompleteLoading();
	}

	
	@Override
	public void processResponse(Message message) {
		
		if (message.getType().equals(MessageType.LOAD_REP)) {
			String [] response = message.getBody().split("::");
			if (response[0].equals("OK")) { 
				processOkResponse();
				updateVertexCounter(Integer.parseInt(response[1]));
			} else { 
				throw new RuntimeException("Error processing load block: " + message.getBody());
			}
		} else if (message.getType().equals(MessageType.UPD_2HOP_NEIGHBORS_REP)) {
			if (message.getBody().equals("OK"))
				registerUpd2HNResponse();
			else
				throw new RuntimeException("Error processing an update request for local 2-hop neighbors");
		} 
		
		
	}

	
	private LoadVertexInfo getVertexInfo(Map<Long, LoadVertexInfo> verticesInfo, long vertexId) {
		LoadVertexInfo vertexInfo = verticesInfo.get(vertexId);
		if (vertexInfo == null) {
			vertexInfo = new LoadVertexInfo(vertexId);
			verticesInfo.put(vertexId, vertexInfo);
			if (!loaderIsMember)
				vertexIds.add(vertexId);
		}
		return vertexInfo;
	}

	private void addEdgeToVertexInfo(Map<Long, LoadVertexInfo> verticesInfo, 
			long sourceId, long destId, boolean isDirected) {
		
		LoadVertexInfo vertexInfo = null;
		
		vertexInfo = getVertexInfo(verticesInfo, sourceId);
		if (isDirected)
			vertexInfo.addOutEdge(destId);
		else
			vertexInfo.addUndirectedEdge(destId);
		
		
		vertexInfo = getVertexInfo(verticesInfo, destId);
		if (isDirected)
			vertexInfo.addInEdge(sourceId);
		else
			vertexInfo.addUndirectedEdge(sourceId);
		
		
	}
	
	private synchronized void registerUpd2HNResponse() {
		pending2HNRequests--;
		System.out.println("Pending 2-hop neighbors requests: " + pending2HNRequests);
		if (pending2HNRequests == 0) {
			synchronized (lock) {
				update2HNInProcess = false;
				lock.notifyAll();
			}
		}
	}
	
	private void updateLocal2HopNeighbors() throws Exception {
		Message message = new Message(MessageType.UPD_2HOP_NEIGHBORS_REQ);
		
		update2HNInProcess = true;
		for (ClientThread ct : clientThreads.values()) 
			ct.addMsgToQueue(message);
		
		pending2HNRequests = clientThreads.size();
		
		lock = new String("UPDATE_2HN");
		
		synchronized (lock) {
			while (update2HNInProcess) {
				lock.wait();
			}
		}
		
	}
	
	private void processVerticesInfo(Map<Long, LoadVertexInfo> verticesInfo, LoadFileType loadFileType) throws Exception {
		sendVerticesInfo(verticesInfo, loadFileType);
		processVerticesCounter++;
		if (processVerticesCounter%5000 == 0) {
			System.out.println("\nWaiting before sending more messages...");
			System.out.flush();
			Thread.sleep(20000);
			System.out.println("Resuming message sending ...");
			System.out.flush();
		}
		verticesInfo.clear();
		if (!loaderIsMember)
			vertexIds.clear();
	}

	public String[] load(String fileName, LoadFileType loadFileType, boolean isDirected) {
		BigTextFile file = null;
		Date startDate, endDate;
		Map<Long, LoadVertexInfo> verticesInfo = new HashMap<Long,LoadVertexInfo>();
		
		long fromNodeId=-1, toNodeId, lastFromNodeId;
		
		long edgeCounter=0, lineCounter=0;
		
		try {
			file = new BigTextFile(fileName);
			startDate = new Date();
			StringTokenizer tokenizer = null;
			System.out.print("Loading\n[");
			pendingLoadBlocks = 0;
			fileReadCompleted = false;
			loadingInProcess = true;
			vertexCounter = 0;
			processVerticesCounter = 0;
			
			addressVerticesInfo = new HashMap<String, List<LoadVertexInfo>[]>();
			vertexIds = new ArrayList<Long>();
			
			for (String line : file) {
				if (!line.startsWith("#") && !line.trim().equals("")) {
					tokenizer = new StringTokenizer(line);
					lineCounter++;
					switch (loadFileType) {
					case ADJ_LIST_TEXT_FILE:
			
						fromNodeId =  Long.parseLong(tokenizer.nextToken(","));
						
						while (tokenizer.hasMoreTokens()) { 
							addEdgeToVertexInfo(verticesInfo, fromNodeId, Long.parseLong(tokenizer.nextToken(",")), 
									isDirected);
							edgeCounter++;
						}
						
						if (verticesInfo.size() >= 500) 
							processVerticesInfo(verticesInfo, loadFileType);
						
						
						break;
					case SIMPLE_TEXT_FILE :
						
						lastFromNodeId = fromNodeId;
						fromNodeId = Long.parseLong(tokenizer.nextToken());
						toNodeId = Long.parseLong(tokenizer.nextToken());
						
						if (lastFromNodeId != fromNodeId && verticesInfo.size() >= 500) 
							processVerticesInfo(verticesInfo, loadFileType);
						
						addEdgeToVertexInfo(verticesInfo, fromNodeId, toNodeId, isDirected);
						edgeCounter++;
												
						break;
					default:
						throw new RuntimeException("Only text files can be processed with this loader");
					}
					
	

				}
				
				if (lineCounter > 0 && (lineCounter % 50000) == 0) {
					System.out.print("]\n" + lineCounter + " lines read\n[" );
					System.out.flush();
				}
				else if (lineCounter % 1000 == 0) {
					
					System.out.print(".");
					System.out.flush();
				}

			}
			
			if (!verticesInfo.isEmpty()) {
				sendVerticesInfo(verticesInfo, loadFileType);
				
				verticesInfo.clear();
			}
			
			
			System.out.println("]\nWaiting for pending load blocks....");
			fileReadCompleted = true;
			
			synchronized (lock) {
				while (loadingInProcess) {
					lock.wait();
				}
			}
			
			
			
			if (Configuration.getProperty(Key.VIRTUAL_EDGES).equals("true")) {
				ImgLogger.log(LogLevel.INFO, "Calculating local 2-Hop neighbors...");
				updateLocal2HopNeighbors();
			}
			
			
			
			endDate =  new Date();
			ImgLogger.log(LogLevel.INFO, "File succesfully loaded in " + (endDate.getTime()-startDate.getTime()) + 
					"ms. " + (vertexCounter) + 
					" vertices and " + edgeCounter + " edges were created. " + lineCounter + " lines were processed");
			
			return new String[] {String.valueOf(vertexCounter), String.valueOf(edgeCounter)}; 

		} catch (Exception e) {
			System.out.println("Error on line: " + lineCounter);
			e.printStackTrace();
			return null;
		} finally {
			if (file!=null) 
				file.Close();
		}
		
		
	}

	public void close() {
		if (clientThreads != null)
			for (ClientThread lt : clientThreads.values())
				lt.stop();
		if (!loaderIsMember) {
			socketToMember.close();
			context.term();
		}
		
	}
	
	public static int loadBlock(List<LoadVertexInfo> verticesInfo) {
		SteffiVertex vertex;
		int newVertices = 0;
		SteffiGraph graph = SteffiGraph.getInstance();
		
		
		for (LoadVertexInfo loadVertex : verticesInfo) {
			
			vertex = (SteffiVertex) graph.retrieveRawCell(loadVertex.getVertexId());
			
			if (vertex == null) {
				vertex = new SteffiVertex(loadVertex.getVertexId(), null, false);
				graph.storeCell(loadVertex.getVertexId(), vertex);
				newVertices++;
			}
			
			for (long outEdgeDest : loadVertex.getOutEdges()) 
				vertex.addPartialEdge(outEdgeDest, EdgeType.OUT, null, false);
			
			for (long inEdgeDest : loadVertex.getInEdges()) 
				vertex.addPartialEdge(inEdgeDest, EdgeType.IN, null, false);
		
			for (long undEdgeDest : loadVertex.getUndirectedEdges())
				vertex.addPartialEdge(undEdgeDest, EdgeType.UNDIRECTED, null, false);
		
			vertex.trimToSize();
			//vertex.compress();
		}
		
		return newVertices;
	}
	
		
	private static void processVertexInfo(SteffiGraphDBGraph graph, List<LoadVertexInfo> verticesInfo) {
		Vertex v = null, w = null;
		Map<Long, Vertex> tempVertices = new HashMap<Long, Vertex>();
		for (LoadVertexInfo vi : verticesInfo) {
			try{ 
				
				v = tempVertices.get(vi.getVertexId());
				if (v == null) {
						v = graph.addVertex(vi.getVertexId());
					tempVertices.put(vi.getVertexId(), v);
				}
				
				
				for (long destId : vi.getOutEdges()) {
					
					w = tempVertices.get(destId);
					if (w == null) {
						w = graph.addVertex(destId);
						tempVertices.put(destId, w);
					}
					graph.addEdge(null, v, w, null);
				}
				graph.stopTransaction(Conclusion.SUCCESS);
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}
		tempVertices.clear();
		
	}
	
	
	public static void singleProcessLoad(SteffiGraphDBGraph graph, String fileName) throws Exception {
		BigTextFile file = null;
		Date startDate, endDate;
		List<LoadVertexInfo> verticesInfo = new ArrayList<LoadVertexInfo>();
		LoadVertexInfo vertexInfo = null;
		long edgeCounter=0, lineCounter=0;
		long vertexId;
		try {
			file = new BigTextFile(fileName);
			startDate = new Date();
			StringTokenizer tokenizer = null;
			System.out.print("Loading\n[");
			vertexId = 0;
			for (String line : file) {
				lineCounter++;
				if (!line.startsWith("#") && !line.trim().equals("")) {
					tokenizer = new StringTokenizer(line);
					
					vertexInfo =  new LoadVertexInfo(Long.parseLong(tokenizer.nextToken(",").trim()));
					while (tokenizer.hasMoreTokens()) 
						vertexInfo.addOutEdge(Long.parseLong(tokenizer.nextToken(",").trim()));
					
					
					verticesInfo.add(vertexInfo);
					vertexId++;
					edgeCounter+=vertexInfo.getOutEdges().size();

					if (verticesInfo.size() >= 300) {
						processVertexInfo(graph, verticesInfo);
						verticesInfo.clear();
						System.out.println(vertexId + " vertices loaded...");
						
						

					}


				}
				

				
				 
			}
			
			
			
			if (!verticesInfo.isEmpty()) {
				processVertexInfo(graph, verticesInfo);
			}
			
			
			endDate =  new Date();
			System.out.println("File succesfully loaded in " + (endDate.getTime()-startDate.getTime()) + 
					"ms. " + (vertexId) + 
					" vertices and " + edgeCounter + " edges were created");

		} catch (Exception e) {
			System.out.println("Error on line: " + lineCounter);
			ImgLogger.logError(e, "Error loading " + fileName);
		} finally {
			if (file!=null) 
				file.Close();

		}
	}



}
