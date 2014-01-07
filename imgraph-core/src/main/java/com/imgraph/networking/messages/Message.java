package com.imgraph.networking.messages;

import java.io.IOException;
import java.io.Serializable;

import com.imgraph.common.CommonTools;




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
