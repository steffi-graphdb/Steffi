package com.imgraph.tests.titan;

import com.thinkaurelius.titan.core.TitanGraph;

import akka.actor.UntypedActor;

public class ManagerActor extends UntypedActor {

	private TitanGraph graph;
	
	@Override
	public void onReceive(Object message) throws Exception {
		System.out.println("Message received of type: " + message.getClass().getName());
		if (message instanceof CassandraStartMsg) {
			TestTools.startTitan((CassandraStartMsg) message);
			
		} else if (message instanceof StopMessage) {
			if (graph != null) {
				graph.shutdown();
				graph = null;
				System.out.println("Titan graph was shutdown");
			}
			if (((StopMessage) message).isStopSystem()) {
				//TODO: ???
			}
		} else {
			unhandled(message);
		}
		
	}

	

}
