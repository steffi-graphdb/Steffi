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

import java.util.Iterator;

import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints Vertex  specification
 */
public class SteffiGraphDBVertex extends SteffiGraphDBElement implements Vertex  {

	public SteffiGraphDBVertex(SteffiGraphDBGraph graph, SteffiVertex vertex ) {
		super(graph);
		this.cell = vertex;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterable<Edge> getEdges(Direction direction, String... labels) {
		return new ImgraphVertexEdgeIterable(this.graph, (SteffiVertex)this.cell, direction, labels);
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterable<Vertex> getVertices(Direction direction, String... labels) {
		return new ImgraphVertexVertexIterable(this.graph, (SteffiVertex)this.cell, direction, labels);
	}

	@Override
	public VertexQuery query() {
		return new DefaultVertexQuery(this);
	}
	
	
	private class ImgraphVertexVertexIterable<T extends Vertex> implements Iterable<SteffiGraphDBVertex> {
        private final SteffiGraphDBGraph graph;
        private final ImgraphVertexEdgeIterable<Edge> edgeIterable;

        public ImgraphVertexVertexIterable(final SteffiGraphDBGraph graph, final SteffiVertex vertex, final Direction direction, final String... labels) {
            this.graph = graph;
            edgeIterable = new ImgraphVertexEdgeIterable<Edge>(graph, vertex, direction, labels);
        }
        
        

        public Iterator<SteffiGraphDBVertex> iterator() {
            final Iterator<SteffiEdge> itty;
            itty = edgeIterable.getRawEdgeIterator();

            return new Iterator<SteffiGraphDBVertex>() {
                public SteffiGraphDBVertex next() {
                    return new SteffiGraphDBVertex(graph, 
                    		 (SteffiVertex) graph.getRawGraph().retrieveCell(itty.next().getDestCellId()));
                }

                public boolean hasNext() {
                    return itty.hasNext();
                }

                public void remove() {
                    itty.remove();
                }
            };
        }
    }
	
	
	
	private class ImgraphVertexEdgeIterable<T extends Edge> implements Iterable<SteffiGraphDBEdge> {

        private final SteffiVertex vertex;
        private final String[] labels;
        private final EdgeType edgeType;
        private final SteffiGraphDBGraph graph;
        
        public ImgraphVertexEdgeIterable(final SteffiGraphDBGraph graph, 
        		final SteffiVertex vertex, final Direction direction, final String... labels) {
            this.graph = graph;
        	this.vertex = vertex;
            this.labels = labels;
            
            switch(direction) {
			case IN:
				edgeType = EdgeType.IN;
				break;
			case OUT:
				edgeType = EdgeType.OUT;
				break;
			default:
				edgeType = EdgeType.UNDIRECTED;
				break;
			
            }
            
        }
        
        public Iterator<SteffiEdge> getRawEdgeIterator() {
        	return vertex.getEdges(edgeType, labels).iterator();
        }
        

        public Iterator<SteffiGraphDBEdge> iterator() {
            final Iterator<SteffiEdge> itty;
            
            itty = vertex.getEdges(edgeType, labels).iterator();

            return new Iterator<SteffiGraphDBEdge>() {
                public SteffiGraphDBEdge next() {
                    return new SteffiGraphDBEdge(itty.next(), graph);
                }

                public boolean hasNext() {
                    return itty.hasNext();
                }

                public void remove() {
                    itty.remove();
                }
            };
        }
        
    }

	@Override
	public String toString() {
		return cell.toString();
	}
	
	public boolean equals(final Object object) {
        return object instanceof SteffiGraphDBVertex && ((SteffiGraphDBVertex) object).getId().equals(this.getId());
    }
	
	public SteffiVertex getRawVertex() {
		return (SteffiVertex) cell;
	}

	
	@Override
	public void remove() {
		((SteffiVertex)this.cell).remove();
		
	}

	@Override
	public Edge addEdge(String label, Vertex vertex) {
		return graph.addEdge(null, this, vertex, label);
		
		
	}
	

}
