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
package com.imgraph.storage;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgIndexedEdges;
import com.imgraph.model.ImgMapEdges;

/**
 * @author Aldemar Reynaga
 * The map containing the virtual edges for the local data server
 */
public class Local2HopNeighbors {
	
	private TLongObjectMap<ImgIndexedEdges> cellNeighbors;
	
	
	private Local2HopNeighbors() {
		cellNeighbors = new TLongObjectHashMap<ImgIndexedEdges>();
	}
	
	private static class SingletonHolder { 
        public static final Local2HopNeighbors instance = new Local2HopNeighbors();
	}
	
	
	
	public static void addNeighborEdge(long cellId, ImgEdge edge) {
		ImgIndexedEdges indexedEdges = SingletonHolder.instance.cellNeighbors.get(cellId);
		if (indexedEdges == null) {
			indexedEdges = new ImgMapEdges();
			synchronized (SingletonHolder.instance.cellNeighbors) {
				SingletonHolder.instance.cellNeighbors.put(cellId, indexedEdges);
			}
		}
		synchronized (indexedEdges) {
			indexedEdges.addEdge(edge);
		}
	}
	
	public static void clearNeighbors(long cellId) {
		ImgIndexedEdges indexedEdges = SingletonHolder.instance.cellNeighbors.get(cellId);
		if (indexedEdges != null) 
			synchronized (indexedEdges) {
				indexedEdges.clear();
			}
	}
	
	public static void removeNeighbors(long cellId) {
		synchronized (SingletonHolder.instance.cellNeighbors) {
			SingletonHolder.instance.cellNeighbors.remove(cellId);
		}
	}
	
	
	public static void setNeighbors(long cellId, ImgIndexedEdges neighbors) {
		synchronized (SingletonHolder.instance.cellNeighbors) {
			SingletonHolder.instance.cellNeighbors.put(cellId, neighbors);
		}
	}
	
	
	
	
	
	
	public static ImgIndexedEdges getNeighbors(final Long cellId) {
		return SingletonHolder.instance.cellNeighbors.get(cellId);
	}
	
	public static long[] getCellIds() {
		return SingletonHolder.instance.cellNeighbors.keys();
	}
	
	
}
