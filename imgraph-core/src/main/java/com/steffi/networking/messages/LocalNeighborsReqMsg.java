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
package com.steffi.networking.messages;

import gnu.trove.set.TLongSet;



public class LocalNeighborsReqMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7271408048526772703L;
	private TLongSet vertexIds;
	private String localAddress;
	
	public String getLocalAddress() {
		return localAddress;
	}


	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}


	public LocalNeighborsReqMsg() {
		super(MessageType.LOCAL_NEIGHBORS_REQ);
	}


	public TLongSet getVertexIds() {
		return vertexIds;
	}


	public void setVertexIds(TLongSet vertexIds) {
		this.vertexIds = vertexIds;
	}


	
}
