package com.imgraph.traversal;

import java.util.Collection;

import com.imgraph.model.ImgVertex;

/**
 * @author Aldemar Reynaga
 * Traversal interface
 */
public interface Traversal {
	public Collection<Evaluator> getEvaluators();
	public Collection<EdgeTraversalConf> getEdgeTraversalConfs();
	public int getHops();
	public void addEvaluators(Evaluator ...evaluators);
	public void setMethod(Method method);
	public void addEdgeTraversalConfs(EdgeTraversalConf ...edgeTraversalConfs);
	public void setHops(int hops);
	public TraversalResults traverse(ImgVertex startVertex);
}
