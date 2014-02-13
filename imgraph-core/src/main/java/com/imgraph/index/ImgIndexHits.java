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
package com.imgraph.index;

import java.util.Iterator;

/**
 * @author Aldemar Reynaga
 * General interface for the results of an index search 
 * @param <T> The elements to be returned by the search, it can be ImgVertex or ImgEdge
 */
public interface ImgIndexHits<T> extends Iterator<T>, Iterable<T> {
	
	int size();
	void close();
}
