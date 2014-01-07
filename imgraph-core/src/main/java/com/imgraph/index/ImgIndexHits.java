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
