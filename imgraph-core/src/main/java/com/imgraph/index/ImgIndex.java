package com.imgraph.index;

import com.imgraph.model.Cell;
import com.imgraph.storage.IndexOperation;

/**
 * @author Aldemar Reynaga
 * General interface for Imgraph indexes
 * @param <T> Class of the elements to be indexed, it can be ImgVertex or ImgEdge
 */
public interface ImgIndex<T extends Cell> {
	
	 String getName();
	 Class<T> getEntityType();
	 ImgIndexHits<T> get( String key, Object value );
	 void put(String key, Object value, T element  );
	 void remove( T element, String key, Object value );
	 boolean hasElementForKeyValue(T element, String key, Object value);
	 long count(String key, Object value);
	 void commitChanges(IndexOperation<T> operations);
}
