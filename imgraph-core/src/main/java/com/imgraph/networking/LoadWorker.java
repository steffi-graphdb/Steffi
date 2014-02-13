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
package com.imgraph.networking;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.imgraph.loader.TextFileLoader;
import com.imgraph.networking.messages.LoadMessage;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.MessageType;
import com.imgraph.storage.ImgpFileTools;
import com.tinkerpop.blueprints.impls.imgraph.ImgraphGraph;

/**
 * @author Aldemar Reynaga
 * Listens and process messages requesting the loading of vertices on the data base  
 */
public class LoadWorker implements Runnable {

	private Context context;
	private boolean alive;
	
	private int id;

	public LoadWorker(Context context, int id) {
		super();
		this.context = context;
		this.id = id;
	}
	
	public void stop() {
		alive = false;
	}

	@Override
	public void run() {
		Socket worker = context.socket(ZMQ.REP);
		worker.connect("inproc://load_backend_" + id);
		int newVertices = 0;
		
		alive = true;
		try {
			while (alive) {
		
				byte msg[] = worker.recv(0);
				
				
				LoadMessage message = (LoadMessage) Message.readFromBytes(msg);
				
				
				
				try {
					switch (message.getLoadFileType()) {
					case IMGP_FILE:
						ImgpFileTools.loadVertexBlock(ImgraphGraph.getInstance(), message.getVerticesInfo());
						newVertices = 0;
						break;
					
					default:
						newVertices = TextFileLoader.loadBlock(message.getVerticesInfo());
					}
					Message response =  new Message(MessageType.LOAD_REP); 
					response.setBody("OK::" + newVertices);
					worker.send(Message.convertMessageToBytes(response), 0);
					
					
					
				} catch (Exception x) {
					x.printStackTrace();
					Message response =  new Message(MessageType.LOAD_REP); 
					response.setBody("ERROR: " + x.getMessage());
					
					worker.send(Message.convertMessageToBytes(response), 0);
				}
		
			
				
			
			}
		} catch (Exception ie) {
			if (alive)
				ie.printStackTrace();
		} finally {
			worker.close();
		}


	}

}
