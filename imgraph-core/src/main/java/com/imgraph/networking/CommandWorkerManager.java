package com.imgraph.networking;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.imgraph.common.Configuration;

/**
 * @author Aldemar Reynaga
 * Manages a pool of command worker processes
 */
public class CommandWorkerManager {
	
	private ZMQ.Socket backendSyncCommand;
	private ZMQ.Socket backendAsyncCommand;
	private CommandWorker[] asyncCommandWorkers;
	private CommandWorker[] syncCommandWorkers;
	
	public void init(Context context, Poller poller) {
		
		int numSyncWorkers = Integer.parseInt(Configuration.getProperty(Configuration.Key.NUM_SYNC_COMMAND_WORKERS));
		int numAsyncWorkers = Integer.parseInt(Configuration.getProperty(Configuration.Key.NUM_ASYNC_COMMAND_WORKERS));
		
		backendSyncCommand = context.socket(ZMQ.DEALER);
		backendSyncCommand.bind("inproc://backend_sync_command");
		backendAsyncCommand = context.socket(ZMQ.DEALER);
		backendAsyncCommand.bind("inproc://backend_async_command");
		
		
		poller.register(backendSyncCommand, ZMQ.Poller.POLLIN);
		poller.register(backendAsyncCommand, ZMQ.Poller.POLLIN);
		
		syncCommandWorkers = new CommandWorker[numSyncWorkers];
		for (int i=0; i<numSyncWorkers; i++) {
			syncCommandWorkers[i] = new CommandWorker(context, "inproc://backend_sync_command"); 
			(new Thread(syncCommandWorkers[i])).start();
		}
		
		asyncCommandWorkers = new CommandWorker[numAsyncWorkers];
		for (int i=0; i<numAsyncWorkers; i++) {
			asyncCommandWorkers[i] = new CommandWorker(context, "inproc://backend_async_command"); 
			(new Thread(asyncCommandWorkers[i])).start();
		}
	}
	
	
	public void sendToCommandWorker(byte[] id, byte []msg, boolean msgFromReq) {
		Socket socket = (msgFromReq)?backendSyncCommand:backendAsyncCommand;
		
		socket.send(id, ZMQ.SNDMORE);
		socket.send(new byte[0], ZMQ.SNDMORE);
		socket.send(msg, 0);
	}
	
	public void sentToFrontend(Socket frontend, boolean msgFromReq) {
		Socket socket = (msgFromReq)?backendSyncCommand:backendAsyncCommand;
		
		byte []id = socket.recv(0);
		byte []empty = socket.recv(0);
		byte []msg = socket.recv(0);
		
		frontend.send(id, ZMQ.SNDMORE);
		if (msgFromReq)
			frontend.send(empty, ZMQ.SNDMORE);
		frontend.send(msg, 0);
		
		
		
	}
	
	public void stop() {
		
		for (CommandWorker cm : syncCommandWorkers)
			cm.stop();
		
		for (CommandWorker cm : asyncCommandWorkers)
			cm.stop();
		
		backendAsyncCommand.close();
		backendSyncCommand.close();
	}
	
	
	
}
