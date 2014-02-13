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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.infinispan.lifecycle.ComponentStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.steffi.Main;
import com.steffi.common.Configuration;
import com.steffi.model.Cell;
import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiGraph;
import com.steffi.model.SteffiVertex;
import com.steffi.networking.NodeServer;
import com.steffi.storage.CacheContainer;


/**
 * @author Aldemar Reynaga
 * Base class for Imgraph's tests. It starts the data server and contains the logic to create a simple graph 
 */
public class BaseLocalTest {
	
	protected SteffiGraph graph = SteffiGraph.getInstance();
	
	@BeforeClass
	public static void imgraphSetup() throws Exception {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		if (!CacheContainer.getCacheContainer().getStatus().equals(ComponentStatus.RUNNING)) {
			CacheContainer.getCacheContainer().start();
			CacheContainer.getCellCache().start();
			new Thread(new NodeServer()).start();
			Thread.sleep(3000);
		}
	}

	
	
	@Before
	public void graphSetup() {
		createTestGraph();
	}
	
	@After
	public void graphTearDown() {
		SteffiGraph.getInstance().removeAll();
	}
	
	private void createTestGraph() {
		SteffiGraph graph = SteffiGraph.getInstance();
		
		graph.startTransaction();
		
		graph.registerItemName("recommends");
		graph.registerItemName("classmate");
		graph.registerItemName("name");
		graph.registerItemName("stars");
		graph.registerItemName("weight");
		
		
		SteffiVertex v100 = graph.addVertex(100L, null);
		SteffiVertex v101 = graph.addVertex(101L, null);
		SteffiVertex v102 = graph.addVertex(102L, null);
		SteffiVertex v103 = graph.addVertex(103L, null);
		SteffiVertex v104 = graph.addVertex(104L, null);
		
		v100.putAttribute("name", "John");
		v101.putAttribute("name", "Mary");
		v102.putAttribute("name", "Peter");
		v103.putAttribute("name", "Jane");
		v104.putAttribute("name", "Amy");
		
		v100.putAttribute("weight", 68);
		v101.putAttribute("weight", 50);
		v102.putAttribute("weight", 77);
		v103.putAttribute("weight", 49);
		v104.putAttribute("weight", 98);
		
		SteffiEdge e100_103 = v100.addEdge(v103, true, "recommends");
		e100_103.putAttribute("stars", 3);
		
		v100.addEdge(v102, false, "classmate");
		
		SteffiEdge e100_104 = v100.addEdge(v104, true, "recommends");
		e100_104.putAttribute("stars", 5);
		
		v103.addEdge(v101, false, "classmate");
		
		SteffiEdge e101_102 = v101.addEdge(v102, true, "recommends");
		e101_102.putAttribute("stars", 3);
		
		SteffiEdge e104_101 = v104.addEdge(v101, true, "recommends");
		e104_101.putAttribute("stars", 4);
		
		graph.commit();
		
	}
	
	protected boolean isEdgeWith(SteffiEdge edge, EdgeType edgeType, String name, long destId, long sourceId) {
		return (edge.getName().equals(name) && edge.getEdgeType().equals(edgeType) &&
				edge.getDestCellId() == destId && edge.getSourceCellId() == sourceId);
	}
	
	protected void checkProperties(Cell cell, Map<String, Object> expectedProperties) {
		for (String key : cell.getAttributeKeys()) {
			if (expectedProperties.containsKey(key))
				assertEquals(expectedProperties.get(key), cell.getAttribute(key));
			else
				fail ("Property or value not expected. Key=" + key + ", Value=" + cell.getAttribute(key));
		}
	}
}
