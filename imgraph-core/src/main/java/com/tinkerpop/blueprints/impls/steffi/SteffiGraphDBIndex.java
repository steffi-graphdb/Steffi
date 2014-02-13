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

import com.steffi.index.ImgIndex;
import com.steffi.index.ImgIndexHits;
import com.steffi.model.Cell;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Index;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints ImgraphIndex
 * @param <T> The Imgraph's implementation class of the Blueprints specification to be indexed 
 * @param <S> The Imgraph's data model class representing the type of graph data to be indexed
 */
public class SteffiGraphDBIndex <T extends SteffiGraphDBElement, S extends Cell> implements Index<T>  {

	private ImgIndex<S> rawIndex;
	private Class<T> indexClass;
	
	public SteffiGraphDBIndex(ImgIndex<S> rawIndex, Class<T> indexClass) {
		this.rawIndex = rawIndex;
		this.indexClass = indexClass;
	}
	
	@Override
	public long count(String key, Object value) {
		final ImgIndexHits<S> indexHits = rawIndex.get(key, value); 
		
		return indexHits.size();
	}

	@Override
	public CloseableIterable<T> get(String key, Object value) {
		final ImgIndexHits<S> rawIndexHits = rawIndex.get(key, value);
		
		if (indexClass.isAssignableFrom(SteffiGraphDBVertex.class))
			return new SteffiGraphDBVertexIterable(rawIndexHits);
		else
			return new SteffiGraphDBEdgeIterable(rawIndexHits);
		
	}

	@Override
	public Class<T> getIndexClass() {
		return indexClass;
	}

	@Override
	public String getIndexName() {
		return rawIndex.getName();
	}

	
	private Cell getRawElement(T element) {
		Cell rawElement = null;
		if (!indexClass.equals(element.getClass()))
			throw new RuntimeException("The element must be of the class " + element.getClass().getSimpleName());

		rawElement = indexClass.cast(element).cell;
		return rawElement;
	}
	
	@Override
	public void put(String key, Object value, T element) {
		
		rawIndex.put(key, value, (S) getRawElement(element));
	}

	@Override
	public CloseableIterable<T> query(String key, Object query) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(String key, Object value, T element) {
		
		rawIndex.remove((S) getRawElement(element), key, value);
	}

}
