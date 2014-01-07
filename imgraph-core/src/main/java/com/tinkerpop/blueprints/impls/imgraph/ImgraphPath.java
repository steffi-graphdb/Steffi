package com.tinkerpop.blueprints.impls.imgraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;
import com.imgraph.traversal.Path;

/**
 * @author Aldemar Reynaga
 * Blueprints implementation for the Path
 */
public class ImgraphPath  {

	private Path path;
	private ImgraphGraph graph;
	
	public ImgraphPath(ImgraphGraph graph, Path path) {
		this.path = path;
		this.graph = graph;
	}
	
	
	public Iterable<ImgraphVertex> getVertexes() {
		
		return new Iterable<ImgraphVertex>() {
			
			@Override
			public Iterator<ImgraphVertex> iterator() {
				List<ImgraphVertex> vertices = new LinkedList<ImgraphVertex>();
				
				
				for (ImgVertex imgVertex : path.getVertexes())
					vertices.add(new ImgraphVertex(graph, imgVertex));
				
				return vertices.iterator();
			}
		};
		
		
	}

	
	public Iterable<ImgraphEdge> getEdges() {
		
		return new Iterable<ImgraphEdge>() {
			
			@Override
			public Iterator<ImgraphEdge> iterator() {
				List<ImgraphEdge> edges = new LinkedList<ImgraphEdge>();
				for (ImgEdge imgEdge : path.getEdges())
					edges.add(new ImgraphEdge(imgEdge, graph));
				return edges.iterator();
			}
		};
	}

	
	public Iterable<Object> getPath() {
		
		return new Iterable<Object>() {
			
			@Override
			public Iterator<Object> iterator() {
				List<Object> pathElements = new LinkedList<Object>();
				
				for (Object element : path.getPath()) {
					if (element instanceof ImgVertex)
						pathElements.add(new ImgraphVertex(graph, (ImgVertex) element));
					else if (element instanceof ImgEdge)
						pathElements.add(new ImgraphEdge((ImgEdge) element, graph));
				}
				
				return pathElements.iterator();
			}
		};
		
		
		
		
	}


	@Override
	public String toString() {
		
		return path.toString();
	}
	
	

}
