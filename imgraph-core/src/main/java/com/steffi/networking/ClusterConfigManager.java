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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.steffi.loader.ResponseProcessor;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.storage.StorageTools;

/**
 * @author Aldemar Reynaga
 * This class sends a message to all the members of the cluster to initialize functionalities
 * required to process traversals
 */
public class ClusterConfigManager implements ResponseProcessor {
	
	private boolean processing;
	private int pendingRequests;
	private boolean processingError;
	private Object lock;
	private List<ClientThread> clientThreads;
	private Map<String, String> clusterAddresses;
	
	public ClusterConfigManager() {
		
		clusterAddresses = StorageTools.getAddressesIps();
		initClientThreads();
	}
	
	private void initClientThreads() {
		this.clientThreads = new ArrayList<ClientThread>();
		ClientThread clientThread = null;
		for (Entry<String, String> entry : clusterAddresses.entrySet()) {
			clientThread = new ClientThread(entry.getValue(), entry.getValue(), "clusterManager",
					this);
			clientThreads.add(clientThread);
			new Thread(clientThread).start();
		}
	}
	
	public void initialize() throws Exception {
		Message request = new Message(MessageType.CONFIG_CLUSTER_REQ);
		
		processing = true;
		processingError = false;
		for (ClientThread ct : clientThreads) 
			ct.addMsgToQueue(request);
		pendingRequests = clientThreads.size();
		
		lock = new String("CONFIG_CLUSTER");
		
		synchronized (lock) {
			while (processing) {
				lock.wait();
			}
		}
		
		if (processingError)
			throw new Exception("There was an error configuring the cluster");
		
	}

	@Override
	public synchronized void processResponse(Message message) {
		if (message.getType().equals(MessageType.CONFIG_CLUSTER_REP)) {
			if (message.getBody().equals("OK")) {
				pendingRequests--;
				if (pendingRequests == 0) {
					synchronized (lock) {
						processing = false;
						lock.notifyAll();
					}
				}
			} else {
				processingError = true;
				synchronized (lock) {
					processing = false;
					lock.notifyAll();
				}
				
			}
			
		}
	}
	
	public void closeClientThreads() {
		if (clientThreads != null)
			for (ClientThread lt : clientThreads)
				lt.stop();
	}
	
}
