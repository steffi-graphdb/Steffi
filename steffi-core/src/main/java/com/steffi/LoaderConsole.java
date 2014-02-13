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
package com.steffi;

import java.util.Map;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.steffi.common.Configuration;
import com.steffi.common.IOUtils;
import com.steffi.loader.TextFileLoader;
import com.steffi.networking.messages.ClusterAddressesRep;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.networking.messages.LoadMessage.LoadFileType;


/**
 * @author Aldemar Reynaga
 * Functions to load text files from a machine not belonging to the Imgraph cluster  
 */
public class LoaderConsole {

	
	private static Map<String, String> loadAddressesIps(String clientIp) throws Exception {
		Context context = ZMQ.context(1);
		Socket requester = context.socket(ZMQ.REQ);
		
		try {
			requester.connect("tcp://" + clientIp + ":" + Configuration.getProperty(Configuration.Key.NODE_PORT));
			
			Message message = new Message(MessageType.CLUSTER_ADDRESSES_REQ);
			
			requester.send(Message.convertMessageToBytes(message), 0);
			
			
			byte[] reply = requester.recv(0);
			
			ClusterAddressesRep adRep = (ClusterAddressesRep) Message.readFromBytes(reply);
			return adRep.getAddressesIp();
			
		} finally {
			requester.close();
			context.term();
		}
		
	}
	
	public static void loadFile() {
		String ipMember, fileName;
		boolean isDirected;
		LoadFileType loadFileType;
		Map<String, String> addressesIps;
		TextFileLoader textFileLoader = null;
		
		try {
			ipMember = IOUtils.readLine("Enter the IP address of one member of the cluster: ");
			fileName = IOUtils.readLine("Enter the data file name: ");
			isDirected = IOUtils.readLine("Directed (Y/N): ").equals("Y");
			loadFileType = IOUtils.readLine("Adjacent List file type (Y/N)?: ").equals("Y")?LoadFileType.ADJ_LIST_TEXT_FILE:
							LoadFileType.SIMPLE_TEXT_FILE;
			
			addressesIps = loadAddressesIps(ipMember);
			textFileLoader = new TextFileLoader(addressesIps, ipMember);
			textFileLoader.load(fileName, loadFileType, isDirected);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (textFileLoader != null)
				textFileLoader.close();
		}

	}

}
