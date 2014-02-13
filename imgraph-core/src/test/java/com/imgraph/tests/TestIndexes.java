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
package com.imgraph.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.imgraph.index.ImgIndex;
import com.imgraph.index.ImgIndexHits;
import com.imgraph.model.Cell;
import com.imgraph.model.EdgeType;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;



/**
 * @author Aldemar Reynaga
 * Set of tests for index functionalities
 */
public class TestIndexes extends BaseLocalTest {

	
	
	
	@Override
	@Before
	public void graphSetup() {
		super.graphSetup();
		graph.createVertexIndex("vIndex");
		graph.createEdgeIndex("eIndex");
	}
	
	

	@Override
	@After
	public void graphTearDown() {
		super.graphTearDown();
		graph.removeIndex("vIndex");
		graph.removeIndex("eIndex");
	}



	@Test
	public void testIndexCreation() {
		
		
		assertNotNull(graph.getIndex("vIndex", ImgVertex.class));
		assertNotNull(graph.getIndex("eIndex", ImgEdge.class));
	}
	
	private <T extends Cell> void indexProperties(T cell, ImgIndex<T> index) {
		for (String key : cell.getAttributeKeys())
			index.put(key, cell.getAttribute(key), cell);
	}
	
	private void putKeyValues() {
		ImgIndex<ImgVertex> vertexIndex = graph.getIndex("vIndex", ImgVertex.class);
		ImgIndex<ImgEdge> edgeIndex = graph.getIndex("eIndex", ImgEdge.class);
		
		graph.startTransaction();
		ImgVertex v100 = graph.getVertex(100);
		ImgVertex v101 = graph.getVertex(101);
		ImgVertex v1 = graph.addVertex(1L, null);
		v1.putAttribute("name", "Paul");
		v1.putAttribute("weight", 50);
		
		indexProperties(v100, vertexIndex);
		indexProperties(v101, vertexIndex);
		indexProperties(v1, vertexIndex);
		
		for (ImgEdge edge : v100.getEdges())
			indexProperties(edge, edgeIndex);
		
		for (ImgEdge edge : v101.getEdges())
			indexProperties(edge, edgeIndex);
		
		graph.commit();
	}
	
	@Test
	public void testIndexRollback() {
		putKeyValues();
		
		ImgIndex<ImgVertex> vertexIndex = graph.getIndex("vIndex", ImgVertex.class);
		ImgIndex<ImgEdge> edgeIndex = graph.getIndex("eIndex", ImgEdge.class);
		
		graph.startTransaction();
		ImgVertex v103 = graph.getVertex(103);
		ImgVertex v104 = graph.getVertex(104);
		indexProperties(v103, vertexIndex);
		indexProperties(v104, vertexIndex);
		
		indexProperties(v103.getEdge(100, EdgeType.IN, "recommends"), edgeIndex);
		indexProperties(v104.getEdge(101, EdgeType.OUT, "recommends"), edgeIndex);
		indexProperties(v104.getEdge(100, EdgeType.IN, "recommends"), edgeIndex);
		
		graph.rollback();
		
		ImgIndexHits<ImgVertex> vertexHits = vertexIndex.get("name", "Jane");
		assertFalse(vertexHits.hasNext());
		vertexHits = vertexIndex.get("weight", 49);
		assertFalse(vertexHits.hasNext());
		vertexHits = vertexIndex.get("name", "Amy");
		assertFalse(vertexHits.hasNext());
		vertexHits = vertexIndex.get("wwight", 98);
		assertFalse(vertexHits.hasNext());
		
		ImgIndexHits<ImgEdge> edgeHits = edgeIndex.get("stars", 3);
		int counter=0;
		for (ImgEdge edge: edgeHits) {
			assertTrue(isEdgeWith(edge, EdgeType.OUT, "recommends", 103, 100) ||
					isEdgeWith(edge, EdgeType.OUT, "recommends", 102, 101));
			counter++;
		}
		assertEquals(2, counter);
		edgeHits = edgeIndex.get("stars", 5);
		assertTrue(isEdgeWith(edgeHits.next(), EdgeType.OUT, "recommends", 104, 100));
		assertFalse(edgeHits.hasNext());
		
	}
	
