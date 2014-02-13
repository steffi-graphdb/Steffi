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
package com.steffi.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.infinispan.Cache;

import com.steffi.common.BigTextFile;
import com.steffi.model.Cell;
import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiVertex;
import com.steffi.storage.CacheContainer;
import com.steffi.storage.CellTransaction.TransactionConclusion;
import com.steffi.traversal.DistributedTraversal;
import com.steffi.traversal.EdgeTraversalConf;
import com.steffi.traversal.Evaluation;
import com.steffi.traversal.MatchEvaluatorConf;
import com.steffi.traversal.Path;
import com.steffi.traversal.TraversalResults;

/**
 * @author Aldemar Reynaga
 * Functions to execute read, write and traversal tests
 */
public class TestTools {
	
	public enum TestFileType{
		RANDOM,
		ALL_PATHS,
		NO_PATHS
	}
	

	public static long nextLong(Random rng, long n) {
		// error checking and 2^x checking removed for simplicity.
		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}

	
	
	private static Long genPathTest(Random randomGen, Cache<Long, Cell> cache, 
			SteffiVertex startVertex, int hops, EdgeType edgeType) throws Exception {
		
		int index;
		SteffiVertex vertex = startVertex;
		
		
		for (int i=0; i<hops; i++) {
			List<SteffiEdge> edges = vertex.getEdgesByType(edgeType);
			if (edges.isEmpty()) {
				return null;
			}
				
				
			index = randomGen.nextInt(edges.size());

			vertex = (SteffiVertex) cache.get(edges.get(index).getDestCellId());
		}
		
		if (vertex.getId() == startVertex.getId())
			return null;
		
		return vertex.getId();
	}
	
	
	
