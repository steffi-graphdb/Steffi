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
package com.imgraph.networking;

import java.io.IOException;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.imgraph.networking.messages.LoadMessage;
import com.imgraph.networking.messages.Message;

/**
 * @author Aldemar Reynaga
 * Manages a pool of load worker processes
 */
public class LoadWorkerManager {
	private ZMQ.Socket[] loadBackends;
	private LoadWorker[] loadWorkers;
	
	
	public void init(Context context, Poller poller, int numLoaders) {
		loadBackends = new ZMQ.Socket[numLoaders];
		loadWorkers = new LoadWorker[numLoaders];
				
		
		for (int j=0; j<numLoaders; j++) {
			loadBackends[j] = context.socket(ZMQ.DEALER);
			loadBackends[j].bind("inproc://load_backend_" + j);
			loadWorkers[j] = new LoadWorker(context, j);
			(new Thread(loadWorkers[j])).start();
			poller.register(loadBackends[j], ZMQ.Poller.POLLIN);
		}
	}
	
	public void sendToLoader(byte[] id, byte []msg) throws IOException {
		if (loadWorkers.length == 1) {
			loadBackends[0].send(id, ZMQ.SNDMORE);
			loadBackends[0].send(new byte[0], ZMQ.SNDMORE);
			loadBackends[0].send(msg, 0);
		} else {
			LoadMessage message = (LoadMessage) Message.readFromBytes(msg);
			loadBackends[message.getLoaderIndex()].send(id, ZMQ.SNDMORE);
			loadBackends[message.getLoaderIndex()].send(new byte[0], ZMQ.SNDMORE);
			loadBackends[message.getLoaderIndex()].send(msg, 0);
		}
		
	}
	
	public void sendToFrontend(int index, Socket frontend) {
		
		byte []id = loadBackends[index].recv(0);
		loadBackends[index].recv(0);
		byte []msg = loadBackends[index].recv(0);
		frontend.send(id, ZMQ.SNDMORE);
		frontend.send(msg, 0);
	}
	
	
	public void stop() {
		
		for (int i=0; i<loadWorkers.length; i++) {
			loadWorkers[i].stop();
			loadBackends[i].close();
		}
		
		
	}
	
	
}
