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
package com.steffi.networking;


import java.util.Map;
import java.util.Map.Entry;

import com.steffi.loader.ResponseProcessor;
import com.steffi.model.SteffiGraph;
import com.steffi.networking.messages.IdentifiableMessage;
import com.steffi.storage.StorageTools;

/**
 * @author Aldemar Reynaga
 * Handles a set of ZMQ sockets (using the ClientMultiThread class) to exchange asynchronous messages
 * with other members of the Infinispan cluster 
 */
public class NodeClients {
	private ClientMultiThread[] clients;
	
	public NodeClients(SteffiGraph graph) {
		Map<String, String> clusterAddresses = StorageTools.getAddressesIps();
		this.clients = new ClientMultiThread[graph.getNumberOfMembers()];
		ClientMultiThread client = null;
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			client = new ClientMultiThread(entry.getValue(), entry.getValue());
			clients[graph.getMemberIndex(entry.getKey())] =  client;
			new Thread(client).start();
		}
	}
	
	public void sendMessage(int memberIndex, IdentifiableMessage message, ResponseProcessor sender) {
		clients[memberIndex].addMsgToQueue(message, sender);
	}
	
	public void close() {
		for (ClientMultiThread client : clients)
			client.stop();
	}
}
