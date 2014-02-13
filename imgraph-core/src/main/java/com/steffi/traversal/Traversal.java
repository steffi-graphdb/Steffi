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

import java.util.Collection;

import com.steffi.model.SteffiVertex;

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
	public TraversalResults traverse(SteffiVertex startVertex);
}
