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
package com.imgraph.storage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.infinispan.distribution.DistributionManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.Channel;
import org.jgroups.Event;
import org.jgroups.PhysicalAddress;
import org.jgroups.stack.IpAddress;
import org.zeromq.ZMQ;

import com.imgraph.common.Configuration;
import com.imgraph.model.ImgGraph;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.MessageType;

/**
 * @author Aldemar Reynaga
 * Functions related to the Infinispan storage of cells
 */
public abstract class StorageTools {
	
	
	public static String getCellAddress(Long cellId) {
		DistributionManager dm = CacheContainer.getCellCache().getAdvancedCache().getDistributionManager();
		return dm.getPrimaryLocation(cellId).toString();
	}
	
	public static void members() {
		DistributionManager dm = CacheContainer.getCellCache().getAdvancedCache().getDistributionManager();
		
	}
	
	public static Map<String, String> getAddressesIps() {
		Map<String, String> result =  new HashMap<String, String>();
		
		for (Address address : CacheContainer.getCacheContainer().getTransport().getMembers()) 
			result.put(address.toString(), getIpAddress(address));
		
		return result;
	}
	
	
	
	public static String getIpAddress(Address address) {
		String ipAddressHost = null;
		JGroupsAddress jgAddress = (JGroupsAddress) address;
		
		
		Channel channel = ((JGroupsTransport)CacheContainer.getCacheContainer().getTransport()).getChannel();
		
		PhysicalAddress physicalAddr = (PhysicalAddress)channel.down(new Event(Event.GET_PHYSICAL_ADDRESS, 
				jgAddress.getJGroupsAddress()));
        if(physicalAddr instanceof IpAddress) {
            IpAddress ipAddr = (IpAddress)physicalAddr;
            InetAddress inetAddr = ipAddr.getIpAddress();
            ipAddressHost = inetAddr.getHostAddress();
        }
		
		return ipAddressHost;
		
	}
	
	
	public static Map<String, Integer> countCellsInCluster() throws IOException {
		Map<String, Integer> results = new HashMap<String, Integer>();
		ZMQ.Context context = ImgGraph.getInstance().getZMQContext();
		Random random =  new Random();
		ZMQ.Socket socket = null;
		String localAddress = CacheContainer.getCellCache().getCacheManager().getAddress().toString();
		Map<String, String> clusterAddresses = StorageTools.getAddressesIps();
		
		try {
			for (Entry<String, String> entry : clusterAddresses.entrySet()) {
				socket = context.socket(ZMQ.REQ);
				socket.setIdentity(("cellCounter_" + localAddress + "_" + random.nextInt() + "_" + 
						entry.getValue()).getBytes());
				
				socket.connect("tcp://" + entry.getValue() + ":" + 
						Configuration.getProperty(Configuration.Key.NODE_PORT));
			
				socket.send(Message.convertMessageToBytes(new Message(MessageType.NUMBER_OF_CELLS_REQ)), 0);
				
				Message response = Message.readFromBytes(socket.recv(0)); 
				
				results.put(entry.getKey(), Integer.parseInt(response.getBody()));
				
				socket.close();
			}
		} finally {
			if (socket !=null)
				socket.close();
		}
		
		return results;
	}
	
	
}
