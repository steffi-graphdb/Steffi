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
package com.steffi.traversal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiVertex;

/**
 * @author Aldemar Reynaga
 * Basic implementation of a traversal that uses the basic retrieval API to explore the graph
 */
public class SimpleTraversal implements Traversal {

	protected List<Evaluator> evaluators;
	protected List<EdgeTraversalConf> edgeTraversalConfs;
	protected Method method;
	protected int hops;


	public SimpleTraversal() {
		evaluators = new ArrayList<Evaluator>();
		edgeTraversalConfs = new ArrayList<EdgeTraversalConf>();
	}


	@Override
	public void addEvaluators(Evaluator... evaluators) {
		this.evaluators.addAll(Arrays.asList(evaluators));
	}

	@Override
	public void setMethod(Method method) {
		this.method = method;
	}

	@Override
	public void addEdgeTraversalConfs(EdgeTraversalConf... edgeTraversalConfs) {
		this.edgeTraversalConfs.addAll(Arrays.asList(edgeTraversalConfs));
	}

	@Override
	public void setHops(int hops) {
		this.hops = hops;
	}

	@Override
	public TraversalResults traverse(SteffiVertex startVertex) {

		try {
			return  graphSearch(startVertex); 
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		
		

	}


	private boolean isConfiguredEdgeType(SteffiEdge edge) {

		if (edgeTraversalConfs.isEmpty())
			return true;

		for (EdgeTraversalConf traversalConf : edgeTraversalConfs) {
			if (traversalConf.getEdgeType().equals(edge.getEdgeType())) {
				if (traversalConf.getName() == null) {
					if (edge.getName() == null)
						return true;
				} else if (traversalConf.getName().equals(edge.getName()))
					return true;
			}
		}
		return false;
	}

	
	boolean evalVertex(TraversalResults results, ReducedVertexPath vertexPath) {
		boolean stop = false;
		for (Evaluator evaluator : evaluators) {
			switch (evaluator.evaluate(vertexPath)) {
			case EXCLUDE_AND_CONTINUE:
				break;
			case EXCLUDE_AND_STOP:
				stop = true;
				break;
			case INCLUDE_AND_CONTINUE:
				results.addVertexPath(vertexPath);
				break;
			case INCLUDE_AND_STOP:
				results.addVertexPath(vertexPath);
				stop = true;
				break;
			}
		}
		return stop;
	}

	boolean isNotExploredEdge(SteffiEdge edge, Set<Long> explored, 
			Collection<ReducedVertexPath> frontier) {

		if (explored.contains(edge.getDestCellId())) 
			return false;


		for (ReducedVertexPath vertexPath : frontier) 
			if (vertexPath.getVertexId() == edge.getDestCellId()) 
				return false;


		return true;

	}


	private TraversalResults graphSearch(SteffiVertex startVertex) throws IOException {
		ReducedVertexPath vertexPath = null, childVertexPath = null;
		LinkedList<ReducedVertexPath> frontier = new LinkedList<ReducedVertexPath>();
		Set<Long> explored = new HashSet<Long>();
		TraversalResults traversalResults = new TraversalResultsImpl();
		
		traversalResults.setTime(new Date().getTime());

		
		vertexPath = new ReducedVertexPath(startVertex.getId());
		vertexPath.setVertex(startVertex);
		
		if (evalVertex(traversalResults, vertexPath))
			return traversalResults;

		frontier.add(vertexPath);

		while (true) {

			if (frontier.isEmpty()) {
				traversalResults.setTime((new Date().getTime() - traversalResults.getTime()));
				return traversalResults;
			}

			vertexPath = frontier.pop();


			explored.add(vertexPath.getVertex().getId());

			if ((vertexPath.getDepth()+1) <= hops) {
				for (SteffiEdge edge : vertexPath.getVertex().getEdges()) {
					if (isConfiguredEdgeType(edge)) {
	
						childVertexPath = new ReducedVertexPath(vertexPath, 
								edge.getEdgeType(), edge.getName(), edge.getDestCellId(), vertexPath.getDepth()+1);
						
						
						
						if (childVertexPath.getVertex() == null) {
							System.out.println("Severe error, vertex not found, id: " + edge.getDestCellId());
							return traversalResults;
						}

						if (evalVertex(traversalResults, childVertexPath)) {
							traversalResults.setTime((new Date().getTime() - traversalResults.getTime()));
							return traversalResults;
						}
						if (isNotExploredEdge(edge, explored, frontier))
							frontier.push(childVertexPath);


					}
				}
			}


			

		}

	}


	@Override
	public Collection<Evaluator> getEvaluators() {
		return this.evaluators;
	}


	@Override
	public Collection<EdgeTraversalConf> getEdgeTraversalConfs() {
		return this.edgeTraversalConfs;
	}


	@Override
	public int getHops() {
		return this.hops;
	}





}
