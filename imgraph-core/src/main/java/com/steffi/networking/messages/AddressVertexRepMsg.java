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
