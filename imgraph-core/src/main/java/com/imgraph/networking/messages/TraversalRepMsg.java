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
package com.imgraph.networking.messages;


import com.imgraph.traversal.TraversalResults;

public class TraversalRepMsg extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6066489429282519013L;
	private TraversalResults traversalResults;
	private boolean completedSuccesfully;
	
	public TraversalRepMsg() {
		super(MessageType.TRAVERSAL_REP);
	}

	public TraversalResults getTraversalResults() {
		return traversalResults;
	}

	public void setTraversalResults(TraversalResults traversalResults) {
		this.traversalResults = traversalResults;
	}

	public boolean isCompletedSuccesfully() {
		return completedSuccesfully;
	}

	public void setCompletedSuccesfully(boolean completedSuccesfully) {
		this.completedSuccesfully = completedSuccesfully;
	}

	
	
	
}
