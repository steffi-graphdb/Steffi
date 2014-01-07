package com.imgraph.networking;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.imgraph.common.Configuration;
import com.imgraph.loader.ResponseProcessor;
import com.imgraph.networking.messages.Message;


/**
 * @author Aldemar Reynaga
 * A sender of asynchronous messages able to process responses through a callback function passed as an argument using
 * an interface. This sender does not distinguish between the message responses that arrive
 */
public class ClientThread implements Runnable{
	private Context context;
	private Socket socket;
	private ZMQ.Poller poller;
	private Queue<Message> messageQueue;
	private boolean alive;
	private ResponseProcessor responseProcessor;

	
	
	public ClientThread(String ipAddress, String address, String loadMngId,
			ResponseProcessor loadResponseProcessor) {
		
		context = ZMQ.context(1);
		socket = context.socket(ZMQ.DEALER);
		String identity =  loadMngId + "_" + address;
		socket.setIdentity(identity.getBytes());
		socket.connect("tcp://" + ipAddress + ":" + 
				Configuration.getProperty(Configuration.Key.NODE_PORT));
		
		poller = context.poller(1);
		poller.register(socket, ZMQ.Poller.POLLIN);
		
		messageQueue = new ConcurrentLinkedQueue<Message>();
		this.responseProcessor = loadResponseProcessor;
	}

	public void addMsgToQueue(Message message) {
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
					
					Message message = Message.readFromBytes(msg);
					
					responseProcessor.processResponse(message);
					
					
				}
				
				//Send from queue
				if (!messageQueue.isEmpty()) {
					Message mq = messageQueue.poll();
					
					socket.send(Message.convertMessageToBytes(mq), 0);
					
					
				}
				
			}
		} catch (Exception x) {
			if (alive)
				throw new RuntimeException(x);
		} finally {
			this.socket.close();
			this.context.term();
		}
		
		
	}
}
