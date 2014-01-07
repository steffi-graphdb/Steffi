package com.imgraph.storage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import org.infinispan.Cache;

import com.imgraph.common.BigTextFile;
import com.imgraph.model.Cell;
import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgVertex;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.impls.imgraph.ImgraphGraph;
import com.tinkerpop.blueprints.impls.imgraph.ImgraphVertex;

/**
 * @author Aldemar Reynaga
 * Text file functions used for the loading of files
 */
public class FileUtilities {
	
	
	
	
	public static void readFromFile(ImgraphGraph graph, String fileName) throws Exception {
		BigTextFile file = null;
		StringTokenizer tokenizer = null;
		ImgVertex vertex = null;
		int inEdgesCounter, outEdgesCounter;
		long counter=0;
		Date startDate, endDate;
		try {
			startDate = new Date();
			file = new BigTextFile(fileName);
			
			System.out.print("Loading\n[");
			for (String line : file) {
				if (!line.trim().equals("")) {
					tokenizer = new StringTokenizer(line);
					vertex = ((ImgraphVertex)graph.addVertex(Long.parseLong(tokenizer.nextToken()))).getRawVertex();
					
					inEdgesCounter = Integer.parseInt(tokenizer.nextToken());
					for (int i=0; i<inEdgesCounter; i++) 
						vertex.addPartialEdge(Long.parseLong(tokenizer.nextToken()), EdgeType.IN, "");
					
					outEdgesCounter = Integer.parseInt(tokenizer.nextToken());
					for (int i=0; i<outEdgesCounter; i++) 
						vertex.addPartialEdge(Long.parseLong(tokenizer.nextToken()), EdgeType.OUT, "");
					
					counter++;
					
					if (counter%500==0) {
						System.out.print(".");
						System.out.flush();
						graph.stopTransaction(Conclusion.SUCCESS);
					} else if ((counter % 50000) == 0) {
						System.out.print("]\nLoaded " + counter + " lines \n[" );
						System.out.flush();
					}
					
				}
			}
			graph.stopTransaction(Conclusion.SUCCESS);
			
			
			System.out.println("Starting to update edgeAddresses...");
			EdgeAddressesUpdater.updateEdgeAddresses();
			
			
			
			System.out.println("Starting to calculate local 1-Hop neighbors");
			//ImgpFileTools.updateLocal1HopNeighbors();
			
			
			endDate =  new Date();


			System.out.println("File succesfully loaded in " + (endDate.getTime() - startDate.getTime()) + 
					"ms. "+ counter + " vertices have been processed");
			
			
		} finally {
			if (file != null) file.Close();
		}
		
	}
	
	
	public static void writeToFile(String fileName) throws IOException {
		Cache<Long, Cell> cache = CacheContainer.getCellCache();
		String line = null, edgeIn = null, edgeOut = null, edgeUnd = null;
		int edgeInCounter, edgeOutCounter, edgeUndCounter;
		BufferedWriter bufWriter = null;
		
		try {
			bufWriter = new BufferedWriter(new FileWriter(fileName));
			
			for (Cell cell : cache.values()) {
				
				
				if (cell instanceof ImgVertex) {
					line = cell.getId() + "\t";
					edgeIn = edgeOut = edgeUnd = "";
					edgeInCounter = edgeOutCounter = edgeUndCounter = 0;
					
					for (ImgEdge edge : ((ImgVertex) cell).getEdges()){
						
						switch (edge.getEdgeType()) {
						case HYPEREDGE:
							break;
						case IN:
							edgeIn += ("\t" + edge.getDestCellId());
							edgeInCounter++;
							break;
						case OUT:
							edgeOut += ("\t" + edge.getDestCellId());
							edgeOutCounter++;
							break;
						case UNDIRECTED:
							edgeUnd += ("\t" + edge.getDestCellId());
							edgeUndCounter++;
							break;
						default:
							break;
						
						}
						
						
					}
					
					line += (edgeInCounter + edgeIn + "\t" + edgeOutCounter + edgeOut +
							"\t" + edgeUndCounter + edgeUnd);
					
					bufWriter.write(line);
					bufWriter.newLine();
				}
			}
		} finally {
			if (bufWriter!=null){try{bufWriter.close();}catch(IOException ioe){}}
		}
	}
}
