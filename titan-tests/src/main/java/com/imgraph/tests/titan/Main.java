package com.imgraph.tests.titan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.imgraph.tests.titan.TraversalResults.Path;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;


public class Main {

	
	
	
	public static void main(String[] args) throws Exception {
		String command;
		
		TitanGraph graph = null;
		
		if (args[0].equals("CONSOLE")) {
			//String cassandraConfFile = args[1];
			String cassandraConfFile = null;
			do {
				command = IOUtils.readLine(">");
				try {
					if (command.equals("START_TITAN")) {
						boolean batchMode = IOUtils.readLine("Batch mode (Y/N)?: ").equals("Y");
						boolean embeddedCassandra = IOUtils.readLine("Embedded Cassandra (Y/N)?: ").equals("Y");
						String cassandraIpAddress = null;
						if (!embeddedCassandra)
							cassandraIpAddress = IOUtils.readLine("An IP address of cassandra cluster: ");
						else
							cassandraConfFile = IOUtils.readLine("Cassandra config file: ");
							
						graph = TestTools.openTitanGraph(batchMode, cassandraConfFile, embeddedCassandra, cassandraIpAddress);
						System.out.println("Titan DB started, batch_mode: " + batchMode);
					} else if (command.equals("LOAD_DATA")) {
						GraphTestCase graphTestCase = null;
						String cassandraIpAddress = null;
						if (args.length < 2) {
							graphTestCase = new GraphTestCase(IOUtils.readLine("Test case conf file: "));
							if (args.length < 3) 
								cassandraIpAddress = IOUtils.readLine("An IP address of cassandra cluster: ");
							else
								cassandraIpAddress = args[2];
						} else {
							graphTestCase = new GraphTestCase(args[1]);
							if (args.length < 3) 
								cassandraIpAddress = IOUtils.readLine("An IP address of cassandra cluster: ");
							else
								cassandraIpAddress = args[2];
						}
						
						
						
						TestTools.loadGraph(graphTestCase.getGraphFileName(), graphTestCase.getLoadFileType(), 
								cassandraIpAddress);
					} else if (command.equals("TEST_CASE")) {
						GraphTestCase graphTestCase = null;
						if (args.length < 2)
							graphTestCase = new GraphTestCase(IOUtils.readLine("Test case conf file: "));
						else
							graphTestCase = new GraphTestCase(args[1]);
						
						TestManager testManager = new TestManager(graphTestCase);
						
						System.out.println("Starting tests...");
						testManager.execute(graph);
						System.out.println("The tests were finished");
					} else if (command.equals("DELETE_DIR")) {
						String directoryName = IOUtils.readLine("Directory to delete: ");
						File directory = new File(directoryName);
						if (!TestManager.removeDirectory(directory))
							throw new RuntimeException("The directory " + directoryName + " was not completely deleted");
					} else if (command.equals("VERTEX")) {
						Iterator<Vertex> iter = graph.getVertices("name", Long.parseLong(IOUtils.readLine("Vertex id: "))).iterator();
						
						if (iter.hasNext()) {
							Vertex v = iter.next();
							System.out.println(v.getProperty("name"));
							System.out.print("IN edges: ");
							
							int c=0;
							for (Edge e : v.getEdges(Direction.IN)) {
								System.out.print(e.getVertex(Direction.OUT).getProperty("name") + ", ");
								c++;
							}
							System.out.println(" Total: " + c);
							System.out.print("OUT edges: ");
							c=0;
							for (Edge e : v.getEdges(Direction.OUT)) {
								System.out.print(e.getVertex(Direction.IN).getProperty("name") + ", ");
								c++;
							}
							System.out.println(" Total: " + c);
							
							
						} else {
							System.out.println("Vertex not found");
						}
						
					} else if (command.equals("STOP_TITAN")) {
						if (graph != null) {
							graph.shutdown();
							graph = null;
							System.out.println("Database was shutdown");
						}
					} else if (command.equals("TEST_TRAVERSAL")) {
						
						Traversal traversal =  new Traversal();
						traversal.setHops(Integer.parseInt(IOUtils.readLine("Max hops: ")));
						traversal.setDirection(Direction.valueOf(IOUtils.readLine("Direction: ")));
						long startId = Long.parseLong(IOUtils.readLine("Start id: "));
						traversal.setSearchedId(Long.parseLong(IOUtils.readLine("Searched id: ")));
						
						Vertex startVertex = graph.getVertices("name", startId).iterator().next();
						System.out.println("Starting traversal...");
						TraversalResults results = traversal.search(startVertex);
						System.out.print("Time: " + results.getTime() + "ms. ");
						for (Path path : results.getPaths())
							System.out.println(path);
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			} while (!command.equals("exit"));
		} else if (args[0].equals("RUN_TESTS")){
			//String cassandraConfFile = args[1];
			String cassandraIpAddress = args[2];
			String testCaseFile = args[1];
			GraphTestCase graphTestCase;
			
			try {
				graph = TestTools.openTitanGraph(false, null, false, cassandraIpAddress);
				
				graphTestCase = new GraphTestCase(testCaseFile);
				
				TestManager testManager = new TestManager(graphTestCase);
				
				System.out.println("Starting tests...");
				testManager.execute(graph);
				System.out.println("The tests were completed");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			
		}
		
		
			

		
		
			
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void oldMain(String[] args) throws Exception {
		
		TitanNode titanNode = new TitanNode();
		if (args[0].equals("START_NODE")) {
			
			
			//TitanNode titanNode = new TitanNode();
			System.out.println("Titan Node actor started");
			
			
			
		} else if (args[0].equals("LOAD_DATA")) {
			
			
			GraphTestCase graphTestCase = new GraphTestCase(args[1]);
			ActorMessenger actorMessenger = new ActorMessenger(titanNode.getActorSystem(),
					graphTestCase.getCassandraClusterIps());
			
			
			actorMessenger.startCassandraCluster(graphTestCase, true);
			
			//start current node
			TestTools.startTitan(new CassandraStartMsg(graphTestCase.getWorkDirectory(), 
					graphTestCase.getStorageDirectory(), actorMessenger.getNumberOfNodes(), 
					0, actorMessenger.getMainNodeIp(), actorMessenger.getMainNodeIp(), true));
			
		//	while(true);
			
			
			
		}

	}

}
