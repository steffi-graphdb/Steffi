package com.imgraph.networking.messages;

import java.util.UUID;



public class InitTraversalMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5321280521449920717L;
	private UUID searchId;
	private TraversalReqMsg traversalReqMsg;
	
	
	public InitTraversalMsg() {
		super(MessageType.INIT_TRAVERSAL);
	}

	public TraversalReqMsg getTraversalReqMsg() {
		return traversalReqMsg;
	}

	public void setTraversalReqMsg(TraversalReqMsg traversalReqMsg) {
		this.traversalReqMsg = traversalReqMsg;
	}

	public UUID getSearchId() {
		return searchId;
	}

	public void setSearchId(UUID searchId) {
		this.searchId = searchId;
	}

	@Override
	public String toString() {
		return super.toString() + traversalReqMsg.toString();
	}

	

}
