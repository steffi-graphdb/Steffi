package com.imgraph.networking.messages;

import java.util.HashMap;
import java.util.Map;

public class AddressVertexRepMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2188598622383116261L;
	private Map<Long, String> cellAddresses;
	
	
	public AddressVertexRepMsg() {
		super(MessageType.ADDRESS_VERTEX_REP);
		cellAddresses = new HashMap<Long, String>();
	}


	public Map<Long, String> getCellAddresses() {
		return cellAddresses;
	}


	public void setCellAddresses(Map<Long, String> cellAddresses) {
		this.cellAddresses = cellAddresses;
	}


}
