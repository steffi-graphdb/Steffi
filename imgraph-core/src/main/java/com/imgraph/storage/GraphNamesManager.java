package com.imgraph.storage;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.imgraph.model.ImgGraph;

/**
 * @author Aldemar Reynaga
 * Callable class used to register a new graph item name in all the data servers 
 */
public class GraphNamesManager implements Serializable, Callable<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4969351470224014844L;
	private final String name;
	
	public GraphNamesManager(String name) {
		this.name = name;
	}
	
	@Override
	public Boolean call() throws Exception {
		ImgGraph graph = ImgGraph.getInstance();
		
		graph.registerLocalItemName(name);
		
		return true;
	}

}