	public static void genPathTestFile(long minId, long maxId, int numTests, String outFile,
			int hops, EdgeType edgeType) throws Exception {
		BufferedWriter writer = null;
		Cache<Long, Cell> cache = CacheContainer.getCellCache();
		long startId=0;
		Long endId = null;
		Cell startCell=null;
		Random randomGen = new Random();

		try {
			writer = new BufferedWriter(new FileWriter(new File(
					outFile), false));

			writer.write("#START_ID, END_ID");
			
			for (int i=0; i<numTests; i++) {

				do {
					do {
						startId = nextLong(randomGen, maxId - minId) + minId;
						startCell = cache.get(startId);
					} while (startCell == null);
					endId = genPathTest(randomGen, cache, (SteffiVertex) startCell, hops, edgeType);
				} while (endId == null);
				
				writer.newLine();
				writer.write(startId + "," + endId);
			}

		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}
	
	
	public static void genTestFile(long minId, long maxId, int numTests, String outFile) throws Exception {
		BufferedWriter writer = null;
		Cache<Long, Cell> cache = CacheContainer.getCellCache();
		long startId=0, endId=0;
		Cell startCell=null, endCell=null;
		Random randomGen = new Random();

		try {
			writer = new BufferedWriter(new FileWriter(new File(
					outFile), false));

			writer.write("#START_ID, END_ID");



			for (int i=0; i<numTests; i++) {

				while (startCell == null) {
					startId = nextLong(randomGen, maxId - minId) + minId;
					startCell = cache.get(startId);
				}
				
				while (endCell == null || startCell.getId() == endCell.getId()) {
					endId = nextLong(randomGen, maxId - minId) + minId;
					endCell = cache.get(endId);
				}
				writer.newLine();
				writer.write(startId + "," + endId);
				startCell = null;
				endCell = null;


			}



		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}


	private static void runTraversal(DistributedTraversal traversal, 
			long startVertexId, long endVertexId, SteffiGraph graph, 
			List<Long> traversalTimes,
			BufferedWriter writer) throws Exception{

		TraversalResults results = traversal.traverse((SteffiVertex) graph.retrieveCell(startVertexId));

		traversalTimes.add(results.getTime());
		if (writer != null) {
			List<Path> paths = results.getPaths();

			writer.write(startVertexId + "," + endVertexId + "," +
					results.getTime());

			if (paths.isEmpty()) {
				writer.write(",N,");
			} else {
				String resPaths = "";
				for (Path path : paths)
					resPaths += path.toString() + "||";
				resPaths = resPaths.substring(0, resPaths.length()-2);
				writer.write(",Y," + resPaths);
			}
			writer.newLine();
			//writer.flush();
		}



	}

	private static Map<StatisticalIndicators, Double> calculateIndicators(List<Long> traversalTimes) {
		Map<StatisticalIndicators, Double> indicators = new HashMap<StatisticalIndicators, Double>();
		Collections.sort(traversalTimes);

		if (traversalTimes.size() % 2 == 0)
			indicators.put(StatisticalIndicators.MEDIAN, (traversalTimes.get((traversalTimes.size()/2) - 1) + 
					traversalTimes.get((traversalTimes.size()/2) + 1))/2D);
		else
			indicators.put(StatisticalIndicators.MEDIAN, traversalTimes.get(traversalTimes.size()/2)/2D);

		indicators.put(StatisticalIndicators.MIN, (double)traversalTimes.get(0));
		indicators.put(StatisticalIndicators.MAX, (double)traversalTimes.get(traversalTimes.size()-1));

		double sum = 0, mean;

		for (long time : traversalTimes) 
			sum += (double)time;

		mean = sum / traversalTimes.size();
		indicators.put(StatisticalIndicators.MEAN, mean);
		indicators.put(StatisticalIndicators.TOTAL, sum);


		sum = 0;
		for (long time : traversalTimes)
			sum += Math.pow(time-mean, 2);

		indicators.put(StatisticalIndicators.DEV_STD, Math.sqrt(sum/(traversalTimes.size()-1)));

		return indicators;
	}

	public static Map<StatisticalIndicators, Double> testTraversalFromFile(SteffiGraph graph, EdgeTraversalConf edgeTraversalConf, 
			Evaluation evaluation,
			String fileName,  
			int maxDepth, 
			String outLogFile,
			long delay) throws Exception{
		return testTraversalFromFile(graph, edgeTraversalConf, evaluation, fileName, maxDepth, outLogFile, delay, false);
	}
	
	
	public static Map<StatisticalIndicators, Double> testTraversalFromFile(SteffiGraph graph, EdgeTraversalConf edgeTraversalConf, 
			Evaluation evaluation,
			String fileName,  
			int maxDepth, 
			String outLogFile,
			long delay, boolean runConcurrentTest) throws Exception{
		BigTextFile file = null;
		DistributedTraversal traversal = new DistributedTraversal();
		BufferedWriter writer = null;
		List<Long> traversalTimes = new ArrayList<Long>();
		int counter = 0;

		long startVertexId, endVertexId;
		try {
			file = new BigTextFile(fileName);

			traversal.setHops(maxDepth);
			traversal.addEdgeTraversalConfs(edgeTraversalConf);
			MatchEvaluatorConf matchConf =  new MatchEvaluatorConf();
			matchConf.setEvaluation(evaluation);
			traversal.setMatchEvaluatorConf(matchConf);

			if (outLogFile != null) 
				writer = new BufferedWriter(new FileWriter(new File(
						outLogFile), false));

			for (String line : file) {
				if (!line.startsWith("#")) {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					startVertexId = Long.parseLong(tokenizer.nextToken());
					endVertexId = Long.parseLong(tokenizer.nextToken());
					matchConf.setCellId(endVertexId);

					runTraversal(traversal, startVertexId, endVertexId, graph, 
							traversalTimes, writer);
					counter++;
					if (!runConcurrentTest)
						System.out.println("Traversal # " + counter + " executed");

					Thread.sleep(delay);
				}
			}

			if (runConcurrentTest)
				return null;
			
			return calculateIndicators(traversalTimes);


		} finally {
			if (file!=null) 
				file.Close();
			if (writer != null) {
				writer.flush();
				writer.close();
			}

		}
	}
	
	
	public static Map<StatisticalIndicators, Double> testReads(String testFile, String outLogFile) throws Exception {
		return testReads(testFile, outLogFile, false);
	}
	
	public static Map<StatisticalIndicators, Double> testReads(String testFile, String outLogFile, boolean runConcurTest) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		try {
			file = new BigTextFile(testFile);
			List<Long> cellIds = new ArrayList<Long>();
			List<Long> times = new ArrayList<Long>();
			SteffiGraph graph = SteffiGraph.getInstance();
			long startTime, endTime;
			
			if (outLogFile != null) { 
				writer = new BufferedWriter(new FileWriter(new File(
						outLogFile), false));
				writer.write("CELL_ID, TIME(nanoseconds)");
			}
			
			for (String line : file) {
				if (!line.startsWith("#")) {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					cellIds.add(Long.parseLong(tokenizer.nextToken()));
					cellIds.add(Long.parseLong(tokenizer.nextToken()));
				}
			}
			
			for (long cellId : cellIds) {
				startTime = System.nanoTime();

				graph.retrieveCell(cellId);
				endTime = System.nanoTime();

				times.add(endTime-startTime);
				if (writer != null) {
					writer.newLine();
					writer.write(cellId + "," + (endTime-startTime));
				}
			}
			
			if (runConcurTest)
				return null;
			
			return calculateIndicators(times);
			
		} finally {
			if (file!=null) 
				file.Close();
			if (writer!=null)
				writer.close();
		}
	}
	
	public static Map<StatisticalIndicators, Double> testWrites(String testFile, String outLogFile) throws Exception {
		return testWrites(testFile, outLogFile, false);
	}
	
	public static Map<StatisticalIndicators, Double> testWrites(String testFile, String outLogFile, boolean runConcurTest) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		try {
			Random random = new Random();
			file = new BigTextFile(testFile);
			List<Long[]> cellIds = new ArrayList<Long[]>();
			List<Long> times = new ArrayList<Long>();
			SteffiGraph graph = SteffiGraph.getInstance();
			long startTime, endTime;
			Set<Long> newCellIds = new HashSet<Long>();
			long cellId; 
			
			if (outLogFile != null) 
				writer = new BufferedWriter(new FileWriter(new File(
						outLogFile), false));
			
			int transactionCounter=0;
			for (String line : file) {
				if (!line.startsWith("#")) {
					
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					boolean isNewId = false;
					
					do {
						cellId = nextLong(random, 50000) + 9999999999L;
						
						if (!newCellIds.contains(cellId)) {
							newCellIds.add(cellId);
							isNewId = true;
						}
					} while (!isNewId);
					
					cellIds.add(new Long[]{cellId, Long.parseLong(tokenizer.nextToken()), 
							Long.parseLong(tokenizer.nextToken())});
				}
			}
			
			if (writer != null)
				writer.write("NEW CELL ID, TIME(nanoseconds)");
			
			
			for (Long[] destCellIds : cellIds) {
				
				
				try {
				
					graph.startTransaction();
					SteffiVertex vertexA = (SteffiVertex) graph.retrieveCell(destCellIds[1]);
					SteffiVertex vertexB = (SteffiVertex) graph.retrieveCell(destCellIds[2]);
					//startTime = new Date().getTime();
					startTime = System.nanoTime();
					SteffiVertex vertex = graph.addVertex(destCellIds[0], null);
					vertex.addEdge(vertexA, true, null);
					vertex.addEdge(vertexB, true, null);
					graph.stopTransaction(TransactionConclusion.COMMIT);
					transactionCounter++;
				} catch (Exception x) {
					System.out.println("Error on transaction " + (transactionCounter+1) +
							", " + destCellIds[0] + "-" + destCellIds[1]);
					throw new Exception(x);
				}
				
				
				
				endTime = System.nanoTime();
				//endTime = new Date().getTime();
				
				if (writer != null) {
					writer.newLine();
					writer.write(String.valueOf(destCellIds[0]) + "," + (endTime-startTime));
				}
				
				times.add(endTime-startTime);
			}
			
			if (runConcurTest)
				return null;
			
			return calculateIndicators(times);
			
		} finally {
			if (file!=null) 
				file.Close();
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}	
	

	public static void runWriteClients (int numberOfClients, String fileName) {
		long startTime, endTime;
		
		startTime = new Date().getTime();
		
		ExecutorService executorService =  Executors.newFixedThreadPool(numberOfClients);
		
		for (int i = 0; i < numberOfClients; i++) {
	      Runnable client = new WriteClient(fileName);
	      executorService.execute(client);
	    }
		
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			
	    }
		
		endTime = new Date().getTime();
		
		System.out.println("Total time (ms): " + (endTime - startTime));
		
		System.out.println("Writes per second: " +  ((numberOfClients*200*1000)/(endTime - startTime)));
	}
	
	
	public static void runReadClients (int numberOfClients, String fileName) {
		long startTime, endTime;
		
		startTime = new Date().getTime();
		
		ExecutorService executorService =  Executors.newFixedThreadPool(numberOfClients);
		
		for (int i = 0; i < numberOfClients; i++) {
	      Runnable client = new ReadClient(fileName);
	      executorService.execute(client);
	    }
		
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			
	    }
		
