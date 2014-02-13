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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.steffi.Main;
import com.steffi.common.Configuration;
import com.steffi.model.Cell;
import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.NodeServer;
import com.steffi.storage.CacheContainer;
import com.steffi.storage.CellTransaction.TransactionConclusion;
import com.tinkerpop.blueprints.Edge;

/**
 * @author Aldemar Reynaga
 * Tests for CRUD operations on the graph
 */
public class TestCrudOperations extends BaseLocalTest{

	@Test
	public void testBasicAddition() {
		System.out.println("Starting tests....");
		SteffiGraph graph = SteffiGraph.getInstance();
		graph.startTransaction();
		graph.registerItemName("friend");
		graph.registerItemName("father");
		graph.registerItemName("age");
		graph.registerItemName("country");
		graph.registerItemName("affinity");
		
		
		
		SteffiVertex v1 = graph.addVertex(1L, null);
		SteffiVertex v2 = graph.addVertex(2L, null);
		SteffiVertex v3 = graph.addVertex(3L, null);
		
		v1.putAttribute("age", 34);
		v2.putAttribute("age", 30);
		v3.putAttribute("age", 15);
		v3.putAttribute("country", "Belgium");
		
		
		SteffiEdge e12 = v1.addEdge(v2, false, "friend");
		v1.addEdge(v3, true, "father");
		SteffiEdge e23 = v2.addEdge(v3, false, "friend");
		
		e12.putAttribute("affinity", "high");
		e23.putAttribute("affinity", "low");
		
		graph.stopTransaction(TransactionConclusion.COMMIT);
		
		
		SteffiVertex rv1 = (SteffiVertex) graph.retrieveCell(1L);
		SteffiVertex rv2 = (SteffiVertex) graph.retrieveCell(2L);
		SteffiVertex rv3 = (SteffiVertex) graph.retrieveCell(3L);
		
		assertEquals(1L, rv1.getId());
		assertEquals(2L, rv2.getId());
		assertEquals(3L, rv3.getId());
		
		assertEquals(34, rv1.getAttribute("age"));
		assertEquals(30, rv2.getAttribute("age"));
		assertEquals(15, rv3.getAttribute("age"));
		assertEquals("Belgium", rv3.getAttribute("country"));
		
		int counter = 0;
		for (String key : rv3.getAttributeKeys()) {
			counter++;
			assertTrue(key.equals("country") || key.equals("age"));
		}
		assertEquals(2, counter);
			
		
		
		for (SteffiEdge edge : rv1.getEdges()) {
			assertNotNull(edge.getId());
			if (edge.getName().equals("friend") && edge.getSourceCellId() == 1 && edge.getDestCellId() == 2
				&& edge.getEdgeType().equals(EdgeType.UNDIRECTED)) {
				assertEquals("high", edge.getAttribute("affinity"));
			} else if (edge.getName().equals("father") && edge.getSourceCellId() == 1 && edge.getDestCellId() == 3
				&& edge.getEdgeType().equals(EdgeType.OUT)) {
				assertFalse(edge.getAttributeKeys().iterator().hasNext());
			} else {
				fail("Edge not valid");
			}
		}
		
		for (SteffiEdge edge : rv2.getEdges()) {
			assertNotNull(edge.getId());
			if (edge.getName().equals("friend") && edge.getSourceCellId() == 2 && edge.getDestCellId() == 1
				&& edge.getEdgeType().equals(EdgeType.UNDIRECTED)) {
				assertEquals("high", edge.getAttribute("affinity"));
			} else if (edge.getName().equals("friend") && edge.getSourceCellId() == 2 && edge.getDestCellId() == 3
				&& edge.getEdgeType().equals(EdgeType.UNDIRECTED)) {
				assertEquals("low", edge.getAttribute("affinity"));
			} else {
				fail("Edge not valid");
			}
		}
		
		for (SteffiEdge edge : rv3.getEdges()) {
			assertNotNull(edge.getId());
			if (edge.getName().equals("friend") && edge.getSourceCellId() == 3 && edge.getDestCellId() == 2
				&& edge.getEdgeType().equals(EdgeType.UNDIRECTED)) {
				assertEquals("low", edge.getAttribute("affinity"));
			} else if (edge.getName().equals("father") && edge.getSourceCellId() == 3 && edge.getDestCellId() == 1
				&& edge.getEdgeType().equals(EdgeType.IN)) {
				assertFalse(edge.getAttributeKeys().iterator().hasNext());
			} else {
				fail("Edge not valid");
			}
		}
		
	}
	
	
	
