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
package com.steffi.testing;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.steffi.common.IOUtils;

/**
 * @author Aldemar Reynaga
 * Generates a text file representing a graph with only on path for some traversals  
 */
public class SinglePathGraphGenerator {
	
	private BufferedWriter graphFileWriter;
	
	private int subGraphSize;
	private int numberOfNodes;
	private Random randomGen;
	private int startNodeEdges;
	private int normalNodeEdges;
	private float numbOfEdgesVar;
	private int maxNumberOfQueries;
	private int depth;
	
	public SinglePathGraphGenerator(int numberOfNodes, int subGraphSize,   
			int maxNumberOfQueries, int depth, int startNodeEdges, 
			int normalNodeEdges, float numbOfEdgesVar) {
		this.numberOfNodes = numberOfNodes;
		this.subGraphSize = subGraphSize;
		this.maxNumberOfQueries = maxNumberOfQueries;
		this.depth = depth;
		this.startNodeEdges = startNodeEdges;
		this.normalNodeEdges = normalNodeEdges;
		this.randomGen = new Random();
		this.numbOfEdgesVar = numbOfEdgesVar;
		
	}
	
	private void generateNodeEdges(long nodeId, int maxEdges, int requiredEdges) throws IOException {
		TLongSet destNodeIds = new TLongHashSet();
		long destNodeId=0;
		for (int j=0; j<maxEdges && j<requiredEdges; j++) {
			do {
				destNodeId = nodeId + TestTools.nextLong(randomGen, maxEdges)+1;
			} while (destNodeIds.contains(destNodeId));
			
			destNodeIds.add(destNodeId);
			
			graphFileWriter.newLine();
			graphFileWriter.write(nodeId + "\t" + destNodeId);
		}
	}
	
	private void generateSubGraph(long startNodeId) throws IOException {
		int numberOfEdges;
		for (int i=0; i<(startNodeId+subGraphSize-2) && i<=numberOfNodes; i++) {
			
			if (i==0)
				numberOfEdges = startNodeEdges + randomGen.nextInt((int) numbOfEdgesVar);
			else
				numberOfEdges = normalNodeEdges + randomGen.nextInt((int) numbOfEdgesVar);
			
			generateNodeEdges(startNodeId+i, subGraphSize-i+1, numberOfEdges);
		}
		graphFileWriter.flush();
	}
	
	private void generateQueries(ArrayList<Long> startNodes, String queryFileName) throws IOException {
		
		List<NodePair> queries = new ArrayList<NodePair>();
		BufferedWriter queryFileWriter = null;
		for (int i= 0; (i+depth)<startNodes.size() && queries.size() < maxNumberOfQueries; i++) 
			queries.add(new NodePair(startNodes.get(i), startNodes.get(i+depth)));
		
		for (int j=0; j<(queries.size()-j) && queries.size() < maxNumberOfQueries; j++)
			queries.add(new NodePair(queries.get(j).getNodeBId(), queries.get(j).getNodeAId()));
		
		try {
			queryFileWriter =  new BufferedWriter(new FileWriter(new File(
					queryFileName), false));
			
			queryFileWriter.write("#START_NODE_ID\tSEARCHED_NODE_ID");
			
			for (NodePair nodePair : queries) {
				queryFileWriter.newLine();
				queryFileWriter.write(nodePair.getNodeAId() + "\t" + nodePair.getNodeBId());
			}
			queryFileWriter.flush();
			
		} finally {
			if (queryFileWriter != null) {try {queryFileWriter.close();}catch(IOException ioe){}}
		}
		
		
		
		
	}
	
	
	
	
	public void generate(String graphFileName, String queryFileName)  throws Exception {
		
		long prevStartNodeId=0;
		long curStartNodeId=1;
		ArrayList<Long> startNodes = new ArrayList<Long>();
		
		try {
			graphFileWriter = new BufferedWriter(new FileWriter(new File(
					graphFileName), false));
			
			graphFileWriter.write("#SOURCE_NODE\tDESTINATION_NODE");
			do {
				if (prevStartNodeId > 0) { //Generate link between subgraphs
					graphFileWriter.newLine();
					graphFileWriter.write(prevStartNodeId + "\t" + curStartNodeId);
				}
				startNodes.add(curStartNodeId);
				generateSubGraph(curStartNodeId);
				prevStartNodeId = curStartNodeId;
				curStartNodeId = prevStartNodeId + subGraphSize;
				System.out.println(curStartNodeId +  " nodes written");
				
			} while(curStartNodeId<=numberOfNodes);
			graphFileWriter.flush();
			generateQueries(startNodes, queryFileName);
		} finally {
			if (graphFileWriter!=null) {try{graphFileWriter.close();}catch(IOException ioe){}}
		}
		
	}
	
	public static void main(String [] args) {
		
		
		
		
		
		try {
			int numberOfNodes = Integer.parseInt(IOUtils.readLine("Number of nodes: "));
			int subGraphSize = Integer.parseInt(IOUtils.readLine("Sub graph size: "));
			int maxQueries = Integer.parseInt(IOUtils.readLine("Max number of queries: "));
			int maxDepthQuery = Integer.parseInt(IOUtils.readLine("Max depth for queries: "));
			int startNodeEdges = Integer.parseInt(IOUtils.readLine("Number of edges of start node: "));
			int normalNodeEdges = Integer.parseInt(IOUtils.readLine("Number of edges of normal node: "));
			int nodeEdgesVar = Integer.parseInt(IOUtils.readLine("Max variation of add edges: "));
			String outGraphFileName = IOUtils.readLine("Graph file name: ");
			String outTestFileName = IOUtils.readLine("Test file name: ");
			
			SinglePathGraphGenerator generator = new SinglePathGraphGenerator(numberOfNodes, 
					subGraphSize, maxQueries, maxDepthQuery, startNodeEdges, normalNodeEdges, nodeEdgesVar);
			
			generator.generate(outGraphFileName, outTestFileName);
			System.out.println("OK");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
		
	
}
