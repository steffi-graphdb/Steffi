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