	@Test
	public void testRemoveKeyValue() {
		putKeyValues();
		
		ImgIndex<ImgVertex> vertexIndex = graph.getIndex("vIndex", ImgVertex.class);
		ImgIndex<ImgEdge> edgeIndex = graph.getIndex("eIndex", ImgEdge.class);
		
		graph.startTransaction();
		ImgVertex v100 = graph.getVertex(100);
		ImgVertex v101 = graph.getVertex(101);
		
		vertexIndex.remove(v100, "name", "John");
		vertexIndex.remove(v101, "weight", 50);
		
		edgeIndex.remove(v100.getEdge(103, EdgeType.OUT, "recommends"), "stars", 3);
		edgeIndex.remove(v101.getEdge(102, EdgeType.OUT, "recommends"), "stars", 3);
		
		graph.commit();
		
		vertexIndex = graph.getIndex("vIndex", ImgVertex.class);
		edgeIndex = graph.getIndex("eIndex", ImgEdge.class);
		
		ImgIndexHits<ImgVertex> vertexHits = vertexIndex.get("name", "John");
		assertFalse(vertexHits.hasNext());
		
		vertexHits = vertexIndex.get("weight", 50);
		assertEquals(1, vertexHits.next().getId());
		assertFalse(vertexHits.hasNext());
		
		ImgIndexHits<ImgEdge> edgeHits= edgeIndex.get("stars", 3);
		assertFalse(edgeHits.hasNext());
		
		
		
	}
	
	@Test
	public void testPutKeyValue() {
		putKeyValues();
		
		ImgIndex<ImgVertex> vertexIndex = graph.getIndex("vIndex", ImgVertex.class);
		ImgIndex<ImgEdge> edgeIndex = graph.getIndex("eIndex", ImgEdge.class);
		
		
		ImgIndexHits<ImgVertex> indexHits = vertexIndex.get("name", "John");
		assertEquals(100, indexHits.next().getId());
		assertFalse(indexHits.hasNext());
		
		indexHits = vertexIndex.get("name", "Mary");
		assertEquals(101, indexHits.next().getId());
		assertFalse(indexHits.hasNext());
		
		indexHits = vertexIndex.get("name", "Paul");
		assertEquals(1, indexHits.next().getId());
		assertFalse(indexHits.hasNext());
		
		indexHits = vertexIndex.get("weight", 68);
		assertEquals(100, indexHits.next().getId());
		assertFalse(indexHits.hasNext());
		
		Set<Long> vertexIds = new HashSet<Long>();
		indexHits = vertexIndex.get("weight", 50);
		for (ImgVertex vertex : indexHits)
			vertexIds.add(vertex.getId());
		assertTrue(vertexIds.contains(1L));
		assertTrue(vertexIds.contains(101L));
		assertEquals(2, vertexIds.size());
		
		ImgIndexHits<ImgEdge> edgeIndexHits = edgeIndex.get("stars", 5);
		assertTrue(isEdgeWith(edgeIndexHits.next(), EdgeType.OUT, "recommends", 104, 100));
		assertFalse(indexHits.hasNext());
		
		edgeIndexHits = edgeIndex.get("stars", 3);
		int counter=0;
		for (ImgEdge edge: edgeIndexHits) {
			assertTrue(isEdgeWith(edge, EdgeType.OUT, "recommends", 103, 100) ||
					isEdgeWith(edge, EdgeType.OUT, "recommends", 102, 101));
			counter++;
		}
		assertEquals(2, counter);
		
		edgeIndexHits = edgeIndex.get("stars", 5);
		isEdgeWith(edgeIndexHits.next(), EdgeType.OUT, "recommends", 104, 100);
		assertFalse(edgeIndexHits.hasNext());
		
		
		
		
	}
	
}
