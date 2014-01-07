package com.imgraph.traversal;

import java.util.UUID;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.imgraph.common.Configuration;
import com.imgraph.networking.messages.Message;

/**
 * @author Aldemar Reynaga
 * Senders of asynchronous messages used for the comunication among the data servers during a traversal
 * execution
 */
public class SearchClientThread implements Runnable {
	private Socket socket;
	private boolean alive;
	private Object lock;
	
	
	public SearchClientThread(ZMQ.Context context, String ipAddress, String address, String searchWorkerId) {

		socket = context.socket(ZMQ.DEALER);
		
		
		String identity =  searchWorkerId + "_" + address + "_" + UUID.randomUUID();
		socket.setIdentity(identity.getBytes());
		socket.connect("tcp://" + ipAddress + ":" + Configuration.getProperty(Configuration.Key.NODE_PORT));

		lock = new String(identity);
		
	
	
	}
	
	
	public synchronized void sendMessage(Message message) throws Exception {
		
		socket.send(Message.convertMessageToBytes(message), 0);
	}
	
	public void close() {
		this.socket.close();
	}
	

	public void stop() {
		synchronized (lock) {
			alive = false;
			lock.notifyAll();
		}
		
	}

	@Override
	public void run() {
		
		alive = true;

		while (alive) {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} 
		this.socket.close();
		


	}

}
