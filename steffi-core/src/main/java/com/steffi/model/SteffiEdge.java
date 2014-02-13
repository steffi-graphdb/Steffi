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

import gnu.trove.procedure.TIntObjectProcedure;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.steffi.storage.CellTransactionThread;
import com.steffi.storage.StorageTools;



/**
 * @author Aldemar Reynaga
 * Defines the base edge for a graph based on the cell structure
 */
public class SteffiEdge extends Cell implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9027106654260186612L;

	protected long sourceCellId;
	protected long destCellId;
	protected EdgeType edgeType;
	
	
	


	protected SteffiEdge( long sourceCellId, 
			long destCellId, 
			EdgeType edgeType, 
			String name, 
			long edgeCellId) {
		super(edgeCellId, name);
		
		this.cellType = CellType.EDGE;
		
		this.destCellId = destCellId;
		this.sourceCellId = sourceCellId;
		this.edgeType = edgeType;
	
	}


	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgEdgeDef#getSourceCellId()
	 */
	
	public long getSourceCellId() {
		return sourceCellId;
	}

	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgEdgeDef#getDestCellId()
	 */
	
	public long getDestCellId() {
		return destCellId;
	}

	
	/* (non-Javadoc)
	 * @see com.imgraph.model.ImgEdgeDef#getEdgeType()
	 */
	
	public EdgeType getEdgeType() {
		return edgeType;
	}


	protected SteffiEdge(Long edgeCellId, String name) {
		super(edgeCellId, name);
		this.cellType = CellType.EDGE;
	}

	public static boolean removeEdgeFromCollection(SteffiEdge edge, Collection<SteffiEdge> edges) {
		if (edges != null) {
			Iterator<SteffiEdge> iterator = edges.iterator();
			
			while (iterator.hasNext()) {
				SteffiEdge item = iterator.next();
				if (item.getId() == edge.getId()) {
					iterator.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	public SteffiEdge getInvertedEdge() {
		SteffiGraph graph = SteffiGraph.getInstance();
		
		SteffiVertex destVertex = (SteffiVertex) graph.retrieveCell(destCellId);
		
		int addressIndex = graph.getMemberIndex(StorageTools.getCellAddress(sourceCellId));
		SteffiEdge invertedEdge = destVertex.getEdge(sourceCellId, EdgeType.invertType(edgeType), getName(), 
				addressIndex);
		
		return invertedEdge;
	}


	@Override
	public void putAttribute(String key, Object value) {
		int keyIndex = getKeyIndex(key);
		SteffiEdge invertedEdge = getInvertedEdge();
		
		Object oldValue = this.getAndInitAttributes().get(keyIndex);
		this.getAndInitAttributes().put(keyIndex, value);
		
		invertedEdge.getAndInitAttributes().put(keyIndex, value);
		
		CellTransactionThread.get().setCellProperty(this, keyIndex, value, oldValue);
		CellTransactionThread.get().setCellProperty(invertedEdge, keyIndex, value, oldValue);
	}

	
	@Override
	public Object removeAttribute(String key) {
		int keyIndex = getKeyIndex(key);
		SteffiEdge invertedEdge = getInvertedEdge();
		Object value = null;
		
		if (attributes != null) {
			value = attributes.remove(keyIndex);
			invertedEdge.attributes.remove(keyIndex);
		}
		
		CellTransactionThread.get().removeCellProperty(this, keyIndex);
		CellTransactionThread.get().removeCellProperty(invertedEdge, keyIndex);
		
		return value;
	}
	
	
	@Override
	public String toString() {
		final StringBuffer string = new StringBuffer("");

		string.append("TYPE: " + getEdgeType() + " S_ID: " + getSourceCellId() +  " D_ID: " + getDestCellId() + 
				" NAME: " + getName());
		
		if (!edgeType.equals(EdgeType.HYPEREDGE) && attributes != null) {
			string.append("\n\t  EDGE ATTRIBUTES:");
			
			attributes.forEachEntry(new TIntObjectProcedure<Object>() {

				@Override
				public boolean execute(int keyIndex, Object value) {
					string.append("\n\t  KEY: " + SteffiGraph.getInstance().getItemName(keyIndex) 
							+ " VALUE: " + value);
					return true;
				}
			});
			

		}
		
		
		return string.toString();
	}


	@Override
	public Cell clone() {
		SteffiEdge clon =  (SteffiEdge) super.clone();
		
		clon.destCellId = this.destCellId;
		clon.sourceCellId = this.sourceCellId;
		clon.edgeType = this.edgeType;
		
		return clon;
	}


	
		

}
