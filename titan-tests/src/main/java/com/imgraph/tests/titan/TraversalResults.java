package com.imgraph.tests.titan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;




public class TraversalResults {
	private List<VertexPath> vertexPaths;
	private Direction direction;
	private long time;
	
	public TraversalResults(Direction direction) {
		vertexPaths = new ArrayList<VertexPath>();
		this.direction = direction;
	}

	public List<Path> getPaths() {
		List<Path> paths = new ArrayList<Path>();
		for (VertexPath vertexPath : vertexPaths) {
			LinkedList<Element> elements  = new LinkedList<Element>();
			VertexPath curVertexPath = vertexPath;
			
			do {
				elements.addFirst(curVertexPath.getVertex());
				if (curVertexPath.getSourceEdge() != null)
					elements.addFirst(curVertexPath.getSourceEdge());
				curVertexPath = curVertexPath.getParentVertexPath();
			} while (curVertexPath != null);
			paths.add(new Path(elements, direction));
		}
		
		return paths;
	}
	
	public void addVertexPath(VertexPath vertexPath) {
		this.vertexPaths.add(vertexPath);
	}

	
	public void addManyVertexPaths(Collection<VertexPath> vertexPaths) {
		this.vertexPaths.addAll(vertexPaths);
	}

	
	public Collection<VertexPath> getVertexPaths() {
		return this.vertexPaths;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public static class Path {
		private final LinkedList<Element> elements;
		private final Direction direction;
		
		public Path(LinkedList<Element> elements, Direction direction) {
			this.elements = elements;
			this.direction = direction;
		}
		
		public String toString() {
			StringBuffer result =  new StringBuffer("");
			String directionString = "";
			switch (direction) {
			case BOTH:
				directionString = "--";
				break;
			case IN:
				directionString = "<-";
				break;
			case OUT:
				directionString = "->";
				break;
			}
			
			for (int i=0; i<elements.size(); i++) {
				
				if (i%2 == 0)
					result.append("[").append(elements.get(i).getProperty("name")).append("]");
				else
					result.append(directionString);
					
			}
			
			return result.toString();
		}
		
	}
	
	
}
