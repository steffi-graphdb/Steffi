package com.imgraph.storage;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.infinispan.Cache;
import org.zeromq.ZMQ;

import com.imgraph.common.Configuration;
import com.imgraph.model.EdgeType;
import com.imgraph.model.ExtImgEdge;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgIndexedEdges;
import com.imgraph.model.ImgVertex;
import com.imgraph.networking.messages.LocalNeighborsRepMsg;
import com.imgraph.networking.messages.LocalNeighborsReqMsg;
import com.imgraph.networking.messages.Message;

/**
 * @author Aldemar Reynaga
 * The process that updates the virtual edges of all the vertices in the second phase of the batch loading
 */
public class Local2HopNeighborProcessor implements Serializable, Callable<Integer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1727696494136688889L;
	
	private ZMQ.Context context;
	private Map<Integer, ZMQ.Socket> sockets;
	private Cache<Long, Object> cache;
	private String localAddress;
	private int localAddressIndex;
	
	
	private void init() {
		//context = ZMQ.context(1);
		context = ImgGraph.getInstance().getZMQContext();
		sockets = new HashMap<Integer, ZMQ.Socket>();
		cache = CacheContainer.getCellCache();
		localAddress = cache.getCacheManager().getAddress().toString();
		localAddressIndex = ImgGraph.getInstance().getMemberIndex(localAddress);
		
		Map<String, String> clusterAddresses = StorageTools.getAddressesIps();
		
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			ZMQ.Socket socket = context.socket(ZMQ.REQ);
			socket.setIdentity(("local2HProc_" + localAddress + "_" + entry.getValue()).getBytes());
			
			socket.connect("tcp://" + entry.getValue() + ":" + 
					Configuration.getProperty(Configuration.Key.NODE_PORT));
			
			sockets.put(ImgGraph.getInstance().getMemberIndex(entry.getKey()), socket);
		}
	}
	
	private void close() {
		for (ZMQ.Socket socket : sockets.values())
			socket.close();
	}
	
	private void updateNeighborFlags(int remoteAddressIndex, Collection<ImgEdge> edges,
			Map<Long, ImgVertex> vertices) throws Exception {
		
		
		Cache<Long, Object> cache = CacheContainer.getCellCache();
		
		for (ImgEdge edge : edges) {
			
			
			
			if (vertices != null) {
				ImgVertex vertex = vertices.get(edge.getDestCellId());
				vertex.markNeighborFlags(remoteAddressIndex, edge.getSourceCellId(), edge.getName());
				cache.put(vertex.getId(), vertex);
			} else {
				ImgVertex vertex = (ImgVertex)cache.getAdvancedCache().getDataContainer().get(edge.getDestCellId()).getValue();
				vertex.markNeighborFlags(remoteAddressIndex, edge.getSourceCellId(), edge.getName());
			}
			
		}
	
	}
	
	private void collectCellIds(TLongSet cellIds, 
			ImgIndexedEdges edges)  {
		
		
		for (ImgEdge edge : edges.getAllEdges()) {
			ExtImgEdge extEdge = (ExtImgEdge) edge;
			if (extEdge.getNeighborFlag() == 0) {
				cellIds.add(extEdge.getDestCellId());
			}
			
		}
			
	}
	
	public static void addInvertedEdges(int addressIndex, Long id, ImgIndexedEdges indexedEdges) {
		for (ImgEdge edge : indexedEdges.getAllEdges()) {
			ImgVertex vertex = (ImgVertex) ImgGraph.getInstance().retrieveRawCell(edge.getDestCellId());
			
			ImgEdge invertedEdge = vertex.getEdge(edge.getSourceCellId(), EdgeType.invertType(edge.getEdgeType()), 
					edge.getName(), addressIndex);   //vertex.getEdge(edge.getId());
			Local2HopNeighbors.addNeighborEdge(id, invertedEdge);
			((ExtImgEdge)invertedEdge).setNeighborFlag((byte) 1);
			
		}
		Local2HopNeighbors.getNeighbors(id).trimToSize();
		//Local2HopNeighbors.setNeighbors(repEntry.getKey(), repEntry.getValue());
	}
	
	private void processExtEdges(Map<Integer, TLongSet> addressVertexIds,
			Map<Long, ImgVertex> vertices) throws Exception{
		for (Entry<Integer, TLongSet> entry : addressVertexIds.entrySet()) {
			LocalNeighborsReqMsg reqMsg = new LocalNeighborsReqMsg();
			reqMsg.setVertexIds(entry.getValue());
			reqMsg.setLocalAddress(localAddress);
			
			ZMQ.Socket socket = sockets.get(entry.getKey());
			
			socket.send(Message.convertMessageToBytes(reqMsg), 0);
			
			LocalNeighborsRepMsg repMsg = (LocalNeighborsRepMsg) Message.readFromBytes(socket.recv(0));
			
			for (Entry<Long, ImgIndexedEdges> repEntry : repMsg.getVertexEdgeMap().entrySet()) {
				
				
				addInvertedEdges(entry.getKey(), repEntry.getKey(), repEntry.getValue());
				
				updateNeighborFlags(entry.getKey(), repEntry.getValue().getAllEdges(), vertices);
			}
		
		}
	}
	
	public int updateLocal2HopNeighbors() {
		try {
			init();
			
			Map<Integer, TLongSet> addressVertexIds = new HashMap<Integer, TLongSet>();
			Map<Long, ImgVertex> vertices = null;
			TLongSet vertexIds = null;
			int vertexCounter = 0;
			ImgGraph graph = ImgGraph.getInstance();
			ImgVertex vertex = null;
			
			if (graph.storeSerializedCells())
				vertices = new HashMap<Long, ImgVertex>();
			
			int numberOfVertices = cache.size();
			
			for (Object rawCell : cache.values()) {
				
				if (graph.storeSerializedCells())
					throw new UnsupportedOperationException("Storage of serialized cells was not implemented");
				else
					vertex = (ImgVertex) rawCell;
				
				if (vertices != null)
					vertices.put(vertex.getId(), vertex);
				
				//for (Entry<String, ImgEdgeMap> entry : vertex.getEdgeAddresses()) {
				for (int i=0; i<graph.getNumberOfMembers(); i++) {
					if (i != localAddressIndex) {
						
						if (vertex.getEdgeAddresses()[i] != null) {
						
							vertexIds = addressVertexIds.get(i);
							if (vertexIds == null) {
								vertexIds = new TLongHashSet();
								addressVertexIds.put(i, vertexIds);
							}
							
							collectCellIds(vertexIds, vertex.getEdgeAddresses()[i]);
						}
						
					}
				}
				vertexCounter++;
				
				if (vertexCounter > 0 && vertexCounter%500 == 0) {
					
					processExtEdges(addressVertexIds, vertices);
					
					if (vertexCounter%100000 == 0) {
					
						System.out.println((numberOfVertices - vertexCounter) + " vertices left to be processed");
						System.out.flush();
					
						Thread.sleep(1500);
					}
					//System.gc();
					for (TLongSet ids  : addressVertexIds.values())
						ids.clear();
					addressVertexIds.clear();
					if (vertices != null)
						vertices.clear();
					//vertexCounter = 0;
				}
				
				
				
			}
			processExtEdges(addressVertexIds, vertices);
			System.out.println("2HN calculation completed");
			
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		} finally {
			close();
		}
	}
	
	
	@Override
	public Integer call() throws Exception {
		
		return updateLocal2HopNeighbors();
	}

}
