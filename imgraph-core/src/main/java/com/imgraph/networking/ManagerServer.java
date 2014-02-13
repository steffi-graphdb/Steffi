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

import java.net.InetAddress;

import org.zeromq.ZMQ;

import com.imgraph.common.Configuration;
import com.imgraph.common.ImgLogger;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.SearchRepMsg;
import com.imgraph.networking.messages.TraversalReqMsg;

/**
 * @author Aldemar Reynaga
 * Starts the thread running a traversal manager server
 */
public class ManagerServer implements Runnable {


	private TraversalWorkerManager traversalWorkerManagerV3;

	private ZMQ.Context context;
	
	public ManagerServer() {
		traversalWorkerManagerV3 = new TraversalWorkerManager();
		context = ZMQ.context(1);
		
	}
	
	
	@Override
	public void run() {
		Message message = null;
		boolean stop = false;
		byte id[], msg [];
		
		ZMQ.Socket frontend = null;
		
		
		
		int numTraversalWorkers = Integer.parseInt(Configuration.getProperty(Configuration.Key.NUM_TRAVERSAL_WORKERS));
		
		frontend = context.socket(ZMQ.ROUTER);
		System.out.println("tcp://*:" + Configuration.getProperty(Configuration.Key.MANAGER_PORT));
		frontend.bind("tcp://*:" + Configuration.getProperty(Configuration.Key.MANAGER_PORT));
		
		
		
		ZMQ.Poller poller = new ZMQ.Poller(1 + numTraversalWorkers);
				
		
		poller.register(frontend, ZMQ.Poller.POLLIN);
		
		traversalWorkerManagerV3.init(context, poller, numTraversalWorkers);
		
		
		
		
		try {
			System.out.println("ZMQ Manager server started on " + InetAddress.getLocalHost().getHostAddress() + 
					" port " + Configuration.getProperty(Configuration.Key.MANAGER_PORT));
			
			
			do {
				poller.poll();
				if (poller.pollin(0)) {
					id = frontend.recv(0);
					
					msg = frontend.recv(0);
					
					
					
					//In case we got a message from a REQ socket
					if (frontend.hasReceiveMore()) 
						msg = frontend.recv(0);
					
					message = Message.readFromBytes(msg);
					
					
					switch (message.getType()) {
					
					case TRAVERSAL_REQ:
						traversalWorkerManagerV3.initTraversal(id, (TraversalReqMsg) message);
						break;
						
					case SEARCH_REP:
						traversalWorkerManagerV3.notifyTraversalWorker((SearchRepMsg) message);
						break;
						
					case STOP:
						traversalWorkerManagerV3.stop();
						Thread.sleep(1000);
						stop = true;
						break;
					default:
						break;
					
					}

				}

				
				for (int i=0; i<numTraversalWorkers; i++) {
					if (poller.pollin(i+1)) {
						
						traversalWorkerManagerV3.freeTraversalWorker(i);
						traversalWorkerManagerV3.sendToFrontend(i, frontend);
					}
					
				}
				
				

			} while (!stop);
		} catch (Exception x) {
			ImgLogger.logError(x, "Error on node server");
		} finally {
			frontend.close();
			context.term();
		}
		
	}
	
		
	
}
