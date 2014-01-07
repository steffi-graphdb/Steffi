package com.imgraph.networking.messages;

import gnu.trove.set.TLongSet;

import java.util.List;
import java.util.UUID;

import com.imgraph.traversal.EdgeTraversalConf;
import com.imgraph.traversal.MatchEvaluatorConf;
import com.imgraph.traversal.ReducedVertexPath;

public class SearchReqMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5126977600635297750L;
	
	
	private List<ReducedVertexPath> vertexPaths;
	private TLongSet explored2HNeighbors;
	
	private UUID searchId;
	private UUID searchMsgId;
	private int managerIndex;
	
	private int maxHops;
	private MatchEvaluatorConf matchConf;
	private List<EdgeTraversalConf> traversalConfs;
	
	
	
	//private List<VertexIdDepth> lastExplored;
	//private TLongIntMap lastExplored;
	
	
	
	public SearchReqMsg() {
		super(MessageType.SEARCH_REQ);
		//vertexPaths = new ArrayList<ReducedVertexPath>();
		//lastExplored = new ArrayList<VertexIdDepth>();
	}
	/*
	public SearchReqMsg(String body) {
		super(MessageType.SEARCH_REQ, body);
	}
	*/
	
	public void setAttributes(List<ReducedVertexPath> vertexPaths,
			TLongSet explored2HNeighbors, UUID searchId, UUID searchMsgId,
			int maxHops, MatchEvaluatorConf matchConf,
			List<EdgeTraversalConf> traversalConfs) {
		this.vertexPaths = vertexPaths;
		this.explored2HNeighbors = explored2HNeighbors;
		this.searchId = searchId;
		this.searchMsgId = searchMsgId;
		this.maxHops = maxHops;
		this.matchConf = matchConf;
		this.traversalConfs = traversalConfs;
	}




	public int getMaxHops() {
		return maxHops;
	}

	public void setMaxHops(int maxHops) {
		this.maxHops = maxHops;
	}

	public MatchEvaluatorConf getMatchConf() {
		return matchConf;
	}

	public void setMatchConf(MatchEvaluatorConf matchConf) {
		this.matchConf = matchConf;
	}

	public UUID getSearchId() {
		return searchId;
	}

	public void setSearchId(UUID searchId) {
		this.searchId = searchId;
	}

	
	public List<EdgeTraversalConf> getTraversalConfs() {
		return traversalConfs;
	}

	public void setTraversalConfs(List<EdgeTraversalConf> traversalConfs) {
		this.traversalConfs = traversalConfs;
	}
	
	
	public UUID getSearchMsgId() {
		return searchMsgId;
	}


	public void setSearchMsgId(UUID searchMsgId) {
		this.searchMsgId = searchMsgId;
	}


	@Override
	public String toString() {
		return super.toString() + ":: SearchReqMsg [searchedVertices=" + vertexPaths
				+ ", searchId=" + searchId + ", \n searchMsgId=" + searchMsgId
				+ ", maxHops=" + maxHops + ", \n matchConf=" + matchConf
				+ ", traversalConfs=" + traversalConfs + ", \n explored2H=" + explored2HNeighbors + "]";
	}


	

	public List<ReducedVertexPath> getVertexPaths() {
		return vertexPaths;
	}


	public void setVertexPaths(List<ReducedVertexPath> vertexPaths) {
		this.vertexPaths = vertexPaths;
	}


	public TLongSet getExplored2HNeighbors() {
		return explored2HNeighbors;
	}


	public void setExplored2HNeighbors(TLongSet explored2HNeighbors) {
		this.explored2HNeighbors = explored2HNeighbors;
	}

	public int getManagerIndex() {
		return managerIndex;
	}

	public void setManagerIndex(int managerIndex) {
		this.managerIndex = managerIndex;
	}
	
}
