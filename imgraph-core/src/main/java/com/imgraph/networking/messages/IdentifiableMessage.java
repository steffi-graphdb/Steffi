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
