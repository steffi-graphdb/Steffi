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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;

import com.steffi.common.BigTextFile;
import com.steffi.common.ImgLogger;
import com.steffi.common.ImgLogger.LogLevel;
import com.steffi.loader.LoadVertexInfo;
import com.steffi.loader.ResponseProcessor;
import com.steffi.model.Cell;
import com.steffi.model.EdgeType;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.ClientThread;
import com.steffi.networking.messages.LoadMessage;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.networking.messages.WriteFileReqMsg;
import com.steffi.networking.messages.LoadMessage.LoadFileType;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.impls.steffi.SteffiGraphDBGraph;
import com.tinkerpop.blueprints.impls.steffi.SteffiGraphDBVertex;


/**
 * @author Aldemar Reynaga
 * Functions to read and write text files using a format called IMGP which allows
 * faster batch loading. (Deprecated) 
 */
public class SteffipFileTools implements ResponseProcessor {

	public static void loadVertexBlock(SteffiGraphDBGraph graph ,List<LoadVertexInfo> loadVertices) {
		SteffiVertex vertex;
		
		graph.startTransaction();
		for (LoadVertexInfo loadVertex : loadVertices) {
			vertex = ((SteffiGraphDBVertex)graph.addVertex(loadVertex.getVertexId())).getRawVertex();
			
			for (long outEdgeDest : loadVertex.getOutEdges()) 
				vertex.addPartialEdge(outEdgeDest, EdgeType.OUT, "");
			
			for (long inEdgeDest : loadVertex.getInEdges()) 
				vertex.addPartialEdge(inEdgeDest, EdgeType.IN, "");
		
			for (long undEdgeDest : loadVertex.getUndirectedEdges())
				vertex.addPartialEdge(undEdgeDest, EdgeType.UNDIRECTED, "");
		}
		
		
		graph.stopTransaction(Conclusion.SUCCESS);
	}
	
	
	private Map<String, String> clusterAddresses;
	private Map<String, ClientThread> clientThreads; 

	private String id;
	private Integer blocksSent;
	private Integer pendingWriteRequests;
	private boolean loadingInProcess;
	private boolean writingInProcess;
	private boolean fileReadingDone;
	private Object lock;
	private boolean update1HNInProcess;
	private int pending1HNRequests;
	
	public SteffipFileTools(String id) {
		this.id = id;
		clusterAddresses = StorageTools.getAddressesIps();
		initClientThreads();
	}
	
