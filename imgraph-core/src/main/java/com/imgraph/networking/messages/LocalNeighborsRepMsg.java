package com.imgraph.networking.messages;

import java.util.HashMap;
import java.util.Map;

import com.imgraph.model.ImgIndexedEdges;

public class LocalNeighborsRepMsg extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3673369718973597416L;
	private Map<Long, ImgIndexedEdges> vertexEdgeMap;
	
	public LocalNeighborsRepMsg() {
		super(MessageType.LOCAL_NEIGHBORS_REP);
		vertexEdgeMap = new HashMap<Long, ImgIndexedEdges>();
	}

	public Map<Long, ImgIndexedEdges> getVertexEdgeMap() {
		return vertexEdgeMap;
	}

	public void setVertexEdgeMap(Map<Long, ImgIndexedEdges> vertexEdgeMap) {
		this.vertexEdgeMap = vertexEdgeMap;
	}

		

		
}
