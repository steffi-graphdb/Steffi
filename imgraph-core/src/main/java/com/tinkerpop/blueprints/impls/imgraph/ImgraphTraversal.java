package com.tinkerpop.blueprints.impls.imgraph;

import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgVertex;
import com.imgraph.traversal.DistributedTraversal;
import com.imgraph.traversal.EdgeTraversalConf;
import com.imgraph.traversal.Evaluation;
import com.imgraph.traversal.MatchEvaluator;
import com.imgraph.traversal.MatchEvaluatorConf;
import com.imgraph.traversal.Method;
import com.imgraph.traversal.SimpleTraversal;
import com.imgraph.traversal.TraversalResults;


/**
 * @author Aldemar Reynaga
 * Wrapper for the distributed traversal engine
 */
public class ImgraphTraversal {
	
	public static ImgraphTraversalResults simpleTraversal(String startVertexId, String edgeType, 
			String searchedVertexId, String namePattern, int hops) {
		ImgraphGraph graph = ImgraphGraph.getInstance();
		
		SimpleTraversal traversal = new SimpleTraversal();
		traversal.setHops(hops);
		MatchEvaluatorConf matchConf =  new MatchEvaluatorConf();
		matchConf.setNamePattern(namePattern);
		matchConf.setCellId(Long.parseLong(searchedVertexId));
		matchConf.setEvaluation(Evaluation.INCLUDE_AND_STOP);
		traversal.addEvaluators(new MatchEvaluator(matchConf));
		
		EdgeTraversalConf traversalConf =  new EdgeTraversalConf("", EdgeType.valueOf(edgeType));
		traversal.addEdgeTraversalConfs(traversalConf);
		
		traversal.setMethod(Method.BREADTH_FIRST);
		
		TraversalResults results = traversal.traverse((ImgVertex) graph.
				getRawGraph().retrieveCell(Long.parseLong(startVertexId)));
		return new ImgraphTraversalResults(graph, results);
	}
	
	public static ImgraphTraversalResults distributedTraversal(String startVertexId, String edgeType, 
			String searchedVertexId, String namePattern, int hops) {
		ImgraphGraph graph = ImgraphGraph.getInstance();
		DistributedTraversal traversal = new DistributedTraversal();
		
		traversal.setHops(hops);
		
		MatchEvaluatorConf matchConf =  new MatchEvaluatorConf();
		matchConf.setNamePattern(namePattern);
		matchConf.setCellId(Long.parseLong(searchedVertexId));
		matchConf.setEvaluation(Evaluation.INCLUDE_AND_STOP);
		traversal.setMatchEvaluatorConf(matchConf);

		EdgeTraversalConf traversalConf =  new EdgeTraversalConf("", EdgeType.valueOf(edgeType));
		traversal.addEdgeTraversalConfs(traversalConf);
		
		traversal.setMethod(Method.BREADTH_FIRST);
		
		
		TraversalResults results = traversal.traverse((ImgVertex) graph.getRawGraph().
				retrieveCell(Long.parseLong(startVertexId)));
		traversal.close();
		
		if (results == null)
			throw new RuntimeException("Error processing the traversal!");
		
		return new ImgraphTraversalResults(graph, results);
	}
}
