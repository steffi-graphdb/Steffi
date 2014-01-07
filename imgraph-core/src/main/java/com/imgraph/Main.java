package com.imgraph;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.imgraph.common.Configuration;
import com.imgraph.networking.ManagerServer;
import com.imgraph.networking.NodeServer;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.MessageType;
import com.imgraph.storage.CacheContainer;
import com.imgraph.testing.GraphTestCase;
import com.imgraph.testing.TestManager;

/**
 * @author Aldemar Reynaga
 * Starting point of the Imgraph class when executed from the JAR file
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
