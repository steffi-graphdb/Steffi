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
package com.steffi.testing;

/**
 * @author Aldemar Reynaga
 * A pair of nodes representing the start and end vertex ids
 */
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
