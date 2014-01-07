package com.tinkerpop.blueprints.impls.imgraph;

import java.util.Iterator;

import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;

/**
 * @author Aldemar Reynaga
 * Implementation of the Blueprints Vertex  specification
 */
public class ImgraphVertex extends ImgraphElement implements Vertex  {

	public ImgraphVertex(ImgraphGraph graph, ImgVertex vertex ) {
		super(graph);
		this.cell = vertex;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterable<Edge> getEdges(Direction direction, String... labels) {
		return new ImgraphVertexEdgeIterable(this.graph, (ImgVertex)this.cell, direction, labels);
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterable<Vertex> getVertices(Direction direction, String... labels) {
		return new ImgraphVertexVertexIterable(this.graph, (ImgVertex)this.cell, direction, labels);
	}

	@Override
	public VertexQuery query() {
		return new DefaultVertexQuery(this);
	}
	
	
	private class ImgraphVertexVertexIterable<T extends Vertex> implements Iterable<ImgraphVertex> {
        private final ImgraphGraph graph;
        private final ImgraphVertexEdgeIterable<Edge> edgeIterable;

        public ImgraphVertexVertexIterable(final ImgraphGraph graph, final ImgVertex vertex, final Direction direction, final String... labels) {
            this.graph = graph;
            edgeIterable = new ImgraphVertexEdgeIterable<Edge>(graph, vertex, direction, labels);
        }
        
        

        public Iterator<ImgraphVertex> iterator() {
            final Iterator<ImgEdge> itty;
            itty = edgeIterable.getRawEdgeIterator();

            return new Iterator<ImgraphVertex>() {
                public ImgraphVertex next() {
                    return new ImgraphVertex(graph, 
                    		 (ImgVertex) graph.getRawGraph().retrieveCell(itty.next().getDestCellId()));
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
	
	
	
	private class ImgraphVertexEdgeIterable<T extends Edge> implements Iterable<ImgraphEdge> {

        private final ImgVertex vertex;
        private final String[] labels;
        private final EdgeType edgeType;
        private final ImgraphGraph graph;
        
        public ImgraphVertexEdgeIterable(final ImgraphGraph graph, 
        		final ImgVertex vertex, final Direction direction, final String... labels) {
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
        
        public Iterator<ImgEdge> getRawEdgeIterator() {
        	return vertex.getEdges(edgeType, labels).iterator();
        }
        

        public Iterator<ImgraphEdge> iterator() {
            final Iterator<ImgEdge> itty;
            
            itty = vertex.getEdges(edgeType, labels).iterator();

            return new Iterator<ImgraphEdge>() {
                public ImgraphEdge next() {
                    return new ImgraphEdge(itty.next(), graph);
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
        return object instanceof ImgraphVertex && ((ImgraphVertex) object).getId().equals(this.getId());
    }
	
	public ImgVertex getRawVertex() {
		return (ImgVertex) cell;
	}

	
	@Override
	public void remove() {
		((ImgVertex)this.cell).remove();
		
	}

	@Override
	public Edge addEdge(String label, Vertex vertex) {
		return graph.addEdge(null, this, vertex, label);
		
		
	}
	

}
