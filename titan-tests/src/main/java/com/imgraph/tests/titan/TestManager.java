package com.imgraph.tests.titan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;



import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;




import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;

public class TestManager {

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
	
	@SuppressWarnings("unchecked")
	public void genCassandraYaml() throws IOException{
		InputStream input = getClass().getResourceAsStream("/cassandra.yaml");
        Yaml inYaml = new Yaml();
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) inYaml.load(input);
        
        
        
        Yaml outYaml =  new Yaml(new NullRepresenter());
        Writer writer = new FileWriter(graphTestCase.getWorkDirectory() +  "cassandra.yaml");
        outYaml.dump(data, writer);
        writer.close();
	}
	
	
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
	
	public void execute(TitanGraph graph) throws IOException {
		
		BufferedWriter writer = null;
		//TitanGraph graph = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(
					graphTestCase.getWorkDirectory() + "results.txt"), false));
			writer.write("TITAN GRAPH DATABASE");
			writer.newLine();
			writer.write("====================");
			writer.newLine();
			writer.write("RESULTS FOR FILE: " + graphTestCase.getGraphFileName());
			writer.newLine();
			Map<StatisticalIndicators, Double> statistics;
			//Config cluster
			
			
			//Load file
			/*
			File directory = new File(graphTestCase.getStorageDirectory());
			if (!removeDirectory(directory))
				throw new RuntimeException("The directory " + graphTestCase.getStorageDirectory() + " was not completely deleted");
			TestTools.loadGraph(graphTestCase.getGraphFileName(), graphTestCase.getLoadFileType(), 
					graphTestCase.getCassandraConfigDir());
			*/
			//graph = TestTools.openTitanGraph(false, graphTestCase.getCassandraConfigDir());
			
			writer.newLine();
			writer.write("TRAVERSAL RESULTS (expressed in milliseconds)");
			writer.newLine();
			
			Direction direction = (graphTestCase.isDirected())?Direction.OUT:Direction.BOTH;
			
		
			writer.newLine();
			writer.write("Test file containing 200 random queries that can have or not a path");
			writer.newLine();
			writer.write("Max hops: 3 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED") 
					+ " - Delay: " + graphTestCase.getDelay());
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTestFileName(), 3, 
					graphTestCase.getWorkDirectory() + "logTraversal-3.txt", graphTestCase.getDelay(),
					graphTestCase.getSearchMethod()
					);
			writeStatistics(writer, statistics);
			
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that can have or not a path");
			writer.newLine();
			writer.write("Max hops: 4 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED"));
			writer.newLine();
			
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTestFileName(), 4, 
					graphTestCase.getWorkDirectory() + "logTraversal-4.txt", graphTestCase.getDelay(),
					graphTestCase.getSearchMethod());
			writeStatistics(writer, statistics);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that have a path");
			writer.newLine();
			writer.write("Max hops: 3 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED"));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTest3HFileName(), 3, 
					graphTestCase.getWorkDirectory() + "logTraversal_3H.txt", graphTestCase.getDelay(),
					graphTestCase.getSearchMethod());
			writeStatistics(writer, statistics);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that have a path");
			writer.newLine();
			writer.write("Max hops: 4 - Edge type: " + (graphTestCase.isDirected()?"OUT":"UNDIRECTED"));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(graph, 
					direction, graphTestCase.getTest4HFileName(), 4, 
					graphTestCase.getWorkDirectory() + "logTraversal_4H.txt", graphTestCase.getDelay(),
					graphTestCase.getSearchMethod());
			writeStatistics(writer, statistics);
			
			
			//Read & write tests
			System.out.println("Processing read tests");
			statistics = TestTools.testReads(graph, graphTestCase.getTestFileName(),
					graphTestCase.getWorkDirectory() + "logReads.txt");
			writer.newLine();
			writer.write("READ RESULTS (expressed in nanoseconds)");
			writer.newLine();
			writeStatistics(writer, statistics);
			
			System.out.println("Processing write tests");
			statistics = TestTools.testWrites(graph, graphTestCase.getTestFileName(), 
					graphTestCase.getWorkDirectory() + "logWrites.txt");
			writer.newLine();
			writer.write("WRITE RESULTS (expressed in nanoseconds)");
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
	
	
	public static void main(String[] args) throws Exception {
		
		
		
		
		
		if (args[0].equals("START_TITAN")) {
			
			boolean batchMode = (args[1].equals("LOAD"));
			
			TestTools.openTitanGraph(batchMode, args[2], true, null);
			
			while (true);
			
		} else if (args[0].equals("LOAD_DATA")) {
			GraphTestCase graphTestCase = new GraphTestCase(args[1]);
			
			/*
			File directory = new File(graphTestCase.getStorageDirectory());
			if (!removeDirectory(directory))
				throw new RuntimeException("The directory " + graphTestCase.getStorageDirectory() + " was not completely deleted");
			*/
			//TestTools.loadGraph(null, graphTestCase.getGraphFileName(), graphTestCase.getLoadFileType(), 
				//	graphTestCase.getCassandraConfFile());
			while(true);
		} else if (args[0].equals("TEST_CASE")){
			GraphTestCase graphTestCase = new GraphTestCase(args[1]);
			TestManager testManager = new TestManager(graphTestCase);
			
			System.out.println("Starting tests...");
			testManager.execute(null);
			System.out.println("The tests were finished");
		}
		
		
		
		
	
	}

}
