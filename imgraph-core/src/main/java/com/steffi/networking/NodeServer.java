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




import org.zeromq.ZMQ;

import com.steffi.common.Configuration;
import com.steffi.common.ImgLogger;
import com.steffi.model.SteffiGraph;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.networking.messages.SearchEndMsg;
import com.steffi.networking.messages.SearchReqMsg;
import com.steffi.storage.CacheContainer;


/**
 * @author Aldemar Reynaga
 * The process representing an Imgraph data server 
 */
public class NodeServer implements Runnable{
	

	private SearchWorkerManager searchWorkerManagerV5;
	private LoadWorkerManager loadWorkerManager;
	private CommandWorkerManager commandWorkerManager;
	private ZMQ.Context context;
	private long searchMsgCounter;
	
	
	public NodeServer() {
		loadWorkerManager = new LoadWorkerManager();
		commandWorkerManager = new CommandWorkerManager();
		searchWorkerManagerV5 = new SearchWorkerManager();
		searchMsgCounter = 0;
		context = SteffiGraph.getInstance().getZMQContext();
	}
	
	public long getSearchMsgCounter() {
		return searchMsgCounter;
	}
	
	@Override
	public void run() {
		Message message = null;
		boolean stop = false;
		
		byte id[], msg [];
		
		
		ZMQ.Socket frontend = null;
		
		int numLoaders = Integer.parseInt(Configuration.getProperty(Configuration.Key.NUM_LOADERS));
		
		frontend = context.socket(ZMQ.ROUTER);
		frontend.bind("tcp://*:" + Configuration.getProperty(Configuration.Key.NODE_PORT));
		
		ZMQ.Poller poller = context.poller(3 + numLoaders);
		
		poller.register(frontend, ZMQ.Poller.POLLIN);
		
		
		commandWorkerManager.init(context, poller); //Registers two backends
		
		loadWorkerManager.init(context, poller, numLoaders);
		
		searchWorkerManagerV5.init(context);
		
		
		
		System.out.println("ZMQ Server started....");
		
		try {
			do {
				poller.poll();
				if (poller.pollin(0)) {
					
					boolean msgFromReq = false;
					
					id = frontend.recv(0);
					
					msg = frontend.recv(0);
					
					//In case we got a message from a REQ socket
					if (frontend.hasReceiveMore()) {  
						msg = frontend.recv(0);
						msgFromReq = true;
					}
					
					message = Message.readFromBytes(msg);
					
					
					
					
					switch (message.getType()) {
					
					case CONFIG_CLUSTER_REQ:
						Message configResponse = new Message(MessageType.CONFIG_CLUSTER_REP);
						try {
							SteffiGraph.getInstance().initializeMemberIndexes();
							searchWorkerManagerV5.initializeClientThreads();
							
							configResponse.setBody("OK");
						} catch (Exception x) {
							configResponse.setBody("ERROR: " + x.getMessage());
							ImgLogger.logError(x, x.getMessage());
						} 
						frontend.send(id, ZMQ.SNDMORE);
						frontend.send(Message.convertMessageToBytes(configResponse), 0);
						break;
					case CLEAR:
						break;

					case LOAD_REQ:
						
						loadWorkerManager.sendToLoader(id, msg);
						break;
					case SEARCH_REQ:
						searchMsgCounter++;
						searchWorkerManagerV5.sendToSearchWorker((SearchReqMsg) message);
						break;
					case STOP:
						
						
						loadWorkerManager.stop();
						commandWorkerManager.stop();
						searchWorkerManagerV5.stop();
						
						
						SteffiGraph.getInstance().closeGraphClients();
						
						
						stop = true;
						break;
					case END_SEARCH:
						searchWorkerManagerV5.endSearch(((SearchEndMsg)message).getSearchId());
						break;
					default:
						commandWorkerManager.sendToCommandWorker(id, msg, msgFromReq);
						break;
					}
				}

				if (poller.pollin(1)) {
					commandWorkerManager.sentToFrontend(frontend, true);
				}
				
				if (poller.pollin(2)) {
					commandWorkerManager.sentToFrontend(frontend, false);
				}
				
				for (int i=0; i<numLoaders; i++) {
					if (poller.pollin(i+3)) {
						loadWorkerManager.sendToFrontend(i, frontend);
					}
				}
				
				
				
			} while (!stop);
		} catch (Exception x) {
			ImgLogger.logError(x, "Error on node server");
		} finally {
			frontend.close();
			context.term();
			
			CacheContainer.getCellCache().stop();
			CacheContainer.getCacheContainer().stop();
			
		}
		System.out.println("Main node server closed...");
		System.out.println(CacheContainer.getCacheContainer().getStatus());
	}



	public void resetCounters() {
		searchMsgCounter = 0;
	}

	
}
