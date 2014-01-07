package neo4j.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;


public class App 
{
	public static void main( String[] args )
	{
		String fileName = null;
		String directory = null;

		InputStreamReader istream;

		BufferedReader bufRead;
		


		istream = new InputStreamReader(System.in) ;

		bufRead = new BufferedReader(istream) ;

		try {
		
			System.out.print("Graph file name: ");
			fileName = bufRead.readLine();
			System.out.print("Output directory: ");
			directory = bufRead.readLine();
				
			BigTextFile file = null;
			
			BatchInserter inserter = BatchInserters.inserter(directory);
			RelationshipType rel = DynamicRelationshipType.withName( "REL" );
			
			Map<String, Long> nodes = new HashMap<String, Long>();
			
			
			file = new BigTextFile(fileName);
			String fromNodeId = null, toNodeId = null;
			Long fromNode, toNode;
			Map<String, Object> properties ;
			StringTokenizer tokenizer = null;
			long edgeCounter=0, vertexCounter=0; 
			
			for (String line : file) {
				if (!line.startsWith("#")) {
					tokenizer = new StringTokenizer(line);
					fromNodeId = tokenizer.nextToken();
					toNodeId = tokenizer.nextToken();
					
					fromNode = nodes.get(fromNodeId);
					
					if (fromNode == null) {
						properties = new HashMap<String, Object>();
						properties.put("name", fromNodeId );
						fromNode = inserter.createNode(properties);
						nodes.put(fromNodeId, fromNode);
						vertexCounter++;
					}
						
					toNode = nodes.get(toNodeId);
					if (toNode == null) {
						properties = new HashMap<String, Object>();
						properties.put("name", toNodeId );
						
						toNode = inserter.createNode(properties);
						nodes.put(toNodeId, toNode);
						
						vertexCounter++;
					}
					
					inserter.createRelationship( fromNode, toNode, rel, null );
					
					edgeCounter++;
				}
				 
			}
			System.out.println("File succesfully loaded: " + vertexCounter + 
					" vertices and " + edgeCounter + " edges");
		
			inserter.shutdown();
	
			/*
			GraphDatabaseService db = new EmbeddedGraphDatabase(
	                "target/batchinserter-example" );
	        Node mNode = db.getNodeById("7");
	        Node cNode = mNode.getSingleRelationship( knows, Direction.OUTGOING )
	                .getEndNode();
	        assertEquals( "Chris", cNode.getProperty( "name" ) );
	        db.shutdown();
			*/
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/*
	public static void emain( String[] args )
	{
		String fileName = null;
		String directory = null;

		InputStreamReader istream;

		BufferedReader bufRead;
		long lineCounter = 0;


		istream = new InputStreamReader(System.in) ;

		bufRead = new BufferedReader(istream) ;

		try {
		
			System.out.print("Graph file name: ");
			fileName = bufRead.readLine();
			System.out.print("Output directory: ");
			directory = bufRead.readLine();
				
			BigTextFile file = null;
			
			Neo4jGraph graph = new Neo4jGraph(directory);
			
			
			
			file = new BigTextFile(fileName);
			String fromNodeId = null, toNodeId = null;
			Vertex fromNode, toNode;
			
			StringTokenizer tokenizer = null;
			long edgeCounter=0, vertexCounter=0; 
			
			for (String line : file) {
				if (!line.startsWith("#")) {
					lineCounter++;
					tokenizer = new StringTokenizer(line);
					fromNodeId = tokenizer.nextToken();
					toNodeId = tokenizer.nextToken();
					
					fromNode = graph.getVertex(fromNodeId);
					
					
					if (fromNode == null) {
						fromNode = graph.addVertex("");
						fromNode.setProperty("name", fromNodeId);
						vertexCounter++;
						
					}
					
					
						
					toNode = graph.getVertex(toNodeId);
					if (toNode == null) {
						toNode = graph.addVertex(toNodeId);
						toNode.setProperty("name", toNodeId);
						vertexCounter++;
					}
					
					
					graph.addEdge(null, fromNode, toNode, "-");
					
					if (lineCounter % 1000 == 0)
						System.out.println(lineCounter + " lines have been written");
					
					edgeCounter++;
					graph.stopTransaction(Conclusion.SUCCESS);
				}
				 
			}
			System.out.println("File succesfully loaded: " + vertexCounter + 
					" vertices and " + edgeCounter + " edges");
		
			graph.shutdown();
	
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	*/
	
	
}
