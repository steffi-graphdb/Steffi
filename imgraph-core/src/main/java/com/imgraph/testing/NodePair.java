package com.imgraph.testing;

/**
 * @author Aldemar Reynaga
 * A pair of nodes representing the start and end vertex ids
 */
public class NodePair {

	private long nodeAId;
	private long nodeBId;

	public NodePair(long nodeAId, long nodeBId) {
		this.nodeAId = nodeAId;
		this.nodeBId = nodeBId;
	}

	public long getNodeAId() {
		return nodeAId;
	}

	public long getNodeBId() {
		return nodeBId;
	}

	
	
}