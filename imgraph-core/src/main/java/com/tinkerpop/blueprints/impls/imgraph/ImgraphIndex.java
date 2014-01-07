package com.tinkerpop.blueprints.impls.imgraph;

import com.imgraph.index.ImgIndex;
import com.imgraph.index.ImgIndexHits;
import com.imgraph.model.Cell;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Index;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints ImgraphIndex
 * @param <T> The Imgraph's implementation class of the Blueprints specification to be indexed 
 * @param <S> The Imgraph's data model class representing the type of graph data to be indexed
 */
public class ImgraphIndex <T extends ImgraphElement, S extends Cell> implements Index<T>  {

	private ImgIndex<S> rawIndex;
	private Class<T> indexClass;
	
	public ImgraphIndex(ImgIndex<S> rawIndex, Class<T> indexClass) {
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
		
		if (indexClass.isAssignableFrom(ImgraphVertex.class))
			return new ImgraphVertexIterable(rawIndexHits);
		else
			return new ImgraphEdgeIterable(rawIndexHits);
		
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
