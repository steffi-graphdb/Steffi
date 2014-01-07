package neo4j.tests;

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

import neo4j.tests.GraphTestCase.LoadFileType;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.Traversal;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.LuceneBatchInserterIndexProvider;

public class TestTools {
	
	/*
	private static enum LoadType {
		VERTEX_EDGE,
		ADJACENT_LIST
	}
	
	private static enum StatisticalIndicators {
		MEAN,
		MEDIAN,
		MIN,
		MAX,
		DEV_STD
		
	}
	*/
	
	private static class CustomEvaluator implements Evaluator {

		private String searchedName;
		
		public CustomEvaluator(String searchedName) {
			this.searchedName = searchedName;
		}
		
		public Evaluation evaluate(Path path) {
			Node node = path.endNode(); 
			if (/*node != null &&*/ node.getProperty("name").equals(searchedName))
				return Evaluation.INCLUDE_AND_PRUNE;
			
			return Evaluation.EXCLUDE_AND_CONTINUE;
		}
		
	}
	
	private enum Rels implements RelationshipType
	{
		REL
	}
	
	
	private static void runTraversalFromFile(Traverser traverser, 
			String startVertexId, String endVertexId, String directionString, 
			GraphDatabaseService graph, 
			List<Long> traversalTimes,
			BufferedWriter writer) throws Exception{

		Date startDate, endDate =null;
		long processTime=0;
		
		String pathString = "";
		startDate = new Date();
		for (Path path : traverser) {
			endDate = new Date();
			for (Node n : path.nodes())
				pathString += ("[" + n.getProperty("name") + "]" + directionString); 
			
			pathString = pathString.substring(0, pathString.length()-2);
			
			break;
		}
		
		if (endDate == null)
			endDate = new Date();
		
		processTime = endDate.getTime() - startDate.getTime();
		
		traversalTimes.add(processTime);
		if (writer != null) {
			writer.write(startVertexId + "," + endVertexId + "," +
					processTime);

			if (pathString.equals("")) {
				writer.write(",N,");
			} else {
				writer.write(",Y," + pathString);
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


		sum = 0;
		for (long time : traversalTimes)
			sum += Math.pow(time-mean, 2);

		indicators.put(StatisticalIndicators.DEV_STD, Math.sqrt(sum/(traversalTimes.size()-1)));

		return indicators;
	}
	
	public static Map<StatisticalIndicators, Double> testTraversalFromFile(GraphDatabaseService graph, 
			Direction direction, String fileName, int maxDepth,	String outLogFile, int delay) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		List<Long> traversalTimes = new ArrayList<Long>();
		int counter = 0;
		Traverser traverser;
		String directionString="";

		String startVertexId, endVertexId;
		try {
			file = new BigTextFile(fileName);

			if (outLogFile != null) 
				writer = new BufferedWriter(new FileWriter(new File(
						outLogFile), false));

			switch (direction) {
			case BOTH:
				directionString = "--";
				break;
			case INCOMING:
				directionString = "<-";
				break;
			case OUTGOING:
				directionString = "->";
				break;
			}
			
			for (String line : file) {
				if (!line.startsWith("#")) {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					startVertexId = tokenizer.nextToken();
					endVertexId = tokenizer.nextToken();
					
					traverser = traverse(graph, startVertexId, endVertexId, maxDepth, direction);
					
					
					runTraversalFromFile(traverser, startVertexId, endVertexId, directionString, 
							graph, traversalTimes, writer);
					
					counter++;
					System.out.println("Traversal # " + counter + " executed");
					Thread.sleep(delay);
					
				}
			}

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
	
	
	public static Traverser traverse(GraphDatabaseService graph, String startNodeName, String searchedNodeName, 
			int depth, Direction direction) {
		Traverser traverser = null;
		
		

		
		Index<Node> index = graph.index().forNodes("index");
		
		Node startNode = index.get("name", startNodeName).getSingle();
		
		if (startNode == null)
			throw new RuntimeException("Start node is Null");
		
		CustomEvaluator customEvaluator = new CustomEvaluator(searchedNodeName);
		
		TraversalDescription travDesc = Traversal.description().breadthFirst().evaluator(customEvaluator)
				.evaluator(Evaluators.toDepth(depth)).relationships(Rels.REL, direction);
				
		
		traverser = travDesc.traverse(startNode);
		
		return traverser;
	}
	
	
	public static void loadGraphV2(String fileName, String directory, LoadFileType loadType) throws Exception {
		long lineCounter = 0;
		BigTextFile file = null;
		Map<Long, Long> mapVertices = new HashMap<Long, Long>();
		
		file = new BigTextFile(fileName);
		String fromNodeId = null;
		List<String> toNodeIdList = new ArrayList<String>();
		
		Long fromNode, toNode;
		
		StringTokenizer tokenizer = null;
		long edgeCounter=0, vertexCounter=0; 
		RelationshipType rel = DynamicRelationshipType.withName( "REL" );
		
		BatchInserter inserter = BatchInserters.inserter(directory);
		
		
		BatchInserterIndexProvider indexProvider =
		        new LuceneBatchInserterIndexProvider( inserter );
		
		
		BatchInserterIndex index =
		        indexProvider.nodeIndex( "index", MapUtil.stringMap( "type", "exact" ) );
		index.setCacheCapacity( "name", 100100 );
		
		Map<String, Object> properties = null;
		
		
		
		
		for (String line : file) {
			if (!line.startsWith("#")) {
				lineCounter++;
				tokenizer = new StringTokenizer(line);
				toNodeIdList.clear();
				
				switch(loadType) {
				case ADJ_LIST_TEXT_FILE:
					fromNodeId = tokenizer.nextToken(",");
					while (tokenizer.hasMoreTokens()) 
						toNodeIdList.add(tokenizer.nextToken(",").trim());
					break;
				case SIMPLE_TEXT_FILE:
					fromNodeId = tokenizer.nextToken();
					toNodeIdList.add(tokenizer.nextToken());	
				}
				
				
				
				fromNode = mapVertices.get(Long.parseLong(fromNodeId));
						
				if (fromNode == null) {
					properties = new HashMap<String, Object>();
					properties.put( "name", fromNodeId );
					fromNode = inserter.createNode( properties );
					mapVertices.put(Long.parseLong(fromNodeId), fromNode);
					index.add(fromNode, properties);
					vertexCounter++;
				}
				
				for (String toNodeId : toNodeIdList) {
					toNode = mapVertices.get(Long.parseLong(toNodeId)); 
					if (toNode == null) {
						properties = new HashMap<String, Object>();
						properties.put( "name", toNodeId );
						toNode = inserter.createNode(properties);
						index.add(toNode, properties);
						mapVertices.put(Long.parseLong(toNodeId), toNode);
						vertexCounter++;
					}
					
					inserter.createRelationship(fromNode, toNode, rel, null );
					edgeCounter++;
				}
				
				if (lineCounter % 100000 == 0) {
					index.flush();
					
					System.out.println(lineCounter + " lines have been read");
					
				}
				
				
				
			}
			 
		}
		index.flush();
		
		indexProvider.shutdown();
		
		inserter.shutdown();
		System.out.println("File succesfully loaded: " + vertexCounter + 
				" vertices and " + edgeCounter + " edges");
	
		
	}
	
	/*
	private static void loadGraph(String fileName, String directory, LoadType loadType) throws Exception {
		
		long lineCounter = 0;
		BigTextFile file = null;
		GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(directory);
		
		file = new BigTextFile(fileName);
		String fromNodeId = null;
		List<String> toNodeIdList = new ArrayList<String>();
		
		Node fromNode, toNode;
		
		StringTokenizer tokenizer = null;
		long edgeCounter=0, vertexCounter=0; 
		Transaction tx = graph.beginTx();
		Index<Node> index = graph.index().forNodes("node-names");
		
		for (String line : file) {
			if (!line.startsWith("#")) {
				lineCounter++;
				tokenizer = new StringTokenizer(line);
				toNodeIdList.clear();
				
				switch(loadType) {
				case ADJACENT_LIST:
					fromNodeId = tokenizer.nextToken(",");
					while (tokenizer.hasMoreTokens()) 
						toNodeIdList.add(tokenizer.nextToken(",").trim());
					break;
				case VERTEX_EDGE:
					fromNodeId = tokenizer.nextToken();
					toNodeIdList.add(tokenizer.nextToken());	
				}
				
				fromNode = index.get("name", fromNodeId).getSingle();
				if (fromNode == null) {
					fromNode = graph.createNode();
					fromNode.setProperty("name", fromNodeId);
					index.add(fromNode, "name", fromNodeId);
					vertexCounter++;
				}
				
				for (String toNodeId : toNodeIdList) {
					toNode = index.get("name", toNodeId).getSingle();
					if (toNode == null) {
						toNode = graph.createNode();
						toNode.setProperty("name", toNodeId);
						index.add(toNode, "name", toNodeId);
						vertexCounter++;
					}
					
					fromNode.createRelationshipTo(toNode, Rels.REL);
					edgeCounter++;
				}
				
				if (lineCounter % 10000 == 0) {
					tx.success();
					tx.finish();
					tx = graph.beginTx();
					
					System.out.println(lineCounter + " lines have been read");
					
				}
				
				
				
			}
			 
		}
		
		tx.success();
		tx.finish();
		
		System.out.println("File succesfully loaded: " + vertexCounter + 
				" vertices and " + edgeCounter + " edges");
	
		graph.shutdown();

	}
	*/
	
	//public static void main(String [] args) {
		
	public static void runConsole() { 
		
		GraphDatabaseService graph = null;
		String graphFolder = null, startNode, endNode, hops, command;
		Direction direction ;
		try {
			
			
			do {
				command = IOUtils.readLine(">");
				
				if (command.equals("traverse")) {
					try {
						startNode = IOUtils.readLine("Start vertex: ");
						endNode = IOUtils.readLine("Searched vertex: ");
						hops = IOUtils.readLine("Max hops: ");		
						direction = Direction.valueOf(IOUtils.readLine("Edge direction (INCOMING/OUTGOING/BOTH): "));
						boolean allPaths = IOUtils.readLine("Search all paths? (Y/N): ").equals("Y");
						
						Date startDate = new Date();
						
						Traverser traverser = traverse(graph, startNode, endNode, 
								Integer.parseInt(hops), direction);
							
						for (Path path : traverser) {
							
							for (Node n : path.nodes())
								System.out.print(n.getProperty("name") + " ");
							System.out.println("");
		
							if (!allPaths)
								break;
								
						}
						
						Date endDate = new Date();
						System.out.println("Traversal time: " + (endDate.getTime() - startDate.getTime()) + "ms");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else if (command.equals("load")) {
					try {
						String fileName = IOUtils.readLine("Graph file: ");
						graphFolder = IOUtils.readLine("Neo4J folder: ");
						LoadFileType loadType = LoadFileType.valueOf(IOUtils.readLine("Load type: "));
						loadGraphV2(fileName, graphFolder, loadType);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					
				} else if (command.equals("open")) {
					graphFolder = IOUtils.readLine("Neo4J folder: ");
					
					graph = new GraphDatabaseFactory().newEmbeddedDatabase(graphFolder);
					System.out.println("Graph succesfully loaded");
					
				} else if (command.equals("testQueryLoad")) {
					String fileName = IOUtils.readLine("Test file: ");
					int maxHops = Integer.parseInt(IOUtils.readLine("Hops: "));
					direction = Direction.valueOf(IOUtils.readLine("Edge direction (INCOMING/OUTGOING/BOTH): "));
					int numberOfClients = Integer.parseInt(IOUtils.readLine("Number of clients: "));
					long duration = Long.parseLong(IOUtils.readLine("Duration (milliseconds): "));
					
					TestTools.runTraversalClients(graph, numberOfClients, fileName, maxHops, direction, duration);
				} else if (command.equals("traverseFile")) {
					try {
						Map<StatisticalIndicators, Double> statistics;
						
						String fileName = IOUtils.readLine("Traversal file: ");
						String outLogFile = IOUtils.readLine("Log File (optional): ");
						hops = IOUtils.readLine("Max hops: ");		
						direction = Direction.valueOf(IOUtils.readLine("Edge direction (INCOMING/OUTGOING/BOTH): "));
						
						statistics = testTraversalFromFile(graph, direction, fileName, Integer.parseInt(hops), 
								(outLogFile.trim().equals("")?null:outLogFile), 10 );
			
						System.out.println("File processing completed");
						
						if (!statistics.isEmpty()) {
							System.out.println("Statistical indicators of processing times");
							System.out.println("Minimum: " + statistics.get(StatisticalIndicators.MIN));
							System.out.println("Maximum: " + statistics.get(StatisticalIndicators.MAX));
							System.out.println("Median: " + statistics.get(StatisticalIndicators.MEDIAN));
							System.out.println("Mean: " + statistics.get(StatisticalIndicators.MEAN));
							System.out.println("Dev. standard: " + statistics.get(StatisticalIndicators.DEV_STD));
						}
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
			} while (!command.equals("exit"));
			
			if (graph != null)
				graph.shutdown();
			System.out.println("Bye...");
			
		} catch (Exception x) {
			x.printStackTrace();
		}
		
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
	
	public static Map<StatisticalIndicators, Double> testReads(GraphDatabaseService graph, String testFile,
			String outLogFile) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		try {
			file = new BigTextFile(testFile);
			List<Long> cellIds = new ArrayList<Long>();
			List<Long> times = new ArrayList<Long>();
			
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
			
			Index<Node> index = graph.index().forNodes("index");
			for (long cellId : cellIds) {
				startTime = System.nanoTime();
				//startTime = new Date().getTime();
				Node startNode = index.get("name", cellId).getSingle();
				endTime = System.nanoTime();
				//endTime = new Date().getTime();
				times.add(endTime-startTime);
				if (writer != null) {
					writer.newLine();
					writer.write(cellId + "," + (endTime-startTime));
				}
			}
			
			
			return calculateIndicators(times);
			
		} finally {
			if (file!=null) 
				file.Close();
			if (writer!=null)
				writer.close();
		}
	}
	
	public static Map<StatisticalIndicators, Double> testWrites(GraphDatabaseService graph, 
			String testFile, String outLogFile) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		try {
			Random random = new Random();
			file = new BigTextFile(testFile);
			List<Long[]> cellIds = new ArrayList<Long[]>();
			
			Index<Node> index = graph.index().forNodes("index");
			RelationshipType rel = DynamicRelationshipType.withName( "REL" );
			
			List<Long> times = new ArrayList<Long>();
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
				if (writer != null) {
					writer.newLine();
					writer.write(String.valueOf(destCellIds[0]));
				}
				
				
				
				
				//startTime = System.nanoTime();
				
				
				try {
				
					Transaction transaction = graph.beginTx();
					
					Node vertexA = index.get("name", destCellIds[1]).getSingle();
					Node vertexB = index.get("name", destCellIds[2]).getSingle();
					startTime = System.nanoTime();
					//startTime = new Date().getTime();
					
					Node vertex = graph.createNode();
					vertex.setProperty("name", destCellIds[0]);
					vertex.createRelationshipTo(vertexA, rel);
					vertex.createRelationshipTo(vertexB, rel);
					transaction.success();
					transaction.finish();
					
					transactionCounter++;
				} catch (Exception x) {
					System.out.println("Error on transaction " + (transactionCounter+1) +
							", " + destCellIds[0] + "-" + destCellIds[1]);
					throw new Exception(x);
				}
				
				//System.out.println("Transaction #" + transactionCounter + " processed");
				
				endTime = System.nanoTime();
				//endTime = new Date().getTime();
				
				if (writer != null) {
					writer.newLine();
					writer.write(String.valueOf(destCellIds[0]) + "," + (endTime-startTime));
				}
				
				times.add(endTime-startTime);
			}
			
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
	
	public static void runTraversalClients(GraphDatabaseService graph, int numberOfClients, String queryFileName, int hops, 
			Direction direction, long testDuration) throws Exception {
		long numberOfTraversals;
		
		ExecutorService executorService =  Executors.newFixedThreadPool(numberOfClients);
		List<TraversalClient> clients = new ArrayList<TraversalClient>();
		
		List<NodePair> queries = readQueries(queryFileName);
		
		for (int i = 0; i < numberOfClients; i++) 
			clients.add(new TraversalClient(hops, direction, queries, graph));
			
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
