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
package com.imgraph.traversal;

import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;

/**
 * @author Aldemar Reynaga
 * Interface for the Path results of taversals
 */
public interface Path {
	public Iterable<ImgVertex> getVertexes();
	public Iterable<ImgEdge> getEdges();
	public Iterable<Object> getPath();
	public String toString();
}
