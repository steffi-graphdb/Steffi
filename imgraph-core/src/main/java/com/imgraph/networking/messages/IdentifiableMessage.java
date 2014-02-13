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

public class IdentifiableMessage extends Message implements Identifiable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4450328866757992762L;

	private UUID id;
	
	
	public IdentifiableMessage(MessageType type) {
		super(type);
	}


	@Override
	public UUID getId() {
		return id;
	}


	@Override
	public void setId(UUID id) {
		this.id = id;		
	}
	
	

}
