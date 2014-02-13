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
package com.tinkerpop.blueprints.impls.imgraph;

import java.util.Iterator;

import com.imgraph.index.ImgIndexHits;
import com.imgraph.model.ImgVertex;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints interface used for indexes results on vertices
 * @param <T> An implementation of the Vertex interface
 */
public class ImgraphVertexIterable <T extends Vertex> implements CloseableIterable<ImgraphVertex> {

	private ImgIndexHits<ImgVertex> rawIndexHits;
	
	public ImgraphVertexIterable  (ImgIndexHits<ImgVertex> rawIndexHits) {
		this.rawIndexHits = rawIndexHits;
	}
	
	@Override
	public Iterator<ImgraphVertex> iterator() {
		return new Iterator<ImgraphVertex>() {
			
			@Override
			public void remove() {
				rawIndexHits.remove();
			}
			
			@Override
			public ImgraphVertex next() {
				return new ImgraphVertex(ImgraphGraph.getInstance(), rawIndexHits.next());
			}
			
			@Override
			public boolean hasNext() {
				return rawIndexHits.hasNext();
			}
		};
	}

	@Override
	public void close() {
		rawIndexHits.close();
	}

}
