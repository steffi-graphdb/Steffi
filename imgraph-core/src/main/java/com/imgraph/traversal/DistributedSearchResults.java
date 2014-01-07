package com.imgraph.traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Aldemar Reynaga
 * Results for a distributed traversal request
 */
public class DistributedSearchResults {
	private TraversalResults traversalresults;
	private List<UUID> sentSearchMsgIds;
	
	public DistributedSearchResults() {
		traversalresults = new TraversalResultsImpl();
		sentSearchMsgIds =  new ArrayList<UUID>();
	}
	
	
	public TraversalResults getTraversalresults() {
		return traversalresults;
	}
	public void setTraversalresults(TraversalResults traversalresults) {
		this.traversalresults = traversalresults;
	}
	public List<UUID> getSentSearchMsgIds() {
		return sentSearchMsgIds;
	}
	public void setSentSearchMsgIds(List<UUID> sentSearchMsgIds) {
		this.sentSearchMsgIds = sentSearchMsgIds;
	}
	
	
	
}
