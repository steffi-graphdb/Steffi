package com.imgraph.networking;




import org.zeromq.ZMQ;

import com.imgraph.common.Configuration;
import com.imgraph.common.ImgLogger;
import com.imgraph.model.ImgGraph;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.MessageType;
import com.imgraph.networking.messages.SearchEndMsg;
import com.imgraph.networking.messages.SearchReqMsg;
import com.imgraph.storage.CacheContainer;


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
		context = ImgGraph.getInstance().getZMQContext();
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
							ImgGraph.getInstance().initializeMemberIndexes();
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
						
						
						ImgGraph.getInstance().closeGraphClients();
						
						
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
