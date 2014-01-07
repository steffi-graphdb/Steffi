package com.imgraph.networking;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.imgraph.networking.messages.AddressVertexReqMsg;
import com.imgraph.networking.messages.LocalNeighborsReqMsg;
import com.imgraph.networking.messages.Message;
import com.imgraph.networking.messages.Update2HNReqMsg;
import com.imgraph.networking.messages.WriteFileReqMsg;

/**
 * @author Aldemar Reynaga
 * Listens and process command requests messages arriving to the server
 */
public class CommandWorker implements Runnable {
	
	private Context context;
	private boolean alive;
	private String backendURL;
	

	public CommandWorker(Context context, String backendURL) {
		super();
		this.context = context;
		this.backendURL = backendURL;
	}
	
	public void stop() {
		alive = false;
	}
	
	

	@Override
	public void run() {
		Socket worker = context.socket(ZMQ.REP);
		worker.connect(backendURL);
		Message message;
		alive = true;
		try {
			while (alive) {
				byte msg[] = worker.recv(0);
				
				message = Message.readFromBytes(msg);
				
				
				
				switch (message.getType()) {
				case LOCAL_NEIGHBORS_REQ: //SYNC
					CommandProcessor.processLocal2HRequest(worker, (LocalNeighborsReqMsg) message);
					break;
				case WRITE_TO_FILE_REQ: //ASYNC
					CommandProcessor.processWriteFileRequest(worker, (WriteFileReqMsg) message);
					break;
				case UPD_2HOP_NEIGHBORS_REQ: //ASYNC
					CommandProcessor.processLocal2HopRequest(worker);
					break;
				case CLUSTER_ADDRESSES_REQ: //SYNC
					CommandProcessor.processClusterAddressRequest(worker);
					break;
				case ADDRESS_VERTEX_REQ: //SYNC
					CommandProcessor.processAddressVertexRequest(worker, (AddressVertexReqMsg) message);
					break;
				case NUMBER_OF_CELLS_REQ: //SYNC
					CommandProcessor.processCellNumberRequest(worker);
					break;
				case UPD_2HN_TRANSACTION_REQ: //ASYNC
					CommandProcessor.processUpdate2HNRequest(worker, (Update2HNReqMsg) message);
				default:
					break;

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
