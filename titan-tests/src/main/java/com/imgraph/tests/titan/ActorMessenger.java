package com.imgraph.tests.titan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class ActorMessenger {

	//private ActorSystem system;
	private List<ActorRef> actors;
	private String mainNodeIp;
	private String [] addresses;
	
	public ActorMessenger(ActorSystem system, String memberAddresses) {
		//this.system = system;
		addresses = memberAddresses.split(",");
		//system = ActorSystem.create("LookupApplication", ConfigFactory.load().getConfig("remotelookup"));
		mainNodeIp = addresses[0];
		actors = new ArrayList<ActorRef>();
		for (int i=1; i<addresses.length; i++) {
			//String path = "akka.tcp://TitanNode@" + addresses[i] + ":2552/user/managerActor";
		    //ActorRef actor = system.actorOf(new Props(ManagerActor.class), path), "lookupActor");
			ActorRef remoteActor = system.actorFor(
					"akka://TitanNode@" + addresses[i].trim() + ":2552/user/managerActor");
			actors.add(remoteActor);
		}
	}
	
	public int getNumberOfNodes() {
		return actors.size()+1;
	}
	
	public String getMainNodeIp() {
		return mainNodeIp;
	}
	
	
	
	public void sendMessage(Object message, int memberIndex) throws Exception {
		final Timeout t = new Timeout(Duration.create(5, TimeUnit.SECONDS));
		Future<Object> future = Patterns.ask(actors.get(memberIndex), message, t);
		Object result = Await.result(future, t.duration());
		if (!(result instanceof String))
			throw new Exception("Error processing the message on machine " + memberIndex);
	}
	
	
	public void startCassandraCluster(GraphTestCase graphTestCase, boolean batchMode) throws Exception {
		
		for (int i=0; i<actors.size(); i++) {
			CassandraStartMsg cassandraStartMsg = new CassandraStartMsg(graphTestCase.getWorkDirectory(), 
					graphTestCase.getStorageDirectory(), actors.size(), i, mainNodeIp, 
					addresses[i].trim(), batchMode);
			sendMessage(cassandraStartMsg, i);
			
		}
		
		
	}
	
}
