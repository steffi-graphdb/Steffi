package com.imgraph.networking.messages;

import java.util.List;


public class AddressVertexReqMsg extends Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1803465808643110411L;
	private List<Long> cellIds;

	public List<Long> getCellIds() {
		return cellIds;
	}

	public void setCellIds(List<Long> cellIds) {
		this.cellIds = cellIds;
	}

	public AddressVertexReqMsg() {
		super(MessageType.ADDRESS_VERTEX_REQ);
	}

	public AddressVertexReqMsg(String body) {
		super(MessageType.ADDRESS_VERTEX_REQ, body);
	}

	
	
	
}
