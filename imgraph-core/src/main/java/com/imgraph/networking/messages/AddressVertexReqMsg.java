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
