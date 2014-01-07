package com.imgraph.storage;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.imgraph.model.Cell;
import com.imgraph.model.ImgEdge;

/**
 * @author Aldemar Reynaga
 * Defines a group of operations inside a transaction that affect a cell. These operations will be 
 * applied on the database only on transaction commit 
 */
public class CellOperations {

	private Cell cell;
	private Set<CellOperationType> types;
	private List<ImgEdge> newEdges;
	private List<ImgEdge> removedEdges;
	private TIntObjectMap<Object> setProperties;
	private TIntList removedProperties;
	private boolean removedCell;
	
	public CellOperations(Cell cell) {
		this.cell = cell;
		this.types = new HashSet<CellOperationType>();
		this.setRemovedCell(false);
	}
	
	public void clear() {
		types.clear();
		if (newEdges != null)
			newEdges.clear();
		
		if (removedEdges != null)
			removedEdges.clear();
		
		if (setProperties != null)
			setProperties.clear();
		
		if (removedProperties != null)
			removedProperties.clear();
	}
	
	public void addOperationType(CellOperationType type) {
		types.add(type);
	}
	
	
	public List<ImgEdge> getNewEdges() {
		if (newEdges == null)
			newEdges = new ArrayList<ImgEdge>();
		return newEdges;
	}
	
	public List<ImgEdge> getRemovedEdges() {
		if (removedEdges == null)
			removedEdges = new ArrayList<ImgEdge>();
		return removedEdges;
	}
	
	public TIntObjectMap<Object> getSetProperties() {
		if (setProperties == null)
			setProperties = new TIntObjectHashMap<Object>();
		return setProperties;
	}
	
	public TIntList getRemovedProperties() {
		if (removedProperties == null)
			removedProperties = new TIntArrayList();
		return removedProperties;
	}
	
	public void setCell(Cell cell) {
		this.cell = cell;
	}
	
	public Cell getCell() {
		return cell;
	}
	
	
	public long getCellId() {
		return cell.getId();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellOperations other = (CellOperations) obj;
		if (cell == null) {
			if (other.cell != null)
				return false;
		} else if (!cell.equals(other.cell))
			return false;
		return true;
	}

	public Set<CellOperationType> getTypes() {
		return types;
	}

	
	public boolean isRemovedCell() {
		return removedCell;
	}

	public void setRemovedCell(boolean removedCell) {
		this.removedCell = removedCell;
	}

	
	public static class ModifiedValue {
		private final Object oldValue;
		private final Object newValue;
		
		public ModifiedValue(Object oldValue, Object newValue) {
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public Object getOldValue() {
			return oldValue;
		}

		public Object getNewValue() {
			return newValue;
		}
		
		
	}
	 
	
	

}
