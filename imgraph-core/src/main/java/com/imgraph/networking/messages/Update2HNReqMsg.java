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
package com.imgraph.networking.messages;

import gnu.trove.map.TLongObjectMap;

import java.util.List;

import com.imgraph.model.ImgEdge;
import com.imgraph.model.ImgIndexedEdges;

public class Update2HNReqMsg extends IdentifiableMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1364125451822459197L;
	private TLongObjectMap<ImgIndexedEdges> local2HNUpd;
	private List<ImgEdge> removedEdges;
	
	public Update2HNReqMsg() {
		super(MessageType.UPD_2HN_TRANSACTION_REQ);
	}

	public TLongObjectMap<ImgIndexedEdges> getLocal2HNUpd() {
		return local2HNUpd;
	}

	public void setLocal2HNUpd(TLongObjectMap<ImgIndexedEdges> local2HNUpd) {
		this.local2HNUpd = local2HNUpd;
	}

	public List<ImgEdge> getRemovedEdges() {
		return removedEdges;
	}

	public void setRemovedEdges(List<ImgEdge> removedEdges) {
		this.removedEdges = removedEdges;
	}
	


}
