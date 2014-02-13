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
