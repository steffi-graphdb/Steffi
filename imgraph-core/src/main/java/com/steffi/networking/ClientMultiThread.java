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
package com.steffi.networking;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.steffi.common.Configuration;
import com.steffi.loader.ResponseProcessor;
import com.steffi.model.SteffiGraph;
import com.steffi.networking.messages.Identifiable;
import com.steffi.networking.messages.IdentifiableMessage;
import com.steffi.networking.messages.Message;

/**
 * @author Aldemar Reynaga
 * A general purpose sender of asynchronous messages, the sender can relate a received response message  with
 * the sent message that triggered that response. It is possible to register a callback function that will be called
 * upon the arrival of a response.  
 */
public class ClientMultiThread implements Runnable {

	
	private Context context;
	private Socket socket;
	private ZMQ.Poller poller;
	private Queue<IdentifiableMessage> messageQueue;
	private boolean alive;
	
	private Map<UUID, ResponseProcessor> pendingResponses;
	
	public ClientMultiThread(String ipAddress, String address) {
		
		context = SteffiGraph.getInstance().getZMQContext();
		socket = context.socket(ZMQ.DEALER);
		String identity =  UUID.randomUUID().toString();
		socket.setIdentity(identity.getBytes());
		socket.connect("tcp://" + ipAddress + ":" + 
				Configuration.getProperty(Configuration.Key.NODE_PORT));
		
		poller = context.poller(1);
		poller.register(socket, ZMQ.Poller.POLLIN);
		
		messageQueue = new ConcurrentLinkedQueue<IdentifiableMessage>();
		pendingResponses = new ConcurrentHashMap<UUID, ResponseProcessor>();
	}

	public void addMsgToQueue(IdentifiableMessage message, ResponseProcessor sender) {
		pendingResponses.put(message.getId(), sender);
		messageQueue.add(message);
	}
	
	public void stop() {
		alive = false;
	}
	
	@Override
	public void run() {

		
		alive = true;
		
		try {
			while (alive) {
				poller.poll(5);
				if (poller.pollin(0)) {
					byte [] msg = socket.recv(0);
					
					IdentifiableMessage message = (IdentifiableMessage) Message.readFromBytes(msg);
					
					ResponseProcessor responseProcessor = pendingResponses.remove(message.getId());
					
					responseProcessor.processResponse(message);
				}
				
				//Send from queue
				if (!messageQueue.isEmpty()) {
					Identifiable mq = messageQueue.poll();
					
					socket.send(Message.convertMessageToBytes((Message) mq), 0);
					
					
				}
				
			}
		} catch (Exception x) {
			if (alive)
				throw new RuntimeException(x);
		} finally {
			this.socket.close();

		}
		

		
	}
}
