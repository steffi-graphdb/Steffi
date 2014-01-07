package com.imgraph.networking.messages;


import com.imgraph.traversal.TraversalResults;

public class TraversalRepMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6066489429282519013L;
	private TraversalResults traversalResults;
	private boolean completedSuccesfully;
	
	public TraversalRepMsg() {
		super(MessageType.TRAVERSAL_REP);
	}

	public TraversalResults getTraversalResults() {
		return traversalResults;
	}

	public void setTraversalResults(TraversalResults traversalResults) {
		this.traversalResults = traversalResults;
	}

	public boolean isCompletedSuccesfully() {
		return completedSuccesfully;
	}

	public void setCompletedSuccesfully(boolean completedSuccesfully) {
		this.completedSuccesfully = completedSuccesfully;
	}

	
	
	
}
