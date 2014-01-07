package com.imgraph.storage;

/**
 * @author Aldemar Reynaga
 * Types of operations on a cell that can be done inside a transaction
 */
public enum CellOperationType {
	CREATE_CELL,
	ADD_EDGE,
	REMOVE_EDGE,
	SET_CELL_PROPERTY,
	REMOVE_CELL_PROPERTY
}
