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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;

import com.steffi.model.EdgeType;
import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiVertex;
import com.steffi.storage.TransactionRequiredException;

/**
 * @author Aldemar Reynaga
 * Tests for transactions properties
 */
public class TestTransactions extends BaseLocalTest{

	private Object isolationTestLock;
	private boolean isolationTestWaiting;
	
	@Test(expected = TransactionRequiredException.class)
	public void testNotInitTransException() {
		graph.addVertex(2L, null);
	}
	
	@Test
	public void testAtomicity() {
		graph.startTransaction();
		SteffiVertex v1 = graph.addVertex(1L, null);
		SteffiVertex v103 = graph.getVertex(103);
		
		v1.addEdge(v103, false, "classmate");
		v103.putAttribute("weight", 75); 
		
		graph.rollback();
		
		assertNull(graph.getVertex(1));
		SteffiVertex rv103 = graph.getVertex(103);
		assertNull(rv103.getEdge(1, EdgeType.UNDIRECTED, "classmate"));
		assertEquals(49, rv103.getAttribute("weight"));
		
	}
	
	@Test
	public void testRepeatableRead() {
		graph.startTransaction();
		
		graph.getVertex(100);
		
		(new Thread(new RepeatableReadUserThread())).start();
		
		isolationTestLock = new String("Isolation main test lock for repeatable reads");
		isolationTestWaiting = true;
		
		try {
			synchronized (isolationTestLock) {
				while (isolationTestWaiting) {
					isolationTestLock.wait();
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		SteffiVertex rv100 = graph.getVertex(100);
		
		for (SteffiEdge edge : rv100.getEdges()) {
			if (!isEdgeWith(edge, EdgeType.OUT, "recommends", 103, 100) && 
					!isEdgeWith(edge, EdgeType.OUT, "recommends", 104, 100) &&
					!isEdgeWith(edge, EdgeType.UNDIRECTED, "classmate", 102, 100))
				fail("Edge not valid: " + edge);
		}
		assertEquals(68, rv100.getAttribute("weight"));
		
		SteffiVertex rv104 = graph.getVertex(104);
		
		rv104.addEdge(graph.getVertex(102), false, "classmate");
		
		graph.commit();
		
		rv100 = graph.getVertex(100);
		
		
		assertNotNull(rv100.getEdge(103, EdgeType.OUT, "recommends"));
		assertNotNull(rv100.getEdge(104, EdgeType.OUT, "recommends"));
		assertNotNull(rv100.getEdge(102, EdgeType.UNDIRECTED, "classmate"));
		SteffiEdge e100_1 = rv100.getEdge(1, EdgeType.OUT, "recommends");
		                 
		assertNotNull(e100_1);
		assertEquals(2, e100_1.getAttribute("stars"));
		assertEquals(100, rv100.getAttribute("weight"));
		
		rv104 = graph.getVertex(104);
		assertNotNull(rv104.getEdge(102, EdgeType.UNDIRECTED, "classmate"));
	}
	
	
	
	@Test
	public void testNoDirtyReads() {
		graph.startTransaction();
		SteffiVertex v1 = graph.addVertex(1L, null);
		SteffiVertex v100 = graph.getVertex(100);
		SteffiVertex v102 = graph.getVertex(102);
		
		v1.addEdge(v100, false, "classmate");
		SteffiEdge e102_1 = v102.addEdge(v1, true, "recommends");
		e102_1.putAttribute("stars", 5);
		v100.putAttribute("name", "Max");
		v102.removeAttribute("weight");
		
		testIsolationChangesApplied(v1, v100, v102);
		
		DirtyReadUserThread userThread =  new DirtyReadUserThread();
		(new Thread(userThread)).start();
		
		isolationTestLock = new String("Isolation main test lock for dirty reads");
		isolationTestWaiting = true;
		
		try {
			synchronized (isolationTestLock) {
				while (isolationTestWaiting) {
					isolationTestLock.wait();
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		graph.commit();
		
		userThread.unlock();
		
	}
	
	private void unlockIsolationTest() {
		synchronized (isolationTestLock) {
			isolationTestWaiting = false;
			isolationTestLock.notifyAll();
		}
	}
	
	
	private void testIsolationChangesApplied(SteffiVertex v1, SteffiVertex v100, SteffiVertex v102) {
		
		Assert.assertNotNull(v1);
		
		for (SteffiEdge edge : v1.getEdges()) {
			if (isEdgeWith(edge, EdgeType.UNDIRECTED, "classmate", 100, 1)) {
				
			} else if (isEdgeWith(edge, EdgeType.IN, "recommends", 102, 1)) {
				Map<String, Object> expectedProp = new HashMap<String, Object>();
				expectedProp.put("stars", 5);
				checkProperties(edge, expectedProp);
			} else {
				Assert.fail("Not valid edge: " + edge);
			}
		}
		
		Assert.assertEquals("Max", v100.getAttribute("name"));
		Assert.assertNull(v102.getAttribute("weight"));
	}
	
	private class DirtyReadUserThread implements Runnable {

		private Object lock;
		private boolean waiting;
		
		public DirtyReadUserThread () {
			lock = new String("Test lock");
		}
		
		public void unlock() {
			synchronized (lock) {
				waiting = false;
				lock.notifyAll();
			}
		}
		
		
		@Override
		public void run() {

			
			Assert.assertNull(graph.getVertex(1));
			SteffiVertex v100 = graph.getVertex(100);
			SteffiVertex v102 = graph.getVertex(102);
			
			Assert.assertNull(v100.getEdge(1, EdgeType.UNDIRECTED, "classmate"));
			Assert.assertNull(v102.getEdge(1, EdgeType.OUT, "recommends"));
			
			Assert.assertEquals("John", v100.getAttribute("name"));
			Assert.assertEquals(77, v102.getAttribute("weight"));
			
			unlockIsolationTest();
			
			waiting = true;
			
			try {
			
				synchronized (lock) {
					while (waiting) {
						lock.wait();
					}
				}
				
				Thread.sleep(500);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			
			
			
			testIsolationChangesApplied(graph.getVertex(1), graph.getVertex(100), graph.getVertex(102));
				
			
		}
		
	}
	
	
	private class RepeatableReadUserThread implements Runnable {

		@Override
		public void run() {

			graph.startTransaction();
			
			SteffiVertex v1 = graph.addVertex(1L, null);
			SteffiVertex v100 = graph.getVertex(100);
			SteffiEdge e = v100.addEdge(v1, true, "recommends");
			e.putAttribute("stars", 2);
			v100.putAttribute("weight", 100);
			
			graph.commit();
			
			
			unlockIsolationTest();
			
		}
		
	}
	
}
