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
package com.imgraph.testing;

import java.util.List;
import java.util.Random;

import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;
import com.imgraph.traversal.DistributedTraversal;
import com.imgraph.traversal.EdgeTraversalConf;
import com.imgraph.traversal.Evaluation;
import com.imgraph.traversal.MatchEvaluatorConf;
import com.imgraph.traversal.Method;



/**
 * @author Aldemar Reynaga
 * Traversal client used to perform parallel traversals 
 */
public class TraversalClient implements Runnable {


	
	private DistributedTraversal traversal;
	private ImgGraph graph;
	private int counter;
	private int hops;
	private List<NodePair> nodePairs;
	private boolean running;

	
	public TraversalClient(int hops, EdgeTraversalConf edgeTraversalConf,
			List<NodePair> nodePairs,
			ImgGraph graph) {
		this.nodePairs = nodePairs;
		this.graph = graph;

		this.hops = hops;
		running =false;
		traversal = new DistributedTraversal();
		traversal.addEdgeTraversalConfs(edgeTraversalConf);
		traversal.setHops(hops);

		traversal.setMethod(Method.BREADTH_FIRST);
	}

	public void stop() {
		this.running = false;
	}
	
	public int getCounter() {
		return counter;
	}
	
	@Override
	public void run() {
		running =true;
		counter = 0;
		Random random = new Random(); 
		try {
			while (running) {
				
				NodePair nodePair = nodePairs.get(random.nextInt(nodePairs.size()));
				
				
				
				MatchEvaluatorConf matchConf =  new MatchEvaluatorConf();
				matchConf.setEvaluation(Evaluation.INCLUDE_AND_STOP);
				matchConf.setCellId(nodePair.getNodeBId());
				traversal.setMatchEvaluatorConf(matchConf);
				
				traversal.traverse((ImgVertex) graph.retrieveCell(nodePair.getNodeAId()));
				counter++;
				
			}
			traversal.close();
			
		} catch (Exception x) {
			x.printStackTrace();
		}
		
	}
	
	
}
