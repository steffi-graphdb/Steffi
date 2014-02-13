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
package com.steffi.storage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import org.infinispan.Cache;

import com.steffi.common.BigTextFile;
import com.steffi.model.Cell;
import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiVertex;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.impls.steffi.SteffiGraphDBGraph;
import com.tinkerpop.blueprints.impls.steffi.SteffiGraphDBVertex;

/**
 * @author Aldemar Reynaga
 * Text file functions used for the loading of files
 */
public class FileUtilities {
	
	
	
	
	public static void readFromFile(SteffiGraphDBGraph graph, String fileName) throws Exception {
		BigTextFile file = null;
		StringTokenizer tokenizer = null;
		SteffiVertex vertex = null;
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
					vertex = ((SteffiGraphDBVertex)graph.addVertex(Long.parseLong(tokenizer.nextToken()))).getRawVertex();
					
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
				
				
				if (cell instanceof SteffiVertex) {
					line = cell.getId() + "\t";
					edgeIn = edgeOut = edgeUnd = "";
					edgeInCounter = edgeOutCounter = edgeUndCounter = 0;
					
					for (SteffiEdge edge : ((SteffiVertex) cell).getEdges()){
						
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
