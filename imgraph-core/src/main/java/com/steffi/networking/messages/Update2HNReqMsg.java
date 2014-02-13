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

import gnu.trove.map.TLongObjectMap;

import java.util.List;

import com.steffi.model.SteffiEdge;
import com.steffi.model.SteffiIndexedEdges;

public class Update2HNReqMsg extends IdentifiableMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1364125451822459197L;
	private TLongObjectMap<SteffiIndexedEdges> local2HNUpd;
	private List<SteffiEdge> removedEdges;
	
	public Update2HNReqMsg() {
		super(MessageType.UPD_2HN_TRANSACTION_REQ);
	}

	public TLongObjectMap<SteffiIndexedEdges> getLocal2HNUpd() {
		return local2HNUpd;
	}

	public void setLocal2HNUpd(TLongObjectMap<SteffiIndexedEdges> local2HNUpd) {
		this.local2HNUpd = local2HNUpd;
	}

	public List<SteffiEdge> getRemovedEdges() {
		return removedEdges;
	}

	public void setRemovedEdges(List<SteffiEdge> removedEdges) {
		this.removedEdges = removedEdges;
	}
	


}
