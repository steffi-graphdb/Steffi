/*******************************************************************************
 * Copyright (c) 2014 EURA NOVA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Aldemar Reynaga - initial API and implementation
 *     Salim Jouili - initial API and implementation
 ******************************************************************************/
package com.steffi.traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Aldemar Reynaga
 * Results for a distributed traversal request
 */
public class DistributedSearchResults {
	private TraversalResults traversalresults;
	private List<UUID> sentSearchMsgIds;
	
	public DistributedSearchResults() {
		traversalresults = new TraversalResultsImpl();
		sentSearchMsgIds =  new ArrayList<UUID>();
	}
	
	
	public TraversalResults getTraversalresults() {
		return traversalresults;
	}
	public void setTraversalresults(TraversalResults traversalresults) {
		this.traversalresults = traversalresults;
	}
	public List<UUID> getSentSearchMsgIds() {
		return sentSearchMsgIds;
	}
	public void setSentSearchMsgIds(List<UUID> sentSearchMsgIds) {
		this.sentSearchMsgIds = sentSearchMsgIds;
	}
	
	
	
}
