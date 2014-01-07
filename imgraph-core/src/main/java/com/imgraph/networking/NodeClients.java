package com.imgraph.networking;


import java.util.Map;
import java.util.Map.Entry;

import com.imgraph.loader.ResponseProcessor;
import com.imgraph.model.ImgGraph;
import com.imgraph.networking.messages.IdentifiableMessage;
import com.imgraph.storage.StorageTools;

/**
 * @author Aldemar Reynaga
 * Handles a set of ZMQ sockets (using the ClientMultiThread class) to exchange asynchronous messages
 * with other members of the Infinispan cluster 
 */
public class NodeClients {
	private ClientMultiThread[] clients;
	
	public NodeClients(ImgGraph graph) {
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
