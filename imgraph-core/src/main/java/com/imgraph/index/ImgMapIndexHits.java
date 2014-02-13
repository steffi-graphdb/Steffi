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

import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;



/**
 * @author Aldemar Reynaga
 * Results of the index searches on the ImgMapIndex implementation
 * @param <T> The class of the elements to be retrieved
 */
public class ImgMapIndexHits<T> implements ImgIndexHits<T> {
	
	//private final Collection<T> values;
	private final Iterator<?> indexIterator;
	private Class<T> elementClass;
	private final int size;
	
	public ImgMapIndexHits(Iterator<?> indexIterator, int size, Class<T> elementClass) {
		//this.values = values;
		this.indexIterator = indexIterator;
		this.elementClass = elementClass;
		this.size = size;
	}

	@Override
	public boolean hasNext() {
		return indexIterator.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		Object nextElementId = indexIterator.next();
		T element;
		
		
		try {
		
			if (elementClass.equals(ImgVertex.class)) {
				element = (T) ImgGraph.getInstance().retrieveCell((Long)nextElementId);
			} else {
				EdgeIndexEntry edgeIndexEntry = (EdgeIndexEntry) nextElementId;
				ImgVertex vertex = (ImgVertex) ImgGraph.getInstance().retrieveCell(edgeIndexEntry.getCellId());
				element = (T) vertex.getEdge(edgeIndexEntry.getEdgeId());
			}
		} catch (ClassCastException cce) {
			throw new RuntimeException("The index does not handle objects of type " + elementClass.getSimpleName() + 
					", please retrieve the index providing the correct class.");
		}
		
		return element;
	}

	@Override
	public void remove() {
		indexIterator.remove();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void close() {
		
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

}
