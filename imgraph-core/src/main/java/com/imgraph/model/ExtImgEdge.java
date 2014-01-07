package com.imgraph.model;

import java.io.Serializable;



/**
 * @author Aldemar Reynaga
 * Models an edge including a bit flag that indicates if there is a virtual edge
 */
public class ExtImgEdge extends ImgEdge implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6036619926344459982L;
	private byte neighborFlag;
	
	
	
	
	public ExtImgEdge(long sourceCellId,
			long destCellId,
			EdgeType edgeType,
			String name,
			Long edgeCellId) {
		super(sourceCellId, destCellId, edgeType, name, edgeCellId);
		this.neighborFlag = 0;
	}

	public ExtImgEdge(ImgEdge edge) {
		super(edge.getSourceCellId(), edge.getDestCellId(), edge.getEdgeType(),
				edge.getName(), edge.getId());
		this.neighborFlag = 0;
	}
	
	public ExtImgEdge(long id, String name) {
		super(id, name);
	}
	
	
	public byte getNeighborFlag() {
		return neighborFlag;
	}
	public void setNeighborFlag(byte neighborFlag) {
		this.neighborFlag = neighborFlag;
	}
	
	
	
	
	@Override
	public Cell clone() {
		ExtImgEdge clon = (ExtImgEdge) super.clone();
		clon.neighborFlag = this.neighborFlag;
		return clon;
	}

	@Override
	public String toString() {
		return "[" + super.toString() + " , " + neighborFlag + "]"; 
	}

	
	
}
