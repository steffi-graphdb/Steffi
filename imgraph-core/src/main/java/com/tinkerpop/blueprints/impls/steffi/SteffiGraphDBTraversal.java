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
package com.tinkerpop.blueprints.impls.steffi;

import com.steffi.model.EdgeType;
import com.steffi.model.SteffiVertex;
import com.steffi.traversal.DistributedTraversal;
import com.steffi.traversal.EdgeTraversalConf;
import com.steffi.traversal.Evaluation;
import com.steffi.traversal.MatchEvaluator;
import com.steffi.traversal.MatchEvaluatorConf;
import com.steffi.traversal.Method;
import com.steffi.traversal.SimpleTraversal;
import com.steffi.traversal.TraversalResults;


/**
 * @author Aldemar Reynaga
 * Wrapper for the distributed traversal engine
 */
public class SteffiGraphDBTraversal {
	
	public static SteffiGraphDBTraversalResults simpleTraversal(String startVertexId, String edgeType, 
			String searchedVertexId, String namePattern, int hops) {
		SteffiGraphDBGraph graph = SteffiGraphDBGraph.getInstance();
		
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
		
		TraversalResults results = traversal.traverse((SteffiVertex) graph.
				getRawGraph().retrieveCell(Long.parseLong(startVertexId)));
		return new SteffiGraphDBTraversalResults(graph, results);
	}
	
	public static SteffiGraphDBTraversalResults distributedTraversal(String startVertexId, String edgeType, 
			String searchedVertexId, String namePattern, int hops) {
		SteffiGraphDBGraph graph = SteffiGraphDBGraph.getInstance();
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
		
		
		TraversalResults results = traversal.traverse((SteffiVertex) graph.getRawGraph().
				retrieveCell(Long.parseLong(startVertexId)));
		traversal.close();
		
		if (results == null)
			throw new RuntimeException("Error processing the traversal!");
		
		return new SteffiGraphDBTraversalResults(graph, results);
	}
}
