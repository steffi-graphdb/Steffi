package com.imgraph.tests.titan;

import java.io.Serializable;

public class CassandraStartMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8072326715661388843L;
	private final String tempDirectory;
	private final String cassandraDirectory; 
	private final int numberOfNodes;
	private final int nodeNumber;
	private final String mainNodeIp; 
	private final String localIpAddress;
	private final boolean startInBatchMode;
	
	public CassandraStartMsg(String tempDirectory, String cassandraDirectory,
			int numberOfNodes, int nodeNumber, String mainNodeIp,
			String localIpAddress, boolean startInBatchMode) {
		this.startInBatchMode = startInBatchMode;
		this.tempDirectory = tempDirectory;
		this.cassandraDirectory = cassandraDirectory;
		this.numberOfNodes = numberOfNodes;
		this.nodeNumber = nodeNumber;
		this.mainNodeIp = mainNodeIp;
		this.localIpAddress = localIpAddress;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	
	public int getNodeNumber() {
		return nodeNumber;
	}

	
	public String getMainNodeIp() {
		return mainNodeIp;
	}

	
	public String getLocalIpAddress() {
		return localIpAddress;
	}

	
	public String getTempDirectory() {
		return tempDirectory;
	}

	public String getCassandraDirectory() {
		return cassandraDirectory;
	}

	public boolean isStartInBatchMode() {
		return startInBatchMode;
	}
	
	
	
}
