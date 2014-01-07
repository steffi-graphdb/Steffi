package com.imgraph.networking.messages;



/**
 * @author Aldemar Reynaga
 * Defines the types of messages handled by Imgraph, each new message type must be registered here. Optionally a 
 * subclass of Message could be implemented for this new message type  
 */
public enum MessageType  {
	STOP,
	CLEAR,
	LOAD_REQ,
	LOAD_REP,
	SEARCH_REQ,
	SEARCH_REP,
	END_SEARCH,
	SEARCH_INFO,
	TRAVERSAL_REQ,
	INIT_TRAVERSAL,
	TRAVERSAL_REP,
	CLUSTER_ADDRESSES_REQ,
	CLUSTER_ADDRESSES_REP,
	ADDRESS_VERTEX_REQ,
	ADDRESS_VERTEX_REP,
	WRITE_TO_FILE_REQ,
	WRITE_TO_FILE_REP,
	UPD_2HOP_NEIGHBORS_REQ,
	UPD_2HOP_NEIGHBORS_REP,
	CONFIG_CLUSTER_REQ,
	CONFIG_CLUSTER_REP,
	LOCAL_NEIGHBORS_REQ,
	LOCAL_NEIGHBORS_REP,
	NUMBER_OF_CELLS_REQ,
	NUMBER_OF_CELLS_REP,
	UPD_2HN_TRANSACTION_REQ,
	UPD_2HN_TRANSACTION_REP;
	
	
	
}


