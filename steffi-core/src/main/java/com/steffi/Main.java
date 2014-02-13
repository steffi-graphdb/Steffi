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


import java.io.FileNotFoundException;
import java.io.IOException;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.steffi.common.Configuration;
import com.steffi.networking.ManagerServer;
import com.steffi.networking.NodeServer;
import com.steffi.networking.messages.Message;
import com.steffi.networking.messages.MessageType;
import com.steffi.storage.CacheContainer;
import com.steffi.testing.GraphTestCase;
import com.steffi.testing.TestManager;

/**
 * @author Aldemar Reynaga
 * Starting point of the Steffi class when executed from the JAR file
 */
public class Main {
	
	public static void sendStopMessage(String port){
		Context context = ZMQ.context(1);
		Socket client = context.socket(ZMQ.DEALER);
		try {
			client.setIdentity("STOP_CLIENT".getBytes());
			client.connect("tcp://localhost:" + port);
			Message stopMsg = new Message(MessageType.STOP);
			
			
			client.send(Message.convertMessageToBytes(stopMsg), 0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.close();
			context.term();
		}
		
		
	}
	
	
	public static void main(String [] args) throws FileNotFoundException, IOException, InterruptedException {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		if (args[0].equals("START_NODE")) {
			if (args.length == 2)
				Configuration.loadProperties(args[1]);
			
			CacheContainer.getCacheContainer().start();
			CacheContainer.getCellCache().start();
			
			new Thread(new NodeServer()).start();
			
		} else if (args[0].equals("STOP_NODE")){
			sendStopMessage(Configuration.getProperty(Configuration.Key.NODE_PORT));
		} else if (args[0].equals("START_MANAGER")){
			if (args.length == 2)
				Configuration.loadProperties(args[1]);
			
			
			new Thread(new ManagerServer()).start();
		} else if (args[0].equals("STOP_MANAGER")){
			sendStopMessage(Configuration.getProperty(Configuration.Key.MANAGER_PORT));
		} else if (args[0].equals("START_CONSOLE")) {
			try {
				if (args.length == 2)
					Configuration.loadProperties(args[1]);
				BasicConsole.runConsole();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (args[0].equals("TEST_CASE")) {
			GraphTestCase graphTestCase =  null;
			if (args.length == 3) {
				Configuration.loadProperties(args[1]);
				graphTestCase = new GraphTestCase(args[2]);
			} else {
				graphTestCase = new GraphTestCase(args[1]);
			}
			
			
			CacheContainer.getCacheContainer().start();
			CacheContainer.getCellCache().start();
			
			new Thread(new NodeServer()).start();
			
			Thread.sleep(2000);
			
			TestManager testManager = new TestManager(graphTestCase);
			testManager.execute();
			
			System.out.println("Test case processal finished");
			Thread.sleep(2000);
			sendStopMessage(Configuration.getProperty(Configuration.Key.MANAGER_PORT));
			
			
		} else if (args[0].equals("LOADER")) {
			
			
			
			LoaderConsole.loadFile();
		} 
		
		
	}
}
