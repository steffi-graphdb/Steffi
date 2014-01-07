package com.imgraph.tests.titan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;




import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TBinaryProtocol;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;




import com.imgraph.tests.titan.GraphTestCase.LoadFileType;
import com.imgraph.tests.titan.Traversal.SearchMethod;

import com.imgraph.tests.titan.TraversalResults.Path;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.VertexIDType;


public class TestTools {
	
	public static String getLocalIP()
    {

        String ipOnly = "";
        try
        {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            if (nifs == null) return "";
            while (nifs.hasMoreElements())
            {
                NetworkInterface nif = nifs.nextElement();
                // We ignore subinterfaces - as not yet needed.

                if (!nif.isLoopback() && nif.isUp() && !nif.isVirtual())
                {
                    Enumeration<InetAddress> adrs = nif.getInetAddresses();
                    while (adrs.hasMoreElements())
                    {
                        InetAddress adr = adrs.nextElement();
                        if (adr != null && !adr.isLoopbackAddress() && (nif.isPointToPoint() || !adr.isLinkLocalAddress()))
                        {
                            String adrIP = adr.getHostAddress();
                            String adrName;
                            if (nif.isPointToPoint()) // Performance issues getting hostname for mobile internet sticks
                                adrName = adrIP;
                            else
                                adrName = adr.getCanonicalHostName();

                            if (!adrName.equals(adrIP))
                                return adrIP;
                            else
                                ipOnly = adrIP;
                        }
                    }
                }
            }
//            if (ipOnly.length()==0) Logger.getLogger(Net.class.getName()).log(Level.WARNING, "No IP address available");
            return ipOnly;
        }
        catch (SocketException ex)
        {
            //Logger.getLogger(Net.class.getName()).log(Level.WARNING, "No IP address available", ex);
            return "";
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
	
	private static class NullRepresenter extends Representer {
        public NullRepresenter() {
            super();
            // null representer is exceptional and it is stored as an instance
            // variable.
            this.nullRepresenter = new RepresentNull();
        }
        
     

        private class RepresentNull implements Represent {
            public Node representData(Object data) {
                // possible values are here http://yaml.org/type/null.html
                return representScalar(Tag.NULL, "");
            }
        }
    }
	
	public static TitanGraph startTitan(CassandraStartMsg cassandraStartMsg) throws IOException {
		String cassandraConfFileName = TestTools.genCassandraYaml(cassandraStartMsg);
		
		File directory = new File(cassandraStartMsg.getCassandraDirectory());
		if (!TestManager.removeDirectory(directory))
			throw new RuntimeException("The directory " + 
					cassandraStartMsg.getCassandraDirectory() + 
					" was not completely deleted");
		
		TitanGraph graph = TestTools.openTitanGraph(cassandraStartMsg.isStartInBatchMode(), cassandraConfFileName, true, null);
		System.out.println("Titan graph was started in " + (cassandraStartMsg.isStartInBatchMode()?"normal mode":"batch mode"));
		return graph;
	}
	
	@SuppressWarnings("unchecked")
	public static String genCassandraYaml(CassandraStartMsg cassandraStartMsg) throws IOException{
		InputStream input = new String().getClass().getResourceAsStream("/cassandra.yaml");
        Yaml inYaml = new Yaml();
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) inYaml.load(input);
        
        BigInteger initialToken =genInitialToken(cassandraStartMsg.getNumberOfNodes(), cassandraStartMsg.getNodeNumber()); 
        
   		System.out.println("Token: " + initialToken);
        		
        data.put("initial_token", initialToken);
        
        data.put("listen_address", cassandraStartMsg.getLocalIpAddress());
        data.put("rpc_address", cassandraStartMsg.getLocalIpAddress());
        data.put("endpoint_snitch", "RackInferringSnitch");
        
        ((LinkedHashMap)((ArrayList) ((ArrayList<LinkedHashMap<String, Object>>)data.get("seed_provider")).
        		get(0).get("parameters")).get(0)).put("seeds", cassandraStartMsg.getMainNodeIp());
        
        ((ArrayList)data.get("data_file_directories")).clear();
        ((ArrayList)data.get("data_file_directories")).add(cassandraStartMsg.getCassandraDirectory());
        data.put("commitlog_directory", cassandraStartMsg.getCassandraDirectory() + "commitLog");
        data.put("saved_caches_directory", cassandraStartMsg.getCassandraDirectory() + "saved_caches");
        
        
        Yaml outYaml =  new Yaml(new NullRepresenter());
        String fileName = cassandraStartMsg.getTempDirectory() +  "cassandra_" + cassandraStartMsg.getNodeNumber() + ".yaml";
        Writer writer = new FileWriter(fileName);
        outYaml.dump(data, writer);
        writer.close();
        System.out.println("The file " + fileName + " was generated satisfactorily");
        return fileName;
	}
	
	private static BigInteger genInitialToken(int numberOfNodes, int nodeNumber) {
		BigInteger token = BigInteger.valueOf(nodeNumber);
        BigInteger pow   = BigInteger.valueOf(2).pow(127).subtract(BigInteger.ONE);
        token = token.multiply(pow).divide(BigInteger.valueOf(numberOfNodes));
        return token.abs();
	}

	public static TitanGraph openTitanGraph(boolean batchMode, String cassandraConfigDir, 
			boolean embeddedCassandra, String cassandraIpAddress) {
		BaseConfiguration configuration = new BaseConfiguration();
		
		if (batchMode) {
			configuration.setProperty("storage.batch-loading", "true");
			configuration.setProperty("storage.buffer-size", "2048");
			configuration.setProperty("storage.write-attempts", "10");
			configuration.setProperty("storage.attempt-wait", "1000");
		}
		
		if (embeddedCassandra) {
			configuration.setProperty("storage.backend", "embeddedcassandra");
			configuration.setProperty("storage.cassandra-config-dir", "file:///" + cassandraConfigDir);
		} else {
			configuration.setProperty("storage.backend","cassandra");
			configuration.setProperty("storage.hostname", cassandraIpAddress);
		}
		
		
		/*
		
		*/
		return TitanFactory.open(configuration);
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
	
	public static Map<StatisticalIndicators, Double> testTraversalFromFile(TitanGraph graph, 
			Direction direction, String fileName, int maxDepth,	String outLogFile, int delay,
			SearchMethod searchMethod) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		List<Long> traversalTimes = new ArrayList<Long>();
		int counter = 0;
		Traversal traversal = new Traversal();
		

		String startVertexId, endVertexId;
		try {
			file = new BigTextFile(fileName);

			if (outLogFile != null) 
				writer = new BufferedWriter(new FileWriter(new File(
						outLogFile), false));

			traversal.setHops(maxDepth);
			traversal.setDirection(direction);
			traversal.setSearchMethod(searchMethod);
			
			for (String line : file) {
				if (!line.startsWith("#")) {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					startVertexId = tokenizer.nextToken();
					endVertexId = tokenizer.nextToken();
					
					traversal.setSearchedId(Long.parseLong(endVertexId)); 
					
					Iterator<Vertex> iterator = graph.getVertices("name", Long.parseLong(startVertexId)).iterator();
					
					if (!iterator.hasNext())
						throw new RuntimeException("Vertex not found with id " + startVertexId);
					
					
					
					TraversalResults results = traversal.search(iterator.next());
					traversalTimes.add(results.getTime());
					
					String pathString = "";
					
					if (!results.getPaths().isEmpty())
						pathString = results.getPaths().get(0).toString();
					
					if (writer != null) {
						writer.write(startVertexId + "," + endVertexId + "," +
								results.getTime());

						if (pathString.equals("")) {
							writer.write(",N,");
						} else {
							writer.write(",Y," + pathString);
						}
						writer.newLine();
					}
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
	
	public static Map<StatisticalIndicators, Double> testReads(TitanGraph graph, String testFile,
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
			
			
			for (long cellId : cellIds) {
				startTime = System.nanoTime();
				//startTime = new Date().getTime();
				graph.getVertices("name", cellId).iterator().next();
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
	
	public static Map<StatisticalIndicators, Double> testWrites(TitanGraph graph, 
			String testFile, String outLogFile) throws Exception {
		BigTextFile file = null;
		BufferedWriter writer = null;
		try {
			Random random = new Random();
			file = new BigTextFile(testFile);
			List<Long[]> cellIds = new ArrayList<Long[]>();
			
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
				
				try {
					
					Vertex vertexA = graph.getVertices("name", destCellIds[1]).iterator().next();
							
					Vertex vertexB = graph.getVertices("name", destCellIds[2]).iterator().next();
					startTime = System.nanoTime();
					
					Vertex vertex = graph.addVertex(null);
					vertex.setProperty("name", destCellIds[0]);
					vertex.addEdge("link", vertexA);
					vertex.addEdge("link", vertexB);
					graph.commit();
					
					transactionCounter++;
				} catch (Exception x) {
					System.out.println("Error on transaction " + (transactionCounter+1) +
							", " + destCellIds[0] + "-" + destCellIds[1]);
					throw new Exception(x);
				}
				
				//System.out.println("Transaction #" + transactionCounter + " processed");
				
				endTime = System.nanoTime();
				
				if (writer != null) {
					writer.newLine();
					writer.write(String.valueOf(destCellIds[0]) + "," + (endTime-startTime));
				}
				
				//endTime = new Date().getTime();
				
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
	
	public static void loadGraphV2(String fileName, 
			LoadFileType loadType, String cassandraAddress) throws Exception {
		long lineCounter = 0;
		BigTextFile file = null;
		
		file = new BigTextFile(fileName);
		String fromNodeId = null;
		List<String> toNodeIdList = new ArrayList<String>();
		Map<String, Long> vertexIds = new HashMap<String, Long>();
		Vertex fromNode = null, toNode = null;
		
		StringTokenizer tokenizer = null;
		long edgeCounter=0, vertexCounter=0; 
		
		//clearCassandra();
		
		//Iterator<Vertex> vertexIterator  = null;
		TitanGraph graph = openTitanGraph(true, null, false, cassandraAddress);
		BatchGraph<TitanGraph> bgraph = new BatchGraph<TitanGraph>(graph, VertexIDType.NUMBER, 20000);
		graph.createKeyIndex("name", Vertex.class);
		
		long start = new Date().getTime();
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
				default:
				}
				
				fromNode = bgraph.getVertex(Long.parseLong(fromNodeId));
		        if (fromNode==null) 
		        	fromNode = bgraph.addVertex(Long.parseLong(fromNodeId));
		        fromNode.setProperty("name", Long.parseLong(fromNodeId));
				
				for (String toNodeId : toNodeIdList) {
					toNode = bgraph.getVertex(Long.parseLong(toNodeId));
			        if (toNode==null) 
			        	toNode = bgraph.addVertex(Long.parseLong(toNodeId));
					
			        toNode.setProperty("name", Long.parseLong(toNodeId));
			        
			        bgraph.addEdge(null, fromNode,toNode, "link");
			        
					edgeCounter++;
				}
				
				if (lineCounter % 500 == 0) 
					System.out.print(".");
				
				if (lineCounter % 50000 == 0) 
					System.out.println("\n" + lineCounter + " lines have been read");
			}
		}
		long end = new Date().getTime();
		graph.shutdown();
		
		System.out.println("File succesfully loaded: " + vertexCounter + 
				" vertices and " + edgeCounter + " edges. Time(ms): " + (end-start));
		
	}
	
	
	public static void loadGraph(String fileName, 
			LoadFileType loadType, String cassandraAddress) throws Exception {
		long lineCounter = 0;
		BigTextFile file = null;
		
		file = new BigTextFile(fileName);
		String fromNodeId = null;
		List<String> toNodeIdList = new ArrayList<String>();
		Map<String, Long> vertexIds = new HashMap<String, Long>();
		Vertex fromNode = null, toNode = null;
		
		StringTokenizer tokenizer = null;
		long edgeCounter=0, vertexCounter=0; 
		
		//clearCassandra();
		
		//Iterator<Vertex> vertexIterator  = null;
		TitanGraph graph = openTitanGraph(true, null, false, cassandraAddress);
		
		Long vertexId;
		Iterator<Vertex> vertexIterator;
		graph.createKeyIndex("name", Vertex.class);
		long start = new Date().getTime();
		
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
				default:
				}
				
				
				
				vertexId = vertexIds.get(fromNodeId);
				
				if (vertexId != null) {
					fromNode = graph.getVertex(vertexId);
				} else {
					/*
					vertexIterator = graph.getVertices("name", Long.parseLong(fromNodeId)).iterator();
					
					if (vertexIterator.hasNext()) {
						fromNode = vertexIterator.next();
					} else {*/
						fromNode = graph.addVertex(Long.parseLong(fromNodeId));
						fromNode.setProperty("name", Long.parseLong(fromNodeId));
						vertexIds.put(fromNodeId, (Long)fromNode.getId());
						vertexCounter++;
					//}
				}
				
				for (String toNodeId : toNodeIdList) {
					vertexId = vertexIds.get(toNodeId);
					
					if (vertexId != null) {
						toNode = graph.getVertex(vertexId);
					} else {
						/*
						vertexIterator = graph.getVertices("name", Long.parseLong(toNodeId)).iterator();
						
						if (vertexIterator.hasNext()) {
							toNode = vertexIterator.next();
						} else {*/
							toNode = graph.addVertex(Long.parseLong(toNodeId));
							toNode.setProperty("name", Long.parseLong(toNodeId));
							vertexIds.put(toNodeId, (Long) toNode.getId());
							vertexCounter++;
						//}
						
					}
					
					graph.addEdge(null, fromNode, toNode, "link");
					edgeCounter++;
				}
				
				if (lineCounter % 500 == 0) {
					System.out.print(".");
				}
				
				if (lineCounter % 50000 == 0) {
					System.out.println("\n" + lineCounter + " lines have been read");
				}
				
				
				if (vertexIds.size() % 500000 == 0) {
					System.out.println("Flushing data.... ");
					graph.commit();
				//if (vertexIds.size() > 1000000) {
				
					/*
					vertexIds.clear();
					graph.shutdown();
					Thread.sleep(10000);
					System.out.println("Resuming load....");
					graph = openTitanGraph(true, null, false, cassandraAddress);
					*/
				}
				
			}
			 
		}
		long end = new Date().getTime();
		graph.shutdown();
		
		System.out.println("File succesfully loaded: " + vertexCounter + 
				" vertices and " + edgeCounter + " edges. Time(ms): " + (end-start));
	
		
	}
	
	private static void clearCassandra() {
		TTransport tr = new TFramedTransport(new TSocket("127.0.0.1", 9160));
        TProtocol proto = new TBinaryProtocol(tr);
        Cassandra.Client client = new Cassandra.Client(proto);
        try {
	        tr.open();
	        //String cql="use titan;";
	        client.execute_cql_query(ByteBuffer.wrap("drop keyspace titan;".getBytes()), Compression.NONE);
	        tr.close();
        } catch (Exception e) {
        	
        }
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
	
		//clearCassandra();
		
		//genCassandraYaml("c:\\uclouvain\\master_thesis\\", "/mnt/data/imgraph/cassandra/", 
			//	2, 1, "192.168.0.214", "192.168.0.215");
		
		//TitanGraph g = openTitanGraph(false, "C:\\uclouvain\\master_thesis\\out.yaml");
	
//		loadGraph("c:\\uclouvain\\master_thesis\\graph_data\\soc-Epinions1\\soc-Epinions1.txt", LoadFileType.SIMPLE_TEXT_FILE, 
	//			"C:\\uclouvain\\master_thesis\\out.yaml");
	
	//	loadGraph("c:\\uclouvain\\master_thesis\\graph_data\\tiny.txt", LoadFileType.SIMPLE_TEXT_FILE, 
		//					"C:\\uclouvain\\master_thesis\\tests\\cassandra.yaml");
		
			/*	
		for (Vertex v1 : g.getVertices()) {
			System.out.println("\nVertex name: " + v1.getProperty("name") + ", id: " + v1.getId() + ":" + v1.getId().getClass());
			System.out.println("OUT");
			for (Edge e : v1.getEdges(Direction.OUT)) 
				System.out.print(e.getVertex(Direction.IN).getProperty("name") + " ");
			
			System.out.println("\nIN");
			for (Edge e : v1.getEdges(Direction.IN)) 
				System.out.print(e.getVertex(Direction.OUT).getProperty("name") + " ");
		}
			
		
		Vertex v = g.getVertex(4L);
		System.out.println(v.getProperty("name"));
		
		Traversal traversal =  new Traversal();
		traversal.setHops(3);
		traversal.setDirection(Direction.OUT);
		traversal.setSearchedId(23L);
		
		Vertex startVertex = g.getVertices("name", 1716L).iterator().next();
		System.out.println("Starting traversal...");
		TraversalResults results = traversal.search(startVertex);
		System.out.print("Time: " + results.getTime() + "ms. ");
		for (Path path : results.getPaths())
			System.out.println(path);
		
		g.shutdown();
		/*
		Iterator<Vertex> vertexIterator = g.getVertices("name", "1").iterator();
		
		if (vertexIterator.hasNext()) {
			Vertex v1 = vertexIterator.next();
			System.out.println("Name: " + v1.getProperty("name"));
			System.out.println("OUT\n");
			for (Edge e : v1.getEdges(Direction.OUT)) 
				System.out.print(e.getVertex(Direction.OUT).getProperty("name") + " ");
			
			System.out.println("\nIN\n");
			for (Edge e : v1.getEdges(Direction.IN)) 
				System.out.print(e.getVertex(Direction.IN).getProperty("name") + " ");
		} else {
			System.out.println("NOT FOUND!!!");
		}
		*/
		
	}

}