	private void initClientThreads() {
		this.clientThreads = new HashMap<String, ClientThread>();
		ClientThread clientThread = null;
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			clientThread = new ClientThread(entry.getValue(), entry.getValue(), "loader_" + this.id,
					this);
			clientThreads.put(entry.getKey(), clientThread);
			new Thread(clientThread).start();
		}
	}
	
	private void sendLoadBlock(Map<String, List<LoadVertexInfo>> addressVertices) {
		
		for (Entry<String, List<LoadVertexInfo>> entry : addressVertices.entrySet()) {
			LoadMessage loadMessage = new LoadMessage();
			loadMessage.setVerticesInfo(entry.getValue());
			loadMessage.setLoadFileType(LoadFileType.IMGP_FILE);
			
			clientThreads.get(entry.getKey()).addMsgToQueue(loadMessage);
			
			updateBlockCounter(1);
		}
		
		
	}
	
	private synchronized void updateBlockCounter(int difference) {
		blocksSent += difference;
	}
	
	private synchronized void verifyLoadComplete() {
		if (blocksSent == 0 && fileReadingDone) {
			synchronized (lock) {
				loadingInProcess = false;
				lock.notifyAll();
			}
		}
	}
	
	public void registerLoadReponse() {
		updateBlockCounter(-1);
		
		verifyLoadComplete();
		
		if (fileReadingDone)
			if (blocksSent % 20 == 0)
				System.out.println("Pending load blocks: " + blocksSent + "...");
	}
	
	public void closeClientThreads() {
		if (clientThreads != null)
			for (ClientThread lt : clientThreads.values())
				lt.stop();
	}
	
	
	public static void updateLocal1HopNeighbors() throws InterruptedException, ExecutionException {
		DistributedExecutorService des = new DefaultExecutorService(CacheContainer.getCellCache());
		Local2HopNeighborProcessor processor = new Local2HopNeighborProcessor();
		List<Future<Integer>> results =  des.submitEverywhere(processor);
		
		for (Future<Integer> future : results) {
	         if (future.get() == 0)
	        	 throw new RuntimeException("Error updating the local 1-Hop neighbors");
	    }
	}
	
	
	public void updateLocal1HopNeighborsV2() throws Exception {
		Message message = new Message(MessageType.UPD_2HOP_NEIGHBORS_REQ);
		
		update1HNInProcess = true;
		for (ClientThread ct : clientThreads.values()) 
			ct.addMsgToQueue(message);
		
		pending1HNRequests = clientThreads.size();
		
		lock = new String("UPDATE_2HN");
		
		synchronized (lock) {
			while (update1HNInProcess) {
				lock.wait();
			}
		}
		
	}
	
	
	
	
	private void addLoadVertexInfo(Map<String, List<LoadVertexInfo>> addressVertices, 
			LoadVertexInfo loadVertexInfo) {
		String cellAddress = StorageTools.getCellAddress(loadVertexInfo.getVertexId());
		List<LoadVertexInfo> vertices = addressVertices.get(cellAddress);
		
		if (vertices == null) {
			vertices = new ArrayList<LoadVertexInfo>();
			addressVertices.put(cellAddress, vertices);
		}
		vertices.add(loadVertexInfo);
		
	}
	
	public void readFromFile(SteffiGraphDBGraph graph, String fileName) throws Exception {
		BigTextFile file = null;
		StringTokenizer tokenizer = null;
		
		int inEdgesCounter, outEdgesCounter, undEdgesCounter;
		Map<String, List<LoadVertexInfo>> addressVertices = new HashMap<String, List<LoadVertexInfo>>();

		LoadVertexInfo loadVertexInfo =  null;
		long counter=0;
		Date startDate, endDate;
		
		
		
		try {
			startDate = new Date();
			file = new BigTextFile(fileName);
			loadingInProcess = true;
			fileReadingDone = false;
			lock = new String("LOAD");
			blocksSent = 0 ;
			
			ImgLogger.log(LogLevel.INFO, "Starting loading of file " + fileName);
			System.out.print("Loading\n[");
			for (String line : file) {
				if (!line.trim().equals("")) {
					tokenizer = new StringTokenizer(line);
					
					loadVertexInfo =  new LoadVertexInfo(Long.parseLong(tokenizer.nextToken()));
					
					inEdgesCounter = Integer.parseInt(tokenizer.nextToken());
					for (int i=0; i<inEdgesCounter; i++)
						loadVertexInfo.addInEdge(Long.parseLong(tokenizer.nextToken()));
					
					outEdgesCounter = Integer.parseInt(tokenizer.nextToken());
					for (int i=0; i<outEdgesCounter; i++)
						loadVertexInfo.addOutEdge(Long.parseLong(tokenizer.nextToken()));
					
					try {
						undEdgesCounter = Integer.parseInt(tokenizer.nextToken());
						for (int i=0; i<undEdgesCounter; i++)
							loadVertexInfo.addUndirectedEdge(Long.parseLong(tokenizer.nextToken()));
					} catch (NoSuchElementException nse) {
						
					}
					
					
					
					
					addLoadVertexInfo(addressVertices, loadVertexInfo);
					counter++;
					
					if (counter%500==0) {
						
						System.out.print(".");
						System.out.flush();
						
						sendLoadBlock(addressVertices);
						
						addressVertices.clear();
					} 
				}
			}
			
			fileReadingDone = true;
			
			if (!addressVertices.isEmpty()) {
				sendLoadBlock(addressVertices);
			}
			
			System.out.println("]");
			
			synchronized (lock) {
				while (loadingInProcess) {
					lock.wait();
				}
			}
			verifyLoadComplete();
			
			Date subStartDate = new Date();
			ImgLogger.log(LogLevel.INFO, "Calculating local 2-Hop neighbors");

			updateLocal1HopNeighborsV2();
			Date subEndDate = new Date();
			ImgLogger.log(LogLevel.INFO, "2 Hop neighbors processed in " + (subEndDate.getTime() - subStartDate.getTime()) + " ms");
			
			
			
			endDate =  new Date();


			ImgLogger.log(LogLevel.INFO, "File succesfully loaded in " + (endDate.getTime() - startDate.getTime()) + 
					"ms. "+ counter + " vertices have been processed");
			
			
		} finally {
			if (file != null) file.Close();
		}
		
	}

	@Override
	public void processResponse(Message message) {
		if (message.getType().equals(MessageType.LOAD_REP)) {
			String response[] = message.getBody().split("::");
			
			if (response[0].equals("OK"))
				registerLoadReponse();
			else
				throw new RuntimeException("Error processing a load block, response: " + 
						message.getBody());
		} else if (message.getType().equals(MessageType.WRITE_TO_FILE_REP)) {
			if (message.getBody().equals("OK"))
				registerWriteResponse();
			else
				throw new RuntimeException("Error processing a write file request");
		} else if (message.getType().equals(MessageType.UPD_2HOP_NEIGHBORS_REP)) {
			if (message.getBody().equals("OK"))
				registerUpd1HNResponse();
			else
				throw new RuntimeException("Error processing an update request for local 2-hop neighbors");
		}
	}
	
	private synchronized void registerUpd1HNResponse() {
		pending1HNRequests--;
		System.out.println("Pending 2-hop neighbors requests: " + pending1HNRequests);
		if (pending1HNRequests == 0) {
			synchronized (lock) {
				update1HNInProcess = false;
				lock.notifyAll();
			}
		}
		
	}

	private synchronized void registerWriteResponse() {
		pendingWriteRequests--;
		System.out.println("Pending write responses: " + pendingWriteRequests);
		if (pendingWriteRequests == 0) {
			synchronized (lock) {
				writingInProcess = false;
				lock.notifyAll();
			}
		}
		
	}
	
	public void writeToFile(String fileNamePrefix, String directory) throws Exception {
		WriteFileReqMsg message = new WriteFileReqMsg();
		
		message.setDirectory(directory);
		message.setFileNamePrefix(fileNamePrefix);
		
		
		writingInProcess = true;
		for (ClientThread ct : clientThreads.values()) 
			ct.addMsgToQueue(message);
		pendingWriteRequests = clientThreads.size();
		
		lock = new String("WRITE");
		
		synchronized (lock) {
			while (writingInProcess) {
				lock.wait();
			}
		}
		
		System.out.println("The files were written in the directory " + directory +
				" in each node with the prefix " + fileNamePrefix);
		
		
	}
	
	
	public static Message processWriteRequest(WriteFileReqMsg message) {
		Cache<Long, Cell> cache = CacheContainer.getCellCache();
		
		BufferedWriter bufWriter = null;
		Message response = new Message(MessageType.WRITE_TO_FILE_REP);
		
		String fileName = message.getDirectory() + message.getFileNamePrefix() 
				+ "_" + cache.getCacheManager().getAddress().toString();
		
		try {
			FileUtilities.writeToFile(fileName);
			response.setBody("OK");
			System.out.println("$$$$$$$$Data write to " + fileName);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setBody("ERROR: " + ex.getMessage());
		} finally {
			if (bufWriter!=null){try{bufWriter.close();}catch(IOException ioe){}}
			
		}
		
		return response;
	}
	
	
	
	
}
