package com.imgraph.traversal;

import java.io.IOException;
import java.util.Date;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.imgraph.common.CommonTools;
import com.imgraph.model.ImgGraph;
import com.imgraph.model.ImgVertex;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.TraversalRepMsg;
import com.imgraph.networking.messages.TraversalReqMsg;

/**
 * @author Aldemar Reynaga
 * The point of access to the traversal engine 
 */
public class DistributedTraversal extends SimpleTraversal {

	private MatchEvaluatorConf matchEvaluatorConf;
	private Context context;
	private Socket requester;
	private int managerIndex;
	
	public DistributedTraversal() {
		ImgGraph graph = ImgGraph.getInstance();
		context = graph.getZMQContext();
		// Socket to talk to server
		requester = context.socket(ZMQ.REQ);
		
		
		managerIndex = graph.getNextTraversalManagerIndex();
		
				
		requester.connect("tcp://" + graph.getTraversalManagerIps()[managerIndex]);
		
	}
	
	public void close() {
		requester.close();
		
	}
	
	public void setMatchEvaluatorConf(MatchEvaluatorConf matchEvaluatorConf) {
		this.matchEvaluatorConf = matchEvaluatorConf;
	}

	private TraversalReqMsg prepareRequestMsg(ImgVertex vertex) throws IOException {
		TraversalReqMsg traversalReqMsg = new TraversalReqMsg();
		
		
		traversalReqMsg.setMaxHops(hops);
		traversalReqMsg.setTraversalConfs(edgeTraversalConfs);
		traversalReqMsg.setVertexId(vertex.getId());
		traversalReqMsg.setMatchConf(matchEvaluatorConf);
		traversalReqMsg.setNodeIp(CommonTools.getLocalIP());
		traversalReqMsg.setManagerIndex(managerIndex);
		
		
		return traversalReqMsg;
	}
	
	private TraversalRepMsg readTraversalRepMsg(byte [] msg) throws IOException, ClassNotFoundException {
		return (TraversalRepMsg) Message.readFromBytes(msg);
		
	}
	
	
	
	
	@Override
	public void addEvaluators(Evaluator... evaluators) {
		throw new UnsupportedOperationException("In distributed traversals the evaluators are " +
				"set by the match evaluator configuration");
	}

	@Override
	public TraversalResults traverse(ImgVertex startVertex) {
		TraversalResults results = null;
		Date startDate, endDate;
		// Socket to talk to server
		try {
			startDate = new Date();
			Message message = prepareRequestMsg(startVertex); 
			
			requester.send(Message.convertMessageToBytes(message), 0);
			
			
			byte[] reply = requester.recv(0);
			
			TraversalRepMsg traversalRepMsg = (TraversalRepMsg) Message.readFromBytes(reply);
			
			if (traversalRepMsg.isCompletedSuccesfully()) {
				results = readTraversalRepMsg(reply).getTraversalResults();
				endDate = new Date();
				results.setTime(endDate.getTime() - startDate.getTime());
			}
			
			
			
		} catch (Exception x) {
			x.printStackTrace();
		} 
		
		return results;
	}

	
	

}
