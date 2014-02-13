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