		endTime = new Date().getTime();
		
		System.out.println("Total time (ms): " + (endTime - startTime));
		
		System.out.println("Reads per second: " +  ((numberOfClients*200*1000)/(endTime - startTime)));
	}
	
	
	private static List<NodePair> readQueries(String queryFileName) throws Exception {
		List<NodePair> queries =  new ArrayList<NodePair>();
		BigTextFile file = null;
		try {
			file = new BigTextFile(queryFileName);
			for (String line : file) {
				if (!(line.trim().equals("") || line.startsWith("#"))) {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					queries.add(new NodePair(Long.parseLong(tokenizer.nextToken()), 
							Long.parseLong(tokenizer.nextToken())));
				}
			}
		} finally {
			if (file != null) file.Close();
		}
		
		
		return queries;
	}
	
	public static void runTraversalClients(int numberOfClients, String queryFileName, int hops, 
			EdgeType edgeType, long testDuration) throws Exception {
		long numberOfTraversals;
		
		
		
		ExecutorService executorService =  Executors.newFixedThreadPool(numberOfClients);
		List<TraversalClient> clients = new ArrayList<TraversalClient>();
		
		List<NodePair> queries = readQueries(queryFileName);
		EdgeTraversalConf edgeTraversalConf = new EdgeTraversalConf("", edgeType);
		
		SteffiGraph graph =SteffiGraph.getInstance();
		
		
		for (int i = 0; i < numberOfClients; i++) 
			clients.add(new TraversalClient(hops, edgeTraversalConf, queries, graph));
			
	    //Launch client threads
		for (Runnable client : clients)
			executorService.execute(client);
		
		Thread.sleep(testDuration);
		numberOfTraversals = 0;
		for (TraversalClient client : clients) {
			client.stop();
			numberOfTraversals += client.getCounter();
		}
		
		
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			
	    }
		
		System.out.println("Traversals per second: " +  ((1000*numberOfTraversals)/testDuration));
		
		
	}
	
	
	
}
