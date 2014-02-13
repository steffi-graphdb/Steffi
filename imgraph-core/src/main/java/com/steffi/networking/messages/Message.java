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

import java.io.IOException;
import java.io.Serializable;

import com.steffi.common.CommonTools;




/**
 * @author Aldemar Reynaga
 * Defines the base class for the Mesages, any message of Imgraph must inherit from this class
 */
public class Message implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2385777220652982426L;

	protected MessageType type;
	
	protected String body;
	
	private static final int BUFFER_SIZE = 1024*1024*10;
	
	
	
	public void setType(MessageType type) {
		this.type = type;
	}





	
	public Message(MessageType type) {
		this.type = type;
	
	}
	
	public Message(MessageType type, String body) {
		this.type = type;
		this.body = body;
	}
	
	
	public String getBody() {
		return body;
	}




	public void setBody(String body) {
		this.body = body;
	}


	public MessageType getType() {
		return type;
	}


	

	@Override
	public String toString() {
		return "T:" + type + " C:" + this.body;
	}


	public static Message readFromBytes(byte [] msg) throws IOException {
		return (Message) CommonTools.readFromBytes(msg);
	}
	
	public static byte[] convertMessageToBytes(Message message) throws IOException {
		return CommonTools.convertObjectToBytes(message);
	}
	
	
}
