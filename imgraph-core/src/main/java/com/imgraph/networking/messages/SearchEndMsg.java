package com.imgraph.networking.messages;

import java.util.UUID;



public class SearchEndMsg extends Message{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8608841241202036456L;

	public SearchEndMsg() {
		super(MessageType.END_SEARCH);
	}

	private UUID searchId;

	public UUID getSearchId() {
		return searchId;
	}

	public void setSearchId(UUID searchId) {
		this.searchId = searchId;
	}

	
	
	
}
