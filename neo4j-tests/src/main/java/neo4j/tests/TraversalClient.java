package neo4j.tests;

import java.util.List;
import java.util.Random;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;



public class TraversalClient implements Runnable {


	
	private int counter;
	private int hops;
	private List<NodePair> nodePairs;
	private boolean running;
	private GraphDatabaseService graph;
	private Direction direction;
	//7private EdgeTraversalConf edgeTraversalConf;
	
	public TraversalClient(int hops, Direction direction,
			List<NodePair> nodePairs,
			GraphDatabaseService graph) {
		this.nodePairs = nodePairs;
		this.graph = graph;
		this.hops = hops;
		this.running =false;
		this.direction = direction;
		
	}

	public void stop() {
		this.running = false;
	}
	
	public int getCounter() {
		return counter;
	}
	
	@Override
	public void run() {
		running =true;
		counter = 0;
		Random random = new Random(); 
		try {
			while (running) {
				
				NodePair nodePair = nodePairs.get(random.nextInt(nodePairs.size()));
				
				Traverser traverser = TestTools.traverse(graph, String.valueOf(nodePair.getNodeAId()), 
						String.valueOf(nodePair.getNodeBId()), hops, direction);
				
				for (Path path : traverser) {
					path.nodes();
					break;
				}
				
				counter++;
				
			}
			
			
		} catch (Exception x) {
			x.printStackTrace();
		}
		
	}
	
	
}
