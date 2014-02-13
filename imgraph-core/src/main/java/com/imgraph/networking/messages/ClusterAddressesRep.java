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

import java.util.Map;



public class ClusterAddressesRep extends Message {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7753197691713742316L;
	private Map<String, String> addressesIp;
	
	public Map<String, String> getAddressesIp() {
		return addressesIp;
	}

	public void setAddressesIp(Map<String, String> addressesIp) {
		this.addressesIp = addressesIp;
	}


	public ClusterAddressesRep() {
		super(MessageType.CLUSTER_ADDRESSES_REP);
	}

	@Override
	public String toString() {
		return super.toString() + " " + "ClusterAddressesRep [addressesIp=" + addressesIp + "]";
	}

	
	
	

}
