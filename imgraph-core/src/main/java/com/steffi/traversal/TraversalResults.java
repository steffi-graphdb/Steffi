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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;




/**
 * @author Aldemar Reynaga
 * Interface for traversal results
 */
public interface TraversalResults extends Serializable {
	public List<Path> getPaths();
	
	public void addVertexPath(ReducedVertexPath vertexPath);
	
	public void addManyVertexPaths(Collection<ReducedVertexPath> vertexPaths);
	
	public void clear();
	
	public long getTime();
	
	public void setTime(long time);
	
	public Collection<ReducedVertexPath> getVertexPaths();
	

}
