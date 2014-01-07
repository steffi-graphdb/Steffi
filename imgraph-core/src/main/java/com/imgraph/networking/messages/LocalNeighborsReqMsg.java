package com.imgraph.networking.messages;

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
