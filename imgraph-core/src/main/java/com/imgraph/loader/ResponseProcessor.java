package com.imgraph.loader;

import com.imgraph.networking.messages.Message;

/**
 * @author Aldemar Reynaga
 * Defines the callback function to be called when the server receives a response for a previous sent message  
 */
public interface ResponseProcessor {

	public void processResponse(Message message);
}
