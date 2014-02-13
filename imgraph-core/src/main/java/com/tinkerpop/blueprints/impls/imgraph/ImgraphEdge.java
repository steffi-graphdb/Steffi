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

import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints Edge specification
 */
public class ImgraphEdge extends ImgraphElement implements Edge {

	private ImgraphGraph graph;
	
	public ImgraphEdge(ImgEdge edge, ImgraphGraph graph) {
		super(graph);
		this.graph = graph;
		this.cell = edge; 
	}
	
	public ImgEdge getRawEdge() {
		return (ImgEdge) cell;
	}
	
	
	@Override
	public Object getId() {
		return cell.getId();
	}
	@Override
	public String getLabel() {
		return cell.getName();
	}

	@Override
	public Vertex getVertex(Direction direction) throws IllegalArgumentException {
		Vertex vertex = null;
		switch (direction) {
		case BOTH:
			throw new RuntimeException("BOTH is not supported");
		case IN:
			vertex =  new ImgraphVertex(graph, (ImgVertex) graph.getRawGraph().retrieveCell(((ImgEdge)cell).getDestCellId()));
			break;
		case OUT:
			vertex =  new ImgraphVertex(graph, (ImgVertex) graph.getRawGraph().retrieveCell(((ImgEdge)cell).getSourceCellId()));
			break;
		}
		return vertex;
	}

	@Override
	public String toString() {
		return cell.toString();
	}

	@Override
	public void remove() {
		ImgVertex sourceVertex =  (ImgVertex) graph.getRawGraph().retrieveCell(((ImgEdge)cell).getSourceCellId());
		sourceVertex.removeEdge((ImgEdge) this.cell);
	}
	
	

}
