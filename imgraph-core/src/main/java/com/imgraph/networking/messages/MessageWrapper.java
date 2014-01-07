package com.imgraph.networking.messages;

import java.io.Serializable;

public class MessageWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -801066558051920391L;
	private Message message;

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
	
	
	
}
