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
package com.steffi.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;
import org.infinispan.remoting.transport.Address;
import org.zeromq.ZMQ;

import com.steffi.common.Configuration;
import com.steffi.common.Configuration.Key;
import com.steffi.index.ImgIndex;
import com.steffi.index.ImgMapIndex;
import com.steffi.loader.ResponseProcessor;
import com.steffi.networking.NodeClients;
import com.steffi.networking.messages.IdentifiableMessage;
import com.steffi.storage.CacheContainer;
import com.steffi.storage.CellTransactionFactory;
import com.steffi.storage.CellTransactionThread;
import com.steffi.storage.GraphNamesManager;
import com.steffi.storage.StorageTools;
import com.steffi.storage.CellTransaction.TransactionConclusion;

/**
 * @author Aldemar Reynaga
 * The Singleton class representing the Imgraph data base, this is the main point of access
 * to the graph functionalities
 */
public class SteffiGraph implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3640095076173432800L;
	
	
	private ImgIndex<SteffiVertex> vertexIndex;
	private ImgIndex<SteffiEdge> edgeIndex;
	private boolean serializedCells;
	private boolean compressCells;
	
	private Map<String, Integer> itemNames;
	private Map<Integer, String> reveresItemNames;
	
	private Map<String, Integer> memberIndexes;
	private int numberOfMembers;

	private String localAddress;
	private NodeClients nodeClients;
	
	private AtomicInteger traversalManagerTurn;
	private String[] traversalManagerIps;
	private final ZMQ.Context context;
	
	
	private static class SingletonHolder { 
        public static final SteffiGraph instance = new SteffiGraph();
	}
	
	
	public static SteffiGraph getInstance() {
		return SingletonHolder.instance;
	}
	
	
	
	private SteffiGraph() {
		context = ZMQ.context(1);

		vertexIndex = new ImgMapIndex<SteffiVertex>(CacheContainer.VERTEX_INDEX_CACHE_NAME, SteffiVertex.class, false);
		edgeIndex = new ImgMapIndex<SteffiEdge>(CacheContainer.EDGE_INDEX_CACHE_NAME, SteffiEdge.class, false);
		serializedCells = Configuration.getProperty(Key.STORE_SERIALIZED_CELLS).equals("true");
		compressCells = Configuration.getProperty(Key.COMPRESS_CELLS).equals("true");
		itemNames = new ConcurrentHashMap<String, Integer>();
		reveresItemNames = new ConcurrentHashMap<Integer, String>();
		memberIndexes = new HashMap<String, Integer>();
		localAddress = CacheContainer.getCellCache().
				getCacheManager().getAddress().toString();
		traversalManagerTurn = new AtomicInteger(0);
		traversalManagerIps = Configuration.getProperty(Key.MANAGER_IPS).split(",");
	}
	
	
	public ZMQ.Context getZMQContext() {
		
		return this.context;
	}
	
	private boolean isLocalCell(long cellId) {
		return StorageTools.getCellAddress(cellId).equals(localAddress);
	}
	
	
	public <T extends Cell> ImgIndex<T> getIndex(String indexName, Class<T> className) {
		return new ImgMapIndex<T>(indexName, className, false);
	}
	
	public ImgIndex<SteffiVertex> createVertexIndex(String indexName) {
		return new ImgMapIndex<SteffiVertex>(indexName, SteffiVertex.class, true);
	}
	
	public ImgIndex<SteffiEdge> createEdgeIndex(String indexName) {
		return new ImgMapIndex<SteffiEdge>(indexName, SteffiEdge.class,  true);
	}
	
	public boolean isCompressCells() {
		return compressCells;
	}
	
	
	
	public void initializeMemberIndexes() {
		memberIndexes.clear();
		for (Address address : CacheContainer.getCacheContainer().getTransport().getMembers()) 
			memberIndexes.put(address.toString(), memberIndexes.size());
		numberOfMembers = memberIndexes.size();
		
		
		nodeClients = new NodeClients(this);
		
	}
	
	
	public int getNextTraversalManagerIndex() {
		int value = traversalManagerTurn.getAndIncrement();
		
		
		return Math.abs(value%traversalManagerIps.length) ;
	}
	
	public String[] getTraversalManagerIps() {
		return traversalManagerIps;
	}
	
	public String getNextManagerIp() {
		return traversalManagerIps[getNextTraversalManagerIndex()];
	}
	
	public String getLocalAddress() {
		return localAddress;
	}
	
	public int getLocalAddressIndex() {
		return getMemberIndex(localAddress);
	}
	
	
	public void sendMessageToNode(int memberIndex, IdentifiableMessage message,
			ResponseProcessor sender) {
		if (nodeClients == null) 
			throw new RuntimeException("The cluster must be initialized");
		
		nodeClients.sendMessage(memberIndex, message, sender);
	}
	
	
	public int getMemberIndex(String address) {
		return getMemberIndexes().get(address);
	}
	
	public Map<String, Integer> getMemberIndexes() {
		if (memberIndexes == null)
			throw new RuntimeException("The cluster must be initialized");
		
		
		return memberIndexes;
	}
	
	
	public void storeCell(long cellId, Cell cell) {
		Cache <Long, Object> cache = CacheContainer.getCellCache();
		
		if (storeSerializedCells()) 
			throw new UnsupportedOperationException("Cell serialization is not implemented"); 
		else
			cache.put(cellId, cell);
	}
	
	
	public int getNumberOfMembers() {
		if (memberIndexes.isEmpty())
			initializeMemberIndexes();
		return numberOfMembers;
	}
	
	public void validateEdgeName(String name) {
		if (name != null && !name.trim().equals("") && !itemNames.containsKey(name))
			throw new RuntimeException("The edge name '" + name + "' has to be registered in the graph");
	}
	
	
	public void registerLocalItemName(String name) {
		int index = itemNames.size();
		
		itemNames.put(name, index);
		reveresItemNames.put(index, name);
	}
	
	public void registerItemName(String name) {
		if (itemNames.containsKey(name))
			return;
		
		
		
		try {
			DistributedExecutorService des = new DefaultExecutorService(CacheContainer.getCellCache());
			GraphNamesManager graphNamesManager = new GraphNamesManager(name);
			List<Future<Boolean>> results =  des.submitEverywhere(graphNamesManager);
			
			for (Future<Boolean> future : results) {
				if (!future.get(5, TimeUnit.MINUTES))
					 throw new RuntimeException("Error registering the new name");
				
		    }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		
	}
	
	
	
	public int getItemNameIndex(String name) {
		
		if (!itemNames.containsKey(name))
			return -1;
		
		return itemNames.get(name);
	}
	
	public String getItemName(int index) {
		return reveresItemNames.get(index);
	}
	
	public int getNumberOfEdgeNames() {
		return itemNames.size();
	}
	
	
	
	
	public SteffiVertex addVertex(Long id, String vertexName) {
		SteffiVertex v = new SteffiVertex(id, vertexName);
		return v;
	}
	
	public void startTransaction() {
		if (! CellTransactionThread.isTransactionSet())
        	CellTransactionFactory.beginTransaction();
	}
	
	public void commit() {
		stopTransaction(TransactionConclusion.COMMIT);
	}
	
	public void rollback() {
		stopTransaction(TransactionConclusion.ROLLBACK);
	}
	
	public void stopTransaction(TransactionConclusion transactionConclusion) {
		if (!CellTransactionThread.isTransactionSet())
			return;
		
		if (transactionConclusion.equals(TransactionConclusion.COMMIT))
			CellTransactionThread.get().commit();
		else
			CellTransactionThread.get().rollback();
	}
	
	
	
	public boolean storeSerializedCells() {
		return serializedCells;
	}
	
	public ImgIndex<SteffiVertex> getDefaultVertexIndex() {
		
		return vertexIndex;
	}
	
	public ImgIndex<SteffiEdge> getDefaultEdgeIndex() {
		
		return edgeIndex;
	}
	
	
	public SteffiVertex getVertex(long vertexId) {
		return (SteffiVertex) retrieveCell(vertexId);
	}
	
	public Cell retrieveCell(long cellId){
		return retrieveCell(cellId, true);
	}
	
	public Cell retrieveRawCell(long cellId) {
		return retrieveCell(cellId, false);
	}
	
	private Cell retrieveCell(long cellId, boolean transactionSupport){
		Cache<Long, Object> cellCache = CacheContainer.getCellCache();
		Cell cell = null;
		Cell rawCell = null;
		
		if (CellTransactionThread.isTransactionSet()) { 
			rawCell = CellTransactionThread.get().getCell(cellId);
			if (rawCell != null)
				return rawCell;
		}
		
		rawCell = (Cell) cellCache.get(cellId);
		
		
		if (rawCell != null) {
			if (transactionSupport && isLocalCell(cellId)) {
				cell = rawCell.clone();
			} else {
				cell = rawCell;
			}
		}
			
		if (CellTransactionThread.isTransactionSet())
			CellTransactionThread.get().addCell(cell);
		
		return cell;
		
	}
	
	public void removeCell(long cellId) {
		Cache<Long, Cell> cellCache = CacheContainer.getCellCache();
		
		cellCache.remove(cellId);
	}
	
	
	public Iterable<Long> getCellIds() {
		throw new UnsupportedOperationException();
	}
	
	
	public Iterable<SteffiVertex> getVertices() {
		throw new UnsupportedOperationException();
	}
	
	
	public SteffiVertex getVertexByName(String vertexName) {
		Cache<Long, Cell> cellCache = CacheContainer.getCellCache();
		for (Cell cell : cellCache.values()) 
			if (cell.getName().equals(vertexName) && cell.getCellType().equals(CellType.VERTEX)) 
				return (SteffiVertex) cell;
			
		return null;
	}
	
	protected void checkExistingVertex(String id) {
		if (CacheContainer.getCellCache().containsKey(id))
			throw new RuntimeException("There  is already a vertex with id: " + id);
	}
	
	public void removeAll() {
		CacheContainer.getCellCache().clear();
	}
	
	public void removeIndex (String name) {
		Cache<Object, Map<Object, Boolean>> indexCache = CacheContainer.getCache(name);
		indexCache.clear();
	}
	
	public void closeGraphClients() {
		if (nodeClients != null)
			nodeClients.close();
	}
	
	
	public <T extends Cell> Iterator <ImgIndex<T>>  getUserIndexes(final Class<T> indexClass) {
		final Iterator<String> nameIterator = CacheContainer.getIndexCacheNames().iterator();
		
		Iterator<ImgIndex<T>> iterator = new Iterator<ImgIndex <T>>() {
			
			@Override
			public void remove() {
				
			}
			
			@Override
			public ImgIndex<T> next() {
				String nextName = nameIterator.next();
				return SteffiGraph.getInstance().getIndex(nextName, indexClass);
					
			}
			
			@Override
			public boolean hasNext() {
				return nameIterator.hasNext();
			}
		}; 
		
		return iterator;
	}
	
	
	
}
