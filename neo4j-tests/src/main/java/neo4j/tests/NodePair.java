package neo4j.tests;

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