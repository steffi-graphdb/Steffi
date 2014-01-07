package com.imgraph.networking.messages;

import java.util.List;
import java.util.UUID;

import com.imgraph.traversal.SearchVertexInfo;

public class SearchInfoMsg extends Message{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7727798145437936821L;
	private UUID searchId;
	
	private String address;
	
	private List<SearchVertexInfo> verticesInfo;
	
	public SearchInfoMsg() {
		super(MessageType.SEARCH_INFO);
	}
	
	public UUID getSearchId() {
		return searchId;
	}
	public void setSearchId(UUID searchId) {
		this.searchId = searchId;
	}
	
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<SearchVertexInfo> getVerticesInfo() {
		return verticesInfo;
	}

	public void setVerticesInfo(List<SearchVertexInfo> verticesInfo) {
		this.verticesInfo = verticesInfo;
	}

	
	

}
