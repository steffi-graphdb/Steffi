package com.imgraph.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.infinispan.Cache;
import org.infinispan.distexec.DefaultExecutorService;
import org.infinispan.distexec.DistributedExecutorService;

import com.imgraph.common.CommonTools;
import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;

/**
 * @author Aldemar Reynaga
 * Classifies the edges of vertices according to the machine where is stored 
 * the destination vertex of each edge
 */
public class EdgeAddressesUpdater implements Serializable, Callable<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5154278152363798053L;



	public static void updateEdgeAddress(ImgVertex vertex, ImgEdge edge, 
			Map<Long, String> cellAddresses) {
		String address = cellAddresses.get(edge.getDestCellId());

		if (address == null) {
			address = StorageTools.getCellAddress(edge.getDestCellId());

			if (address == null)
				throw new RuntimeException("Cell not found in cache");

			cellAddresses.put(edge.getDestCellId(), address);
		}

		vertex.addEdgeAddress(edge, address);
	}


	public static void updateEdgeAddress(ImgVertex vertex, ImgEdge edge) {
		String address = StorageTools.getCellAddress(edge.getDestCellId());
		vertex.addEdgeAddress(edge, address);
	}
	
	

	@Override
	public Integer call() throws Exception {
		Cache<Long, Object> cache = CacheContainer.getCellCache();
		ImgVertex vertex = null;
		ImgGraph graph = ImgGraph.getInstance();
		Map<Long, String> cellAddresses = new HashMap<Long, String>();
		try {
			for (Object rawCell : cache.values()) {
				
				if (graph.storeSerializedCells())
					throw new UnsupportedOperationException("Serialization was not implemented");
				else
					vertex = (ImgVertex) rawCell;
				
				for (ImgEdge edge : vertex.getEdges()) 
					updateEdgeAddress(vertex, edge, cellAddresses);
			
				cache.put(vertex.getId(), CommonTools.convertObjectToBytes(vertex));
				
			}
			return 1;
		} catch (Exception x){
			x.printStackTrace();
			return 0;
		}
		
	}
	
	public static void updateEdgeAddresses() throws InterruptedException, ExecutionException {
		DistributedExecutorService des = new DefaultExecutorService(CacheContainer.getCellCache());
		EdgeAddressesUpdater edgeAdrUpdater =  new EdgeAddressesUpdater();
		List<Future<Integer>> results =  des.submitEverywhere(edgeAdrUpdater);
		
		for (Future<Integer> future : results) {
			try {
				if (future.get(20, TimeUnit.MINUTES) == 0)
					 throw new RuntimeException("Error updating the edgeLocations");
			} catch (TimeoutException e) {
				throw new ExecutionException(e);
			}
	    }
	}

}
