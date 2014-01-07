package neo4j.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;



public class TestManager {

	
	public static boolean removeDirectory(File directory) {

		  // System.out.println("removeDirectory " + directory);

		  if (directory == null)
		    return false;
		  if (!directory.exists())
		    return true;
		  if (!directory.isDirectory())
		    return false;

		  String[] list = directory.list();

		  // Some JVMs return null for File.list() when the
		  // directory is empty.
		  if (list != null) {
		    for (int i = 0; i < list.length; i++) {
		      File entry = new File(directory, list[i]);

		      //        System.out.println("\tremoving entry " + entry);

		      if (entry.isDirectory())
		      {
		        if (!removeDirectory(entry))
		          return false;
		      }
		      else
		      {
		        if (!entry.delete())
		          return false;
		      }
		    }
		  }

		  return directory.delete();
		}

	
	private GraphTestCase graphTestCase;
	
	public TestManager (GraphTestCase testCase) {
		this.graphTestCase = testCase;
	}
	
	private void writeStatistics(BufferedWriter writer, Map<StatisticalIndicators, Double> statistics ) throws IOException {
		DecimalFormat df =  new DecimalFormat("##############.##", new DecimalFormatSymbols(Locale.US));
		writer.write("Statistical indicators of processing times");
		writer.newLine();
		writer.write("Minimum: " + df.format(statistics.get(StatisticalIndicators.MIN)));
		writer.newLine();
		writer.write("Maximum: " + df.format(statistics.get(StatisticalIndicators.MAX)));
		writer.newLine();
		writer.write("Median: " + df.format(statistics.get(StatisticalIndicators.MEDIAN)));
		writer.newLine();
		writer.write("Mean: " + df.format(statistics.get(StatisticalIndicators.MEAN)));
		writer.newLine();
		writer.write("Dev. standard: " + df.format(statistics.get(StatisticalIndicators.DEV_STD)));
		writer.newLine();
	}
	
	public void execute() throws IOException {
		
		BufferedWriter writer = null;
		GraphDatabaseService graph = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(
					graphTestCase.getWorkDirectory() + "results.txt"), false));
			writer.write("RESULTS FOR FILE: " + graphTestCase.getGraphFileName());
			writer.newLine();
			Map<StatisticalIndicators, Double> statistics;
			//Config cluster
			
			
			//Load file
			/*
			File directory = new File(graphTestCase.getStorageDirectory());
			if (!removeDirectory(directory))
				throw new RuntimeException("The directory " + graphTestCase.getStorageDirectory() + " was not completely deleted");
			TestTools.loadGraphV2(graphTestCase.getGraphFileName(), graphTestCase.getStorageDirectory(), graphTestCase.getLoadFileType());
			
			*/
			
			graph = new GraphDatabaseFactory().newEmbeddedDatabase(graphTestCase.getStorageDirectory());
			
			writer.newLine();
			writer.write("TRAVERSAL RESULTS (expressed in milliseconds)");
			writer.newLine();
			
			Direction direction = (graphTestCase.isDirected())?Direction.OUTGOING:Direction.BOTH;
			
		
			writer.newLine();
			writer.write("Test file containing 200 random queries that can have or not a path");
			writer.newLine();
			writer.write("Max hops: 3 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED") 
					+ " - Delay: " + graphTestCase.getDelay());
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTestFileName(), 3, 
					graphTestCase.getWorkDirectory() + "logTraversal-3.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that can have or not a path");
			writer.newLine();
			writer.write("Max hops: 4 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED"));
			writer.newLine();
			
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTestFileName(), 4, 
					graphTestCase.getWorkDirectory() + "logTraversal-4.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that have a path");
			writer.newLine();
			writer.write("Max hops: 3 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED"));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTest3HFileName(), 3, 
					graphTestCase.getWorkDirectory() + "logTraversal_3H.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that have a path");
			writer.newLine();
			writer.write("Max hops: 4 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED"));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTest4HFileName(), 4, 
					graphTestCase.getWorkDirectory() + "logTraversal_4H.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			
			
			//Read & write tests
			System.out.println("Processing read tests (expressed in nanoseconds)");
			statistics = TestTools.testReads(graph, graphTestCase.getTestFileName(), 
					graphTestCase.getWorkDirectory() + "logReads.txt");
			writer.newLine();
			writer.write("READ RESULTS (400 reads)");
			writer.newLine();
			writeStatistics(writer, statistics);
			
			System.out.println("Processing write tests (expressed in nanoseconds)");
			statistics = TestTools.testWrites(graph, graphTestCase.getTestFileName(), 
					graphTestCase.getWorkDirectory() + "logWrites.txt");
			writer.newLine();
			writer.write("WRITE RESULTS (200 writes)");
			writer.newLine();
			writeStatistics(writer, statistics);
			
			writer.flush();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		
		if (args[0].equals("CONSOLE")) {
			TestTools.runConsole();
		} else if (args[0].equals("TEST_CASE")) {
			GraphTestCase graphTestCase = new GraphTestCase(args[1]);
			
			TestManager testManager = new TestManager(graphTestCase);
			
			System.out.println("Starting tests...");
			testManager.execute();
			System.out.println("The tests were finished");
		}
		
		
		
		
	
	}

}