	@Test
	public void testRemoveVertex() {
		SteffiGraph graph = SteffiGraph.getInstance();
		
		
		graph.startTransaction();
		SteffiVertex v101 = (SteffiVertex) graph.retrieveCell(101L);
		v101.remove();
		graph.commit();
		
		assertNull(graph.retrieveCell(101L));
		SteffiVertex v103 = (SteffiVertex) graph.retrieveCell(103L);
		SteffiVertex v104 = (SteffiVertex) graph.retrieveCell(104L);

		
		for (SteffiEdge edge : v103.getEdges()) 
			if (!isEdgeWith(edge, EdgeType.IN, "recommends", 100, 103))
				fail("Edge not valid of v103: " + edge);
		
		for (SteffiEdge edge : v104.getEdges()) 
			if (!isEdgeWith(edge, EdgeType.IN, "recommends", 100, 104))
				fail("Edge not valid of v104: " + edge);
		
		
	}
	
	
		
	@Test
	public void testSetProperties() {
		SteffiGraph graph = SteffiGraph.getInstance();
		
		graph.registerItemName("height");
		graph.registerItemName("date");
		
		graph.startTransaction();
		SteffiVertex v102 = (SteffiVertex)graph.retrieveCell(102);
		v102.putAttribute("height", 1.69);
		v102.putAttribute("name", "David");
		
		SteffiEdge e102_100 = v102.getEdge(100, EdgeType.UNDIRECTED, "classmate");
		SteffiEdge e102_101 = v102.getEdge(101, EdgeType.IN, "recommends");
		e102_100.putAttribute("date", "2002-10-15");
		e102_101.putAttribute("date", "2012-01-01");
		
		graph.commit();
		
		//Check vertex properties
		SteffiVertex rv102 = (SteffiVertex)graph.retrieveCell(102);
		Map<String, Object> expectedProperties = new HashMap<String, Object>(); 
		expectedProperties.put("height", 1.69);
		expectedProperties.put("name", "David");
		expectedProperties.put("weight", 77);
		
		checkProperties(rv102, expectedProperties);
		
		//Check edge properties
		SteffiVertex rv100 = (SteffiVertex)graph.retrieveCell(100);
		SteffiVertex rv101 = (SteffiVertex)graph.retrieveCell(101);
		SteffiEdge e100_102 = rv100.getEdge(102, EdgeType.UNDIRECTED, "classmate");
		SteffiEdge e101_102 = rv101.getEdge(102, EdgeType.OUT, "recommends");
		
		e102_100 = rv102.getEdge(100, EdgeType.UNDIRECTED, "classmate");
		expectedProperties.clear();
		expectedProperties.put("date", "2002-10-15");
		checkProperties(e102_100, expectedProperties);
		checkProperties(e100_102, expectedProperties);
		
		e102_101 = rv102.getEdge(101, EdgeType.IN, "recommends");
		expectedProperties.clear();
		expectedProperties.put("date", "2012-01-01");
		expectedProperties.put("stars", 3);
		checkProperties(e102_101, expectedProperties);
		checkProperties(e101_102, expectedProperties);
		
		
		
	}
	
	@Test
	public void testRemoveProperties() {
		SteffiGraph graph = SteffiGraph.getInstance();
		graph.startTransaction();
		
		SteffiVertex v104 = (SteffiVertex)graph.retrieveCell(104);
		v104.removeAttribute("weight");
		
		SteffiEdge edge = v104.getEdge(101L, EdgeType.OUT, "recommends");
		edge.removeAttribute("stars");
		
		graph.commit();
		
		SteffiVertex rv104 = (SteffiVertex)graph.retrieveCell(104);
		edge = v104.getEdge(101L, EdgeType.OUT, "recommends");
		
		Map<String, Object> expectedProperties = new HashMap<String, Object>();
		expectedProperties.put("name", "Amy");
		checkProperties(rv104, expectedProperties);
		
		expectedProperties.clear();
		checkProperties(edge, expectedProperties);
		
	}

	
	@Test
	public void removeEdge() {
		SteffiGraph graph = SteffiGraph.getInstance();
		graph.startTransaction();
		
		SteffiVertex v102 = graph.getVertex(102);
		SteffiEdge e102_100 = v102.getEdge(100, EdgeType.UNDIRECTED, "classmate");
		v102.removeEdge(e102_100);
		
		SteffiVertex v104 = graph.getVertex(104);
		SteffiEdge e104_100 = v104.getEdge(100, EdgeType.IN, "recommends");
		v104.removeEdge(e104_100);
		
		graph.commit();
		
		SteffiVertex rv102 = graph.getVertex(102);
		SteffiVertex rv104 = graph.getVertex(104);
		SteffiVertex rv100 = graph.getVertex(100);
		
		for (SteffiEdge edge : rv102.getEdges()) 
			if (!isEdgeWith(edge, EdgeType.IN, "recommends", 101, 102))
				fail("Edge not valid: " + edge);
		
		for (SteffiEdge edge : rv104.getEdges())
			if (!isEdgeWith(edge, EdgeType.OUT, "recommends", 101, 104))
				fail("Edge not valid: " + edge);
		
		for (SteffiEdge edge : rv100.getEdges())
			if (!isEdgeWith(edge, EdgeType.OUT, "recommends", 103, 100))
				fail("Edge not valid: " + edge);
	}
}
