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
