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
package com.steffi.index;

import java.io.Serializable;

/**
 * @author Aldemar Reynaga
 * Identifies uniquely and edge to be stored in the ImgMapIndex implementation 
 */
public class EdgeIndexEntry implements Serializable {
	
	private static final long serialVersionUID = -3905505166572320331L;
	private final long cellId;
	private final long edgeId;
	
	public EdgeIndexEntry(long cellId, long edgeId) {
		super();
		this.cellId = cellId;
		this.edgeId = edgeId;
	}
	
	public long getCellId() {
		return cellId;
	}
	
	public long getEdgeId() {
		return edgeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cellId ^ (cellId >>> 32));
		result = prime * result + (int) (edgeId ^ (edgeId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeIndexEntry other = (EdgeIndexEntry) obj;
		if (cellId != other.cellId)
			return false;
		if (edgeId != other.edgeId)
			return false;
		return true;
	}

	
	
	
	
}
