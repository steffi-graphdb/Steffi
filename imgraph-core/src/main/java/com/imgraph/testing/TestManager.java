package com.imgraph.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

import com.imgraph.common.ImgLogger;
import com.imgraph.loader.TextFileLoader;
import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;
import com.imgraph.networking.ClusterConfigManager;
import com.imgraph.traversal.EdgeTraversalConf;
import com.imgraph.traversal.Evaluation;
import com.imgraph.traversal.MatchEvaluator;
import com.imgraph.traversal.MatchEvaluatorConf;
import com.imgraph.traversal.Method;
import com.imgraph.traversal.SimpleTraversal;
import com.imgraph.traversal.TraversalResults;

/**
 * @author Aldemar Reynaga
 * Manages and execute the test case read from a graph test case file
 */
public class TestManager {

	public enum GraphEngine {
		IMGRAPH,
		NEO4J,
		TITAN
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
	
	
	private void warmUpTraversal(EdgeType edgeType) throws Exception {
		SimpleTraversal traversal = new SimpleTraversal();
		TraversalResults results = null;
		traversal.setHops(4);
		MatchEvaluatorConf matchConf =  new MatchEvaluatorConf();
		matchConf.setCellId(4354L);
		matchConf.setEvaluation(Evaluation.INCLUDE_AND_STOP);
		traversal.addEvaluators(new MatchEvaluator(matchConf));
		
		EdgeTraversalConf traversalConf =  new EdgeTraversalConf("", edgeType);
		traversal.addEdgeTraversalConfs(traversalConf);
		
		traversal.setMethod(Method.BREADTH_FIRST);
		
		results = traversal.traverse((ImgVertex) ImgGraph.getInstance().retrieveRawCell(1));
		
		matchConf.setCellId(8787L);
		results = traversal.traverse((ImgVertex) ImgGraph.getInstance().retrieveRawCell(655));
		
		matchConf.setCellId(11112L);
		results = traversal.traverse((ImgVertex) ImgGraph.getInstance().retrieveRawCell(9652));
		
	}
	
	public void execute() throws IOException {
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(new File(
					graphTestCase.getWorkDirectory() + "results.txt"), false));
			writer.write("RESULTS FOR FILE: " + graphTestCase.getGraphFileName());
			writer.newLine();
			Map<StatisticalIndicators, Double> statistics;
			//Config cluster
			ClusterConfigManager configManager = new ClusterConfigManager();
			configManager.initialize();
			configManager.closeClientThreads();
			
			//Load file
			TextFileLoader loader = new TextFileLoader();
			String loadResults [] = loader.load(graphTestCase.getGraphFileName(), graphTestCase.getLoadFileType(), 
					graphTestCase.isDirected());
			writer.write("Loading results: number of vertices: " + loadResults[0] + ", number of edges: " + loadResults[1]);
			writer.newLine();
			
			
			Thread.sleep(5000);
			
			
			EdgeType edgeTypeTraversal = graphTestCase.isDirected()?EdgeType.OUT:EdgeType.UNDIRECTED;

			
			//Traverse test
			
			writer.newLine();
			writer.write("TRAVERSAL RESULTS (expressed in milliseconds)");
			writer.newLine();
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that can have or not a path");
			writer.newLine();
			writer.write("Max hops: 3 - Edge type: " + (graphTestCase.isDirected()?EdgeType.OUT:EdgeType.UNDIRECTED) 
					+ " - Delay: " + graphTestCase.getDelay());
			writer.newLine();
			EdgeTraversalConf traversalConf =  new EdgeTraversalConf("", edgeTypeTraversal);
			statistics = TestTools.testTraversalFromFile(ImgGraph.getInstance(), 
					traversalConf, Evaluation.INCLUDE_AND_STOP, graphTestCase.getTestFileName(), 
					3, graphTestCase.getWorkDirectory() + "logTraversal-3.txt", 
					graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			Thread.sleep(2000);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that can have or not a path");
			writer.newLine();
			writer.write("Max hops: 4 - Edge type: " + (graphTestCase.isDirected()?EdgeType.OUT:EdgeType.UNDIRECTED));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(ImgGraph.getInstance(), 
					traversalConf, Evaluation.INCLUDE_AND_STOP, graphTestCase.getTestFileName(), 
					4, graphTestCase.getWorkDirectory() + "logTraversal-4.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			Thread.sleep(2000);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that have a path");
			writer.newLine();
			writer.write("Max hops: 3 - Edge type: " + (graphTestCase.isDirected()?EdgeType.OUT:EdgeType.UNDIRECTED));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(ImgGraph.getInstance(), 
					traversalConf, Evaluation.INCLUDE_AND_STOP, graphTestCase.getTest3HFileName(), 
					3, graphTestCase.getWorkDirectory() + "logTraversal_3H.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			Thread.sleep(2000);
			
			writer.newLine();
			writer.write("Test file containing 200 random queries that have a path");
			writer.newLine();
			writer.write("Max hops: 4 - Edge type: " + (graphTestCase.isDirected()?EdgeType.OUT:EdgeType.UNDIRECTED));
			writer.newLine();
			statistics = TestTools.testTraversalFromFile(ImgGraph.getInstance(), 
					traversalConf, Evaluation.INCLUDE_AND_STOP, graphTestCase.getTest4HFileName(), 
					4, graphTestCase.getWorkDirectory() + "logTraversal_4H.txt", graphTestCase.getDelay());
			writeStatistics(writer, statistics);
			Thread.sleep(2000);
			
			
			//Read & write tests
			System.out.println("Processing read tests");
			statistics = TestTools.testReads(graphTestCase.getTestFileName(), 
					graphTestCase.getWorkDirectory() + "logReads.txt");
			writer.newLine();
			writer.write("READ RESULTS (400 reads) (expressed in nanoseconds)");
			writer.newLine();
			writeStatistics(writer, statistics);
			Thread.sleep(5000);
			
			System.out.println("Processing write tests");
			statistics = TestTools.testWrites(graphTestCase.getTestFileName(), 
					graphTestCase.getWorkDirectory() + "logWrites.txt");
			writer.newLine();
			writer.write("WRITE RESULTS (200 writes) (expressed in nanoseconds)");
			writer.newLine();
			writeStatistics(writer, statistics);
			
			writer.flush();
			
			
		} catch (Exception e) {
			ImgLogger.logError(e, "Error loading adjacent list format file (parallel");
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}
	
	

}
