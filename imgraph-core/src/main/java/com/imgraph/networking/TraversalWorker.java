package com.imgraph.networking;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.imgraph.networking.messages.InitTraversalMsg;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.SearchRepMsg;
import com.imgraph.networking.messages.TraversalRepMsg;
import com.imgraph.traversal.TraversalManager;


/**
 * @author Aldemar Reynaga
 * Receives and process the traversal messages
 */
public class TraversalWorker implements Runnable {

	private Context context;
	private boolean alive;
	final private int id;
	private TraversalManager traversalManager;
	
	
	
	public TraversalWorker(Context context, int id) {
		super();
		this.id = id;
		this.context = context;
		traversalManager =  new TraversalManager(context, id);
	}
	
	
	public void processRepMessage(SearchRepMsg searchRepMsg) {
		traversalManager.registerSearchRepMsg(searchRepMsg);
	}
	
	public void stop() {
		traversalManager.closeClientThreads();
		alive = false;
	}
	
	@Override
	public void run() {
		Socket worker = context.socket(ZMQ.REP);
		
		InitTraversalMsg initTraversalMsg = null;
		TraversalRepMsg traversalRepMsg = null;
		worker.connect("inproc://backend_traversal_" + id);
		

		
		alive = true;
		try {
			
			
			while (alive) {
				byte msg[] = worker.recv(0);
				
				initTraversalMsg = (InitTraversalMsg) Message.readFromBytes(msg);
				
				
				try {
					traversalManager.init(initTraversalMsg.getSearchId(), initTraversalMsg.getTraversalReqMsg().getVertexId(), 
							initTraversalMsg.getTraversalReqMsg().getMaxHops(), initTraversalMsg.getTraversalReqMsg().getMatchConf(), 
							initTraversalMsg.getTraversalReqMsg().getTraversalConfs(), initTraversalMsg.getTraversalReqMsg().getNodeIp(),
							initTraversalMsg.getTraversalReqMsg().getManagerIndex());
					traversalRepMsg = new TraversalRepMsg();
					traversalRepMsg.setTraversalResults(traversalManager.traverse());
					traversalRepMsg.setCompletedSuccesfully(true);
				} catch (Exception ex) {
					ex.printStackTrace();
					traversalRepMsg.setCompletedSuccesfully(false);
				}
				
				
				
				worker.send(Message.convertMessageToBytes(traversalRepMsg), 0);
				
				
				
			}
		} catch (Exception x) {
			if (alive)
				x.printStackTrace();
		} finally {
			worker.close();
		}
	}

}
