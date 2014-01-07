package com.imgraph.tests.titan;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class Traversal {

	public static enum SearchMethod {
		BFS,
		DFS
	}
	
	private Set<Long> explored;
	private Set<Long> frontierHash;
	
	private boolean stop;
	private TraversalResults traversalResults;
	private LinkedList<VertexPath> frontier;
	
	private int hops;
	private Direction direction;
	private Long searchedId;
	private SearchMethod searchMethod;
	
	public Traversal() {
		frontier = new LinkedList<VertexPath>();
		frontierHash = new HashSet<Long>(); 
		explored = new HashSet<Long>();
	}
	
	public int getHops() {
		return hops;
	}

	public void setHops(int hops) {
		this.hops = hops;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Long getSearchedId() {
		return searchedId;
	}

	public void setSearchedId(Long searchedId) {
		this.searchedId = searchedId;
	}

	private Direction switchDirection() {
		Direction oppositeDirection = null;
		switch(direction) {
		case BOTH:
			oppositeDirection = Direction.BOTH;
			break;
		case IN:
			oppositeDirection = Direction.OUT;
			break;
		case OUT:
			oppositeDirection = Direction.IN;
			break;
		}
		return oppositeDirection;
	}
	
	private void evalVertex(VertexPath vertexPath) {
		stop = false;

		if (vertexPath.getVertex().getProperty("name").equals(searchedId)) {
			traversalResults.addVertexPath(vertexPath);
			stop = true;
		}
		
	}
	
	
	private void exploreVertex(VertexPath vertexPath, Direction direction) {
		int edgeDepth = vertexPath.getDepth()+1;
		for (Edge edge : vertexPath.getVertex().getEdges(direction)) {
			
			Vertex childVertex = edge.getVertex(direction.opposite());
			
			if (!explored.contains(childVertex.getProperty("name")) && 
					!frontierHash.contains(childVertex.getProperty("name"))) {
				VertexPath childVertexPath = new VertexPath(childVertex, 
						edge, vertexPath, edgeDepth);
				evalVertex(childVertexPath);
				if (stop) {
					traversalResults.setTime((new Date().getTime() - traversalResults.getTime()));
					return;
				}
				
				switch (searchMethod) {
				case BFS:
					frontier.add(childVertexPath);
					break;
				case DFS:
					frontier.push(childVertexPath);
					break;
				}
				
				frontierHash.add((Long)childVertexPath.getVertex().getProperty("name"));
			}
		}
	}
	
	public TraversalResults search(Vertex startVertex) throws Exception {
		VertexPath vertexPath = null;
		/*
		frontier = new LinkedList<VertexPath>();
		frontierHash = new HashSet<Long>(); 
		explored = new HashSet<Long>();
		*/
		frontier.clear();
		frontierHash.clear();
		explored.clear();
		stop = false;
		traversalResults = new TraversalResults(direction);
		
		traversalResults.setTime(new Date().getTime());
		
		vertexPath = new VertexPath(startVertex);
		
		evalVertex(vertexPath);
		if (stop){
			traversalResults.setTime((new Date().getTime() - traversalResults.getTime()));
			return traversalResults;
		}

		frontier.add(vertexPath);
		frontierHash.add((Long) vertexPath.getVertex().getProperty("name"));

		while (true) {

			if (frontier.isEmpty()) {
				traversalResults.setTime((new Date().getTime() - traversalResults.getTime()));
				return traversalResults;
			}

			vertexPath = frontier.poll();
			
			frontierHash.remove(vertexPath.getVertex().getProperty("name"));
			explored.add((Long) vertexPath.getVertex().getProperty("name"));
			
			int edgeDepth = vertexPath.getDepth()+1;

			if (edgeDepth <= hops) {
				
				if (direction.equals(Direction.BOTH)) {
					exploreVertex(vertexPath, Direction.OUT);
					if (stop)
						return traversalResults;
					exploreVertex(vertexPath, Direction.IN);
					if (stop)
						return traversalResults;
					
				} else {
					exploreVertex(vertexPath, direction);
					if (stop)
						return traversalResults;
					/*
					for (Edge edge : vertexPath.getVertex().getEdges(direction)) {
						
						Vertex childVertex = edge.getVertex(switchDirection());
						
						if (!explored.contains(childVertex.getProperty("name")) && 
								!frontierHash.contains(childVertex.getProperty("name"))) {
							VertexPath childVertexPath = new VertexPath(childVertex, 
									edge, vertexPath, edgeDepth);
							evalVertex(childVertexPath);
							if (stop) {
								traversalResults.setTime((new Date().getTime() - traversalResults.getTime()));
								return traversalResults;
							}
							
							frontier.add(childVertexPath);
							frontierHash.add((Long)childVertexPath.getVertex().getProperty("name"));
						}
					}
					*/
				}
				
			}

		}
		
	}
	
	public static void main(String [] args) {
		LinkedList<Integer> list =  new LinkedList<Integer>();
		
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		
		while (!list.isEmpty()) 
			System.out.println(list.poll());
		
		
		list.push(1);
		list.push(2);
		list.push(3);
		list.push(4);
		
		while (!list.isEmpty()) 
			System.out.println(list.poll());
		
		
	}

	public SearchMethod getSearchMethod() {
		return searchMethod;
	}

	public void setSearchMethod(SearchMethod searchMethod) {
		this.searchMethod = searchMethod;
	}
	
}
