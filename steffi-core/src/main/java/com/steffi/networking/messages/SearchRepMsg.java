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

import java.util.List;
import java.util.UUID;

import com.steffi.traversal.TraversalResults;

public class SearchRepMsg extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5684996961923348500L;
	private UUID searchId;
	private UUID searchMsgId;
	
	private boolean ok;
	
	private List<UUID> sentSearchMsgIds;
	
	private TraversalResults traversalResults;
	
	public SearchRepMsg() {
		super(MessageType.SEARCH_REP);
	}

	public SearchRepMsg(String body) {
		super(MessageType.SEARCH_REP, body);
	}
	
	
	public void setAttributes(UUID searchId, UUID searchMsgId,
			boolean ok, List<UUID> sentSearchMsgIds,
			TraversalResults traversalResults) {
		this.searchId = searchId;
		this.searchMsgId = searchMsgId;
		this.ok = ok;
		this.sentSearchMsgIds = sentSearchMsgIds;
		this.traversalResults = traversalResults;
	}




	public List<UUID> getSentSearchMsgIds() {
		return sentSearchMsgIds;
	}


	public void setSentSearchMsgIds(List<UUID> sentSearchMsgIds) {
		this.sentSearchMsgIds = sentSearchMsgIds;
	}


	public UUID getSearchId() {
		return searchId;
	}

	public void setSearchId(UUID searchId) {
		this.searchId = searchId;
	}

	public TraversalResults getTraversalResults() {
		return traversalResults;
	}

	public void setTraversalResults(TraversalResults traversalResults) {
		this.traversalResults = traversalResults;
	}

	
	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public UUID getSearchMsgId() {
		return searchMsgId;
	}

	public void setSearchMsgId(UUID searchMsgId) {
		this.searchMsgId = searchMsgId;
	}

	@Override
	public String toString() {
		return  super.toString() +  " :: SearchRepMsg [searchId=" + searchId + ", searchMsgId="
				+ searchMsgId  
				+ ", ok=" + ok + ", searchMsgIdsSent=" + sentSearchMsgIds
				+ ", traversalResults=" + traversalResults + "]";
	}

	
}
